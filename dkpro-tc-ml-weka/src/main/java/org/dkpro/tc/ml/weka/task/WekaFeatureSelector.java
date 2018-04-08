/**
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.ml.weka.task;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;

import meka.filters.unsupervised.attribute.MekaClassAttributes;
import mulan.data.InvalidDataFormatException;
import mulan.data.LabelNodeImpl;
import mulan.data.LabelsMetaDataImpl;
import mulan.data.MultiLabelInstances;
import mulan.dimensionalityReduction.BinaryRelevanceAttributeEvaluator;
import mulan.dimensionalityReduction.LabelPowersetAttributeEvaluator;
import mulan.dimensionalityReduction.Ranker;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.AttributeSelection;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class WekaFeatureSelector
    implements Constants
{
    private Instances trainData;
    private Instances testData;
    private boolean multiLabel;
    private List<String> attributeEvaluator;
    private List<String> featureSearcher;
    private String labelTransformationMethod;

    public final static String featureSelectionFile = "featureSelection.txt";
    private File workingFolder;
    private int numLabelsToKeep;

    public WekaFeatureSelector(Instances trainData, Instances testData, boolean multiLabel,
            List<String> attributeEvaluator, List<String> featureSearcher,
            String labelTransformationMethod, int numLabelsToKeep, File workingFolder)
    {
        this.trainData = trainData;
        this.testData = testData;
        this.multiLabel = multiLabel;
        this.attributeEvaluator = attributeEvaluator;
        this.featureSearcher = featureSearcher;
        this.labelTransformationMethod = labelTransformationMethod;
        this.numLabelsToKeep = numLabelsToKeep;
        this.workingFolder = workingFolder;
    }

    public void apply() throws Exception
    {
        // FEATURE SELECTION
        if (!multiLabel) {
            if (featureSearcher != null && attributeEvaluator != null) {
                AttributeSelection attSel = featureSelectionSinglelabel(trainData, featureSearcher,
                        attributeEvaluator);
                FileUtils.writeStringToFile(new File(workingFolder, featureSelectionFile),
                        attSel.toResultsString(), "utf-8");
                trainData = attSel.reduceDimensionality(trainData);
                testData = attSel.reduceDimensionality(testData);
            }
        }
        else {
            if (attributeEvaluator != null && labelTransformationMethod != null
                    && numLabelsToKeep > 0) {
                Remove attSel = featureSelectionMultilabel(trainData, attributeEvaluator,
                        labelTransformationMethod, numLabelsToKeep);
                Logger.getLogger(getClass()).info("APPLYING FEATURE SELECTION");
                trainData = applyAttributeSelectionFilter(trainData, attSel);
                testData = applyAttributeSelectionFilter(testData, attSel);
            }
        }

    }
    
    public Instances getTrainingInstances() {
        return trainData;
    }
    
    public Instances getTestingInstances() {
        return testData;
    }

    /**
     * Applies a filter to reduce the dimension of attributes and reorders them to be used within
     * Meka
     * 
     * @param trainData
     *            the train data
     * @param removeFilter
     *            remove filter
     * @return weka instances
     * @throws Exception
     *             in case of error
     */
    public static Instances applyAttributeSelectionFilter(Instances trainData, Remove removeFilter)
        throws Exception
    {
        // less attributes than should be kept => ignore filter
        if (removeFilter == null) {
            return trainData;
        }

        Instances filtered = Filter.useFilter(trainData, removeFilter);
        filtered.setClassIndex(trainData.classIndex());
        // swap attributes to fit MEKA
        MekaClassAttributes attFilter = new MekaClassAttributes();
        attFilter.setAttributeIndices(
                filtered.numAttributes() - trainData.classIndex() + 1 + "-last");
        attFilter.setInputFormat(filtered);
        filtered = Filter.useFilter(filtered, attFilter);
        int newClassindex = filtered.classIndex();
        filtered.setRelationName(
                filtered.relationName().replaceAll("\\-C\\s[\\d]+", "-C " + newClassindex));

        return filtered;
    }

    public Remove featureSelectionMultilabel(Instances trainData, List<String> attributeEvaluator,
            String labelTransformationMethod, int numLabelsToKeep)
        throws TextClassificationException
    {
        // file to hold the results of attribute selection
        File fsResultsFile = new File(workingFolder, featureSelectionFile);

        // filter for reducing dimension of attributes
        Remove filterRemove = new Remove();
        try {
            MultiLabelInstances mulanInstances = convertMekaInstancesToMulanInstances(trainData);

            ASEvaluation eval = ASEvaluation.forName(attributeEvaluator.get(0), attributeEvaluator
                    .subList(1, attributeEvaluator.size()).toArray(new String[0]));

            AttributeEvaluator attributeSelectionFilter;

            // We currently only support the following Mulan Transformation methods (configuration
            // is complicated due to missing commandline support of mulan):
            if (labelTransformationMethod.equals("LabelPowersetAttributeEvaluator")) {
                attributeSelectionFilter = new LabelPowersetAttributeEvaluator(eval,
                        mulanInstances);
            }
            else if (labelTransformationMethod.equals("BinaryRelevanceAttributeEvaluator")) {
                attributeSelectionFilter = new BinaryRelevanceAttributeEvaluator(eval,
                        mulanInstances, "max", "none", "rank");
            }
            else {
                throw new TextClassificationException(
                        "This Label Transformation Method is not supported.");
            }

            Ranker r = new Ranker();
            int[] result = r.search(attributeSelectionFilter, mulanInstances);

            // collect evaluation for *all* attributes and write to file
            StringBuffer evalFile = new StringBuffer();
            for (Attribute att : mulanInstances.getFeatureAttributes()) {
                evalFile.append(att.name() + ": " + attributeSelectionFilter
                        .evaluateAttribute(att.index() - mulanInstances.getNumLabels()) + "\n");
            }
            FileUtils.writeStringToFile(fsResultsFile, evalFile.toString(), "utf-8");

            // create a filter to reduce the dimension of the attributes
            int[] toKeep = new int[numLabelsToKeep + mulanInstances.getNumLabels()];
            System.arraycopy(result, 0, toKeep, 0, numLabelsToKeep);
            int[] labelIndices = mulanInstances.getLabelIndices();
            System.arraycopy(labelIndices, 0, toKeep, numLabelsToKeep,
                    mulanInstances.getNumLabels());

            filterRemove.setAttributeIndicesArray(toKeep);
            filterRemove.setInvertSelection(true);
            filterRemove.setInputFormat(mulanInstances.getDataSet());
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // less attributes than we want => no filtering
            return null;
        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }
        return filterRemove;
    }

    /**
     * Converts the Meka-specific instances format to Mulan-specific instances. Hierarchical
     * relationships among labels cannot be expressed.
     * 
     * @param instances
     *            instances
     * @return multi label instances
     * @throws InvalidDataFormatException
     *             in case of data format error
     */
    private MultiLabelInstances convertMekaInstancesToMulanInstances(Instances instances)
        throws InvalidDataFormatException
    {
        LabelsMetaDataImpl labelsMetaDataImpl = new LabelsMetaDataImpl();
        for (int i = 0; i < instances.classIndex(); i++) {
            String classAttName = instances.attribute(i).name();
            LabelNodeImpl labelNodeImpl = new LabelNodeImpl(classAttName);
            labelsMetaDataImpl.addRootNode(labelNodeImpl);
        }
        return new MultiLabelInstances(instances, labelsMetaDataImpl);
    }

    /**
     * feature selection
     * 
     * @param aContext
     *            lab context
     * @param trainData
     *            weka instances
     * @param featureSearcher
     *            searcher
     * @param attributeEvaluator
     *            evaluator
     * @return attribute selection
     * @throws Exception
     *             in case of errors
     */
    private AttributeSelection featureSelectionSinglelabel(Instances trainData,
            List<String> featureSearcher, List<String> attributeEvaluator)
        throws Exception
    {
        AttributeSelection selector = singleLabelAttributeSelection(trainData, featureSearcher,
                attributeEvaluator);
        FileUtils.writeStringToFile(new File(workingFolder, featureSelectionFile),
                selector.toResultsString(), "utf-8");
        return selector;
    }

    /**
     * Feature selection using Weka.
     * 
     * @param trainData
     *            weka train data
     * @param featureSearcher
     *            list of features
     * @param attributeEvaluator
     *            list of attribute evaluators
     * @return attribute selection
     * @throws Exception
     *             in case of errors
     */
    public static AttributeSelection singleLabelAttributeSelection(Instances trainData,
            List<String> featureSearcher, List<String> attributeEvaluator)
        throws Exception
    {
        AttributeSelection selector = new AttributeSelection();

        // Get feature searcher
        ASSearch search = ASSearch.forName(featureSearcher.get(0),
                featureSearcher.subList(1, featureSearcher.size()).toArray(new String[0]));
        // Get attribute evaluator
        ASEvaluation evaluation = ASEvaluation.forName(attributeEvaluator.get(0),
                attributeEvaluator.subList(1, attributeEvaluator.size()).toArray(new String[0]));

        selector.setSearch(search);
        selector.setEvaluator(evaluation);
        selector.SelectAttributes(trainData);

        return selector;
    }

}
