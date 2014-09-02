/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.weka.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import meka.classifiers.multilabel.MultilabelClassifier;
import meka.core.MLUtils;
import meka.core.Result;
import meka.core.ThresholdUtils;
import meka.filters.unsupervised.attribute.MekaClassAttributes;
import mulan.data.InvalidDataFormatException;
import mulan.data.LabelNodeImpl;
import mulan.data.LabelsMetaDataImpl;
import mulan.data.MultiLabelInstances;
import mulan.dimensionalityReduction.BinaryRelevanceAttributeEvaluator;
import mulan.dimensionalityReduction.LabelPowersetAttributeEvaluator;
import mulan.dimensionalityReduction.Ranker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.bzip2.CBZip2InputStream;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.AttributeSelection;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaTestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaFeatureEncoder;

/**
 * Utils for WEKA
 */
public class WekaUtils
    implements Constants
{

    /**
     * Name of the relation == name of the arff file
     */
    public static final String RELATION_NAME = "dkpro-tc-generated";

    /**
     * Suffix for class label names in the test data that have been adapted to match the training
     * data
     * 
     * @see #makeOutcomeClassesCompatible(Instances, Instances, boolean)
     */
    public static final String COMPATIBLE_OUTCOME_CLASS = "_Comp";

    /**
     * Adapts the test data class labels to the training data. Class labels from the test data
     * unseen in the training data will be deleted from the test data. Class labels from the
     * training data unseen in the test data will be added to the test data. If training and test class
     * labels are equal, nothing will be done.
     * 
     * @param trainData
     *            training data
     * @param testData
     *            test data
     * @param multilabel
     *            whether this is a multi-label classification problem
     * @return the adapted test data
     * @throws Exception
     */
    public static Instances makeOutcomeClassesCompatible(Instances trainData, Instances testData,
            boolean multilabel)
        throws Exception
    {
        // new (compatible) test data
        Instances compTestData = null;

        // ================ SINGLE LABEL BRANCH ======================
        if (!multilabel) {
            // retrieve class labels
            Enumeration trainOutcomeValues = trainData.classAttribute().enumerateValues();
            Enumeration testOutcomeValues = testData.classAttribute().enumerateValues();
            ArrayList trainLabels = Collections.list(trainOutcomeValues);
            ArrayList testLabels = Collections.list(testOutcomeValues);

            // add new outcome class attribute to test data
            Add addFilter = new Add();
            addFilter.setNominalLabels(StringUtils.join(trainLabels, ','));
            addFilter.setAttributeName(Constants.CLASS_ATTRIBUTE_NAME + COMPATIBLE_OUTCOME_CLASS);
            addFilter.setInputFormat(testData);
            testData = Filter.useFilter(testData, addFilter);

            // fill NEW test data with values from old test data plus the new class attribute
            compTestData = new Instances(testData, testData.numInstances());
            for (int i = 0; i < testData.numInstances(); i++) {
                weka.core.Instance instance = testData.instance(i);
                String label = (String) testLabels.get(new Double(instance.value(testData
                        .classAttribute())).intValue());
                if (trainLabels.indexOf(label) != -1) {
                    instance.setValue(
                            testData.attribute(Constants.CLASS_ATTRIBUTE_NAME
                                    + COMPATIBLE_OUTCOME_CLASS), label);
                }
                else {
                    instance.setMissing(testData.classIndex());
                }
                compTestData.add(instance);
            }

            // remove old class attribute
            Remove remove = new Remove();
            remove.setAttributeIndices(Integer.toString(compTestData.attribute(
                    Constants.CLASS_ATTRIBUTE_NAME).index() + 1));
            remove.setInvertSelection(false);
            remove.setInputFormat(compTestData);
            compTestData = Filter.useFilter(compTestData, remove);

            // set new class attribute
            compTestData.setClass(compTestData.attribute(Constants.CLASS_ATTRIBUTE_NAME
                    + COMPATIBLE_OUTCOME_CLASS));
        }
        // ================ MULTI LABEL BRANCH ======================
        else {

            int numTrainLabels = trainData.classIndex();
            int numTestLabels = testData.classIndex();

            ArrayList<String> trainLabels = getLabels(trainData);
            // ArrayList<String> testLabels = getLabels(testData);

            // add new outcome class attributes to test data

            Add filter = new Add();
            for (int i = 0; i < numTrainLabels; i++) {
                // numTestLabels +i (because index starts from 0)
                filter.setAttributeIndex(new Integer(numTestLabels + i + 1).toString());
                filter.setNominalLabels("0,1");
                filter.setAttributeName(trainData.attribute(i).name() + COMPATIBLE_OUTCOME_CLASS);
                filter.setInputFormat(testData);
                testData = Filter.useFilter(testData, filter);
            }

            // fill NEW test data with values from old test data plus the new class attributes
            compTestData = new Instances(testData, testData.numInstances());
            for (int i = 0; i < testData.numInstances(); i++) {
                weka.core.Instance instance = testData.instance(i);
                // fullfill with 0.
                for (int j = 0; j < numTrainLabels; j++) {
                    instance.setValue(j + numTestLabels, 0.);
                }
                // fill the real values:
                for (int j = 0; j < numTestLabels; j++) {
                    // part of train data: forget labels which are not part of the train data
                    if (trainLabels.indexOf(instance.attribute(j).name()) != -1) {
                        // class label found in test data
                        int index = trainLabels.indexOf(instance.attribute(j).name());
                        instance.setValue(index + numTestLabels, instance.value(j));
                    }
                }
                compTestData.add(instance);
            }

            // remove old class attributes
            for (int i = 0; i < numTestLabels; i++) {
                Remove remove = new Remove();
                remove.setAttributeIndices("1");
                remove.setInvertSelection(false);
                remove.setInputFormat(compTestData);
                compTestData = Filter.useFilter(compTestData, remove);
            }

            // adapt header and set new class label
            String relationTag = compTestData.relationName();
            compTestData.setRelationName(relationTag.substring(0, relationTag.indexOf("-C") + 2)
                    + " " + numTrainLabels + " ");
            compTestData.setClassIndex(numTrainLabels);
        }
        return compTestData;
    }

    private static ArrayList<String> getLabels(Instances data)
    {
        int numLabels = data.classIndex();
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < numLabels; i++) {
            list.add(data.attribute(i).name());
        }
        return list;
    }

    /**
     * Converts a feature store to a list of instances. Single-label case.
     * 
     * @param outputFile
     * @param instanceList
     * @throws Exception
     */
    public static void instanceListToArffFile(File outputFile, FeatureStore instanceList)
        throws Exception
    {
        instanceListToArffFile(outputFile, instanceList, false, false);
    }

    /**
     * Converts a feature store to a list of instances. Single-label case.
     * 
     * TODO: maybe rename to "featureStore2ArffFile"
     * 
     * @param outputFile
     * @param instanceList
     * @param useDenseInstances
     * @param isRegressionExperiment
     * @throws Exception
     */
    public static void instanceListToArffFile(File outputFile, FeatureStore instanceList,
            boolean useDenseInstances, boolean isRegressionExperiment)
        throws Exception
    {

        // check for error conditions
        if (instanceList.getUniqueOutcomes().isEmpty()) {
            throw new IllegalArgumentException("List of instance outcomes is empty.");
        }

        // Filter preprocessingFilter = new ReplaceMissingValuesWithZeroFilter();

        AttributeStore attributeStore = WekaFeatureEncoder.getAttributeStore(instanceList);

        // Make sure "outcome" is not the name of an attribute
        List<String> outcomeList = new ArrayList<String>(instanceList.getUniqueOutcomes());
        Attribute outcomeAttribute = createOutcomeAttribute(outcomeList,
                isRegressionExperiment);
        if (attributeStore.containsAttributeName(CLASS_ATTRIBUTE_NAME)) {
            System.err
                    .println("A feature with name \"outcome\" was found. Renaming outcome attribute");
            outcomeAttribute = outcomeAttribute.copy(CLASS_ATTRIBUTE_PREFIX + CLASS_ATTRIBUTE_NAME);
        }
        attributeStore.addAttribute(outcomeAttribute.name(), outcomeAttribute);

        Instances wekaInstances = new Instances(RELATION_NAME, attributeStore.getAttributes(),
                instanceList.size());
        wekaInstances.setClass(outcomeAttribute);

        if (!outputFile.exists()) {
            outputFile.mkdirs();
            outputFile.createNewFile();
        }

        ArffSaver saver = new ArffSaver();
        // preprocessingFilter.setInputFormat(wekaInstances);
        saver.setRetrieval(Saver.INCREMENTAL);
        saver.setFile(outputFile);
        saver.setCompressOutput(true);
        saver.setInstances(wekaInstances);

        for (int i = 0; i < instanceList.size(); i++) {
            Instance instance = instanceList.getInstance(i);

            double[] featureValues = getFeatureValues(attributeStore, instance);

            weka.core.Instance wekaInstance;

            if (useDenseInstances) {
                wekaInstance = new DenseInstance(1.0, featureValues);
            }
            else {
                wekaInstance = new SparseInstance(1.0, featureValues);
            }

            wekaInstance.setDataset(wekaInstances);

            String outcome = instanceList.getOutcomes(i).get(0);
            if (isRegressionExperiment) {
                wekaInstance.setClassValue(Double.parseDouble(outcome));
            }
            else {
                wekaInstance.setClassValue(outcome);
            }

            // preprocessingFilter.input(wekaInstance);
            // saver.writeIncremental(preprocessingFilter.output());
            saver.writeIncremental(wekaInstance);
        }

        // finishes the incremental saving process
        saver.writeIncremental(null);
    }

    /**
     * /** Converts a feature store to a list of instances. Multi-label case.
     * 
     * TODO: maybe rename to "featureStore2ArffFile"
     * 
     * @param outputFile
     * @param featureStore
     * @param useDenseInstances
     * @throws Exception
     */
    public static void instanceListToArffFileMultiLabel(File outputFile, FeatureStore featureStore,
            boolean useDenseInstances)
        throws Exception
    {

        // Filter preprocessingFilter = new ReplaceMissingValuesWithZeroFilter();

        AttributeStore attributeStore = WekaFeatureEncoder.getAttributeStore(featureStore);

        List<Attribute> outcomeAttributes = createOutcomeAttributes(new ArrayList<String>(
                featureStore.getUniqueOutcomes()));

        // in Meka, class label attributes have to go on top
        for (Attribute attribute : outcomeAttributes) {
            attributeStore.addAttributeAtBegin(attribute.name(), attribute);
        }

        // for Meka-internal use
        Instances wekaInstances = new Instances(RELATION_NAME + ": -C " + outcomeAttributes.size()
                + " ", attributeStore.getAttributes(), featureStore.size());
        wekaInstances.setClassIndex(outcomeAttributes.size());

        if (!outputFile.exists()) {
            outputFile.mkdirs();
            outputFile.createNewFile();
        }

        ArffSaver saver = new ArffSaver();
        // preprocessingFilter.setInputFormat(wekaInstances);
        saver.setRetrieval(Saver.INCREMENTAL);
        saver.setFile(outputFile);
        saver.setCompressOutput(true);
        saver.setInstances(wekaInstances);

        for (int i = 0; i < featureStore.size(); i++) {
            Instance instance = featureStore.getInstance(i);

            double[] featureValues = getFeatureValues(attributeStore, instance);

            // set class label values
            List<String> instanceOutcome = instance.getOutcomes();
            for (Attribute label : outcomeAttributes) {
                String labelname = label.name();
                featureValues[attributeStore.getAttributeOffset(labelname)] = instanceOutcome
                        .contains(labelname.split(CLASS_ATTRIBUTE_PREFIX)[1]) ? 1.0d : 0.0d;
            }

            weka.core.Instance wekaInstance;

            if (useDenseInstances) {
                wekaInstance = new DenseInstance(1.0, featureValues);
            }
            else {
                wekaInstance = new SparseInstance(1.0, featureValues);
            }

            wekaInstance.setDataset(wekaInstances);

            // preprocessingFilter.input(wekaInstance);
            // saver.writeIncremental(preprocessingFilter.output());
            saver.writeIncremental(wekaInstance);
        }

        // finishes the incremental saving process
        saver.writeIncremental(null);
    }

    /**
     * Converts a TC instance object into a Meka instance object, compatible with the given
     * attribute set and class labels.
     * 
     * @param instance
     *            tc instance object
     * @param attributes
     *            the attribute set which will be used to create the header information of the
     *            resulting instance, class attributes must be at the beginning
     * @param allClassLabels
     *            names of all classes
     * @return a Meka instance object, without any class values assigned
     * @throws Exception
     */
    public static weka.core.Instance tcInstanceToMekaInstance(Instance instance,
            List<Attribute> attributes, List<String> allClassLabels)
        throws Exception
    {
        // Filter preprocessingFilter = new ReplaceMissingValuesWithZeroFilter();

        AttributeStore attributeStore = new AttributeStore();
        List<Attribute> outcomeAttributes = createOutcomeAttributes(allClassLabels);

        // in Meka, class label attributes have to go on top
        for (Attribute attribute : outcomeAttributes) {
            attributeStore.addAttributeAtBegin(attribute.name(), attribute);
        }

        for (int i = outcomeAttributes.size(); i < attributes.size(); i++) {
            attributeStore.addAttribute(attributes.get(i).name(), attributes.get(i));
        }

        // for Meka-internal use
        Instances wekaInstances = new Instances(RELATION_NAME + ": -C " + outcomeAttributes.size()
                + " ", attributeStore.getAttributes(), instance.getFeatures().size());
        wekaInstances.setClassIndex(outcomeAttributes.size());
        // System.out.println(instances);
        // preprocessingFilter.setInputFormat(wekaInstances);

        double[] featureValues = getFeatureValues(attributeStore, instance);

        SparseInstance sparseInstance = new SparseInstance(1.0, featureValues);
        sparseInstance.setDataset(wekaInstances);
        // preprocessingFilter.input(sparseInstance);
        // return preprocessingFilter.output();
        return sparseInstance;
    }

    private static Attribute createOutcomeAttribute(List<String> outcomeValues, boolean isRegresion)
    {
        if (isRegresion) {
            return new Attribute(CLASS_ATTRIBUTE_NAME);
        }
        else {
            // make the order of the attributes predictable
            Collections.sort(outcomeValues);
            return new Attribute(CLASS_ATTRIBUTE_NAME, outcomeValues);
        }
    }

    private static List<Attribute> createOutcomeAttributes(List<String> outcomeValues)
    {
        // make the order of the attributes predictable
        Collections.sort(outcomeValues);
        List<Attribute> atts = new ArrayList<Attribute>();

        for (String outcome : outcomeValues) {
            atts.add(new Attribute(CLASS_ATTRIBUTE_PREFIX + outcome, Arrays.asList(new String[] {
                    "0", "1" })));
        }
        return atts;
    }

    /**
     * Converts a TC instance object into a Weka instance object, compatible with the given
     * attribute set and class labels.
     * 
     * @param instance
     *            tc instance object
     * @param attributes
     *            the attribute set which will be used to create the header information of the
     *            resulting instance, class attribute must be at the end
     * @param allClasses
     *            class label names
     * @param isRegressionExperiment
     * @return a Weka instance object, class value is set to missing
     * @throws Exception
     */
    public static weka.core.Instance tcInstanceToWekaInstance(Instance instance,
            List<Attribute> attributes, List<String> allClasses, boolean isRegressionExperiment)
        throws Exception
    {
        // Filter preprocessingFilter = new ReplaceMissingValuesWithZeroFilter();
        AttributeStore attributeStore = new AttributeStore();

        // outcome attribute is last and will be ignored
        for (int i = 0; i < attributes.size() - 1; i++) {
            attributeStore.addAttribute(attributes.get(i).name(), attributes.get(i));
        }

        // add outcome attribute
        Attribute outcomeAttribute = createOutcomeAttribute(allClasses, isRegressionExperiment);
        attributeStore.addAttribute(outcomeAttribute.name(), outcomeAttribute);

        Instances wekaInstances = new Instances(RELATION_NAME, attributeStore.getAttributes(),
                instance.getFeatures().size());
        wekaInstances.setClass(outcomeAttribute);

        // preprocessingFilter.setInputFormat(wekaInstances);

        double[] featureValues = getFeatureValues(attributeStore, instance);

        SparseInstance sparseInstance = new SparseInstance(1.0, featureValues);
        sparseInstance.setDataset(wekaInstances);
        // sparseInstance.setClassMissing();
        // preprocessingFilter.input(sparseInstance);
        // return preprocessingFilter.output();
        return sparseInstance;
    }

    private static double[] getFeatureValues(AttributeStore attributeStore, Instance instance)
    {
        double[] featureValues = new double[attributeStore.getAttributes().size()];

        for (Feature feature : instance.getFeatures()) {

            try {
                Attribute attribute = attributeStore.getAttribute(feature.getName());
                Object featureValue = feature.getValue();

                double attributeValue;
                if (featureValue instanceof Number) {
                    // numeric attribute
                    attributeValue = ((Number) feature.getValue()).doubleValue();
                }
                else if (featureValue instanceof Boolean) {
                    // boolean attribute
                    attributeValue = (Boolean) featureValue ? 1.0d : 0.0d;
                }
                else if (featureValue instanceof MissingValue) {
                    // missing value
                    attributeValue = WekaFeatureEncoder.getMissingValueConversionMap().get(
                            ((MissingValue) featureValue).getType());
                }
                else if (featureValue == null) {
                    // null
                    throw new IllegalArgumentException(
                            "You have an instance which doesn't specify a value for the feature "
                                    + feature.getName());
                }
                else {
                    // nominal or string
                    Object stringValue = feature.getValue();
                    if (!attribute.isNominal() && !attribute.isString()) {
                        throw new IllegalArgumentException("Attribute neither nominal nor string: "
                                + stringValue);
                    }

                    int valIndex = attribute.indexOfValue(stringValue.toString());
                    if (valIndex == -1) {
                        if (attribute.isNominal()) {
                            throw new IllegalArgumentException(
                                    "Value not defined for given nominal attribute!");
                        }
                        else {
                            attribute.addStringValue(stringValue.toString());
                            valIndex = attribute.indexOfValue(stringValue.toString());
                        }
                    }
                    attributeValue = valIndex;
                }
                int offset = attributeStore.getAttributeOffset(attribute.name());

                if (offset != -1) {
                    featureValues[offset] = attributeValue;
                }
            }
            catch (NullPointerException e) {
                // ignore unseen attributes
            }
        }
        return featureValues;
    }

    /**
     * Evaluates a given single-label classifier on given train and test sets.
     * 
     * @param cl
     *            single-label classifier, needs to be trained beforehand
     * @param trainData
     * @param testData
     * @return
     * @throws Exception
     */
    public static Evaluation getEvaluationSinglelabel(Classifier cl, Instances trainData,
            Instances testData)
        throws Exception
    {
        Evaluation eval = new Evaluation(trainData);
        eval.evaluateModel(cl, testData);
        return eval;
    }

    /**
     * Evaluates a given multi-label classifier on given train and test sets.
     * 
     * @param cl
     *            multi-label classifier, needs not be trained beforehand
     * @param trainData
     * @param testData
     * @param threshold
     *            Meka threshold option
     * @return
     * @throws Exception
     */
    public static Result getEvaluationMultilabel(Classifier cl, Instances trainData,
            Instances testData, String threshold)
        throws Exception
    {
        Result r = meka.classifiers.multilabel.Evaluation.evaluateModel((MultilabelClassifier) cl,
                trainData, testData, threshold);
        return r;
    }

    /**
     * Generates an instances object containing the predictions of a given single-label classifier
     * for a given test set
     * 
     * @param testData
     *            test set
     * @param cl
     *            single-label classifier, needs to be trained beforehand, needs to be compatible
     *            with the test set trained classifier
     * @return instances object with additional attribute storing the predictions
     * @throws Exception
     */
    public static Instances getPredictionInstancesSingleLabel(Instances testData, Classifier cl)
        throws Exception
    {

        StringBuffer classVals = new StringBuffer();
        for (int i = 0; i < testData.classAttribute().numValues(); i++) {
            if (classVals.length() > 0) {
                classVals.append(",");
            }
            classVals.append(testData.classAttribute().value(i));
        }

        // get predictions
        List<Double> labelPredictionList = new ArrayList<Double>();
        for (int i = 0; i < testData.size(); i++) {
            labelPredictionList.add(cl.classifyInstance(testData.instance(i)));
        }

        // add an attribute with the predicted values at the end off the attributes
        Add filter = new Add();
        filter.setAttributeName(WekaTestTask.PREDICTION_CLASS_LABEL_NAME);
        if (classVals.length() > 0) {
            filter.setAttributeType(new SelectedTag(Attribute.NOMINAL, Add.TAGS_TYPE));
            filter.setNominalLabels(classVals.toString());
        }
        filter.setInputFormat(testData);
        testData = Filter.useFilter(testData, filter);

        // fill predicted values for each instance
        for (int i = 0; i < labelPredictionList.size(); i++) {
            testData.instance(i).setValue(testData.classIndex() + 1, labelPredictionList.get(i));
        }
        return testData;
    }

    /**
     * Generates an instances object containing the predictions of a given multi-label classifier
     * for a given test set
     * 
     * @param testData
     *            test set
     * @param cl
     *            multi-label classifier, needs not to be trained beforehand, needs to be compatible
     *            with the test set
     * @param thresholdArray
     *            an array of double, one for each label
     * @return instances object with additional attribute storing the predictions
     * @throws Exception
     */
    public static Instances getPredictionInstancesMultiLabel(Instances testData, Classifier cl,
            double[] thresholdArray)
        throws Exception
    {
        int numLabels = testData.classIndex();

        // get predictions
        List<double[]> labelPredictionList = new ArrayList<double[]>();
        for (int i = 0; i < testData.numInstances(); i++) {
            labelPredictionList.add(cl.distributionForInstance(testData.instance(i)));
        }

        // add attributes to store predictions in test data
        Add filter = new Add();
        for (int i = 0; i < numLabels; i++) {
            filter.setAttributeIndex(new Integer(numLabels + i + 1).toString());
            filter.setNominalLabels("0,1");
            filter.setAttributeName(testData.attribute(i).name() + "_"
                    + WekaTestTask.PREDICTION_CLASS_LABEL_NAME);
            filter.setInputFormat(testData);
            testData = Filter.useFilter(testData, filter);
        }

        // fill predicted values for each instance
        for (int i = 0; i < labelPredictionList.size(); i++) {
            for (int j = 0; j < labelPredictionList.get(i).length; j++) {
                testData.instance(i).setValue(j + numLabels,
                        labelPredictionList.get(i)[j] >= thresholdArray[j] ? 1. : 0.);
            }
        }
        return testData;
    }

    /**
     * Removes the instanceId attribute, iff present
     * 
     * @param data
     *            data set with or without instanceId attribute
     * @return the data set without instanceId attribute
     * @throws Exception
     */
    public static Instances removeInstanceId(Instances data, boolean multilabel)
        throws Exception
    {

        Instances filteredData;
        int classIndex = data.classIndex();

        if (data.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME) != null) {
            int instanceIdOffset = data.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME).index();

            Remove remove = new Remove();
            remove.setAttributeIndices(Integer.toString(instanceIdOffset + 1));
            remove.setInvertSelection(false);
            remove.setInputFormat(data);
            filteredData = Filter.useFilter(data, remove);
        }
        else {
            filteredData = new Instances(data);
        }
        // make sure the class index gets retained in multi-label
        if (multilabel) {
            filteredData.setClassIndex(classIndex);
        }
        return filteredData;
    }

    /**
     * Copies the instanceId attribute and its values from an existing data set, iff present. It will
     * be indexed right before the class attribute
     * 
     * @param newData
     *            data set without instanceId attribute
     * @param oldData
     *            data set with or without instanceId attribute
     * @return a data set with or without instanceId attribute
     * @throws Exception
     */
    public static Instances addInstanceId(Instances newData, Instances oldData, boolean isMultilabel)
        throws Exception
    {
        Instances filteredData;

        if (oldData.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME) != null) {
            int instanceIdOffset = oldData.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME).index();

            Add add = new Add();
            add.setAttributeName(AddIdFeatureExtractor.ID_FEATURE_NAME);
            // for multi-label setups, id attribute goes to the end of the header, and vice verse
            // for single-label
            if (isMultilabel) {
                add.setAttributeIndex("last");
            }
            else {
                add.setAttributeIndex("first");
            }
            add.setAttributeType(new SelectedTag(Attribute.STRING, Add.TAGS_TYPE));
            add.setInputFormat(newData);
            filteredData = Filter.useFilter(newData, add);
            int j = isMultilabel ? filteredData.numAttributes() - 1 : 0;
            for (int i = 0; i < filteredData.numInstances(); i++) {
                String outcomeId = oldData.instance(i).stringValue(instanceIdOffset);
                filteredData.instance(i).setValue(j, outcomeId);
            }
        }
        else {
            filteredData = new Instances(newData);
        }
        return filteredData;
    }
    

    /**
     * Read instances from uncompressed or compressed arff files. Compression is determined by
     * filename suffix. For bz2 files, it is expected that the first two bytes mark the compression
     * types (BZ) - thus, the first bytes of the stream are skipped. <br>
     * For arff files with single-label outcome, the class attribute is expected at the end of the
     * attribute set. For arff files with multi-label outcome, the class attribute is expected at
     * the beginning of the attribute set; additionally the number of class labels must be specified
     * in the relation tag behind a "-C" argument, e.g. "-C 3".
     * 
     * @param instancesFile
     *            arff File
     * @param multiLabel
     *            whether this arff file contains single- or multi-label outcome
     * @return instances with class attribute set
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Instances getInstances(File instancesFile, boolean multiLabel)
        throws FileNotFoundException, IOException
    {
        FileInputStream fis = new FileInputStream(instancesFile);
        BufferedInputStream bufStr = new BufferedInputStream(fis);

        InputStream underlyingStream = null;
        if (instancesFile.getName().endsWith(".gz")) {
            underlyingStream = new GZIPInputStream(bufStr);
        }
        else if (instancesFile.getName().endsWith(".bz2")) {
            // skip bzip2 prefix that we added manually
            fis.read();
            fis.read();
            underlyingStream = new CBZip2InputStream(bufStr);
        }
        else {
            underlyingStream = bufStr;
        }

        Reader reader = new InputStreamReader(underlyingStream, "UTF-8");
        Instances trainData = new Instances(reader);

        if (multiLabel) {
            String relationTag = trainData.relationName();
            // for multi-label classification, class labels are expected at beginning of attribute
            // set and their number must be specified with the -C parameter in the relation tag
            Matcher m = Pattern.compile("-C\\s\\d+").matcher(relationTag);
            m.find();
            trainData.setClassIndex(Integer.parseInt(m.group().split("-C ")[1]));
        }
        else {
            // for single-label classification, class label expected as last attribute
            trainData.setClassIndex(trainData.numAttributes() - 1);
        }
        reader.close();
        return trainData;
    }

    /**
     * @return The offset of the instanceId attribute within the weka instance
     */
    @SuppressWarnings("unchecked")
    public static int getInstanceIdAttributeOffset(Instances data)
    {
        int attOffset = 1;
        Enumeration<Attribute> enumeration = data.enumerateAttributes();
        while (enumeration.hasMoreElements()) {
            Attribute att = enumeration.nextElement();
            // System.out.println(att.name());
            if (att.name().equals(AddIdFeatureExtractor.ID_FEATURE_NAME)) {
                return attOffset;
            }
            attOffset++;
        }
        return -1;
    }

    /**
     * Returns a list with names of the class attribute values. Only works for single-label outcome.
     * 
     * @param eval
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<String> getClassLabels(Instances data, boolean isMultilabel)
    {
        List<String> classLabelList = new ArrayList<String>();
        if (!isMultilabel) {
            Enumeration<String> classLabels = data.classAttribute().enumerateValues();
            while (classLabels.hasMoreElements()) {
                classLabelList.add(classLabels.nextElement());
            }
        }
        else {
            int numLabels = data.classIndex();
            for (int i = 0; i < numLabels; i++) {
                classLabelList.add(data.attribute(i).name());
            }
        }
        return classLabelList;
    }

    /**
     * Calculates the threshold to turn a ranking of label predictions into a bipartition (one
     * threshold for each label)
     * 
     * @param threshold
     *            PCut1, PCutL, or number between 0 and 1 (see Meka documentation for details on
     *            this)
     * @param r
     *            Results file
     * @param data
     *            training data to use for automatically determining the threshold
     * @return an array with thresholds for each label
     * @throws Exception
     */
    public static double[] getMekaThreshold(String threshold, Result r, Instances data)
        throws Exception
    {
        double[] t = new double[r.L];
        if (threshold.equals("PCut1")) {
            // one threshold for all labels (PCut1 in Meka)
            Arrays.fill(
                    t,
                    ThresholdUtils.calibrateThreshold(r.predictions,
                            Double.valueOf(r.getValue("LCard_train"))));
        }
        else if (threshold.equals("PCutL")) {
            // one threshold for each label (PCutL in Meka)
            t = ThresholdUtils.calibrateThresholds(r.predictions,
                    MLUtils.labelCardinalities(data));
            // FIXME
            throw new Exception("Not yet implemented.");
        }
        else {
            // manual threshold
            Arrays.fill(t, Double.valueOf(threshold));
        }
        return t;
    }

    /**
     * 
     * Feature selection using Weka.
     * 
     * @param trainData
     *            training data
     * @param featureSearcher
     * @param attributeEvaluator
     * @return a feature selector
     * @throws Exception
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

    /**
     * Feature selection using Mulan.
     * 
     * @param trainData
     *            training data
     * @param labelTransformationMethod
     *            method to transform multi-label data into single-label data
     * @param attributeEvaluator
     * @param numLabelsToKeep
     *            number of labels that should be kept
     * @param featureSelectionResultsFile
     *            a file to write the evaluated attributes to
     * @return a filter to reduce the attribute dimension
     * @throws TextClassificationException
     */
    public static Remove multiLabelAttributeSelection(Instances trainData,
            String labelTransformationMethod, List<String> attributeEvaluator, int numLabelsToKeep,
            File featureSelectionResultsFile)
        throws TextClassificationException
    {
        Remove filterRemove = new Remove();
        try {
            MultiLabelInstances mulanInstances = convertMekaInstancesToMulanInstances(trainData);

            ASEvaluation eval = ASEvaluation.forName(attributeEvaluator.get(0), attributeEvaluator
                    .subList(1, attributeEvaluator.size()).toArray(new String[0]));

            AttributeEvaluator attributeSelectionFilter;

            // We currently only support the following Mulan Transformation methods (configuration
            // is complicated due to missing commandline support of mulan):
            if (labelTransformationMethod.equals("LabelPowersetAttributeEvaluator")) {
                attributeSelectionFilter = new LabelPowersetAttributeEvaluator(eval, mulanInstances);
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
                evalFile.append(att.name()
                        + ": "
                        + attributeSelectionFilter.evaluateAttribute(att.index()
                                - mulanInstances.getNumLabels()) + "\n");
            }
            FileUtils.writeStringToFile(featureSelectionResultsFile, evalFile.toString());

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
     * @return
     * @throws InvalidDataFormatException
     */
    public static MultiLabelInstances convertMekaInstancesToMulanInstances(Instances instances)
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
     * Applies a filter to reduce the dimension of attributes and reorders them to be used within
     * Meka
     * 
     * @param trainData
     * @param removeFilter
     * @return a dataset to be used with Meka
     * @throws Exception
     */
    public static Instances applyAttributeSelectionFilter(Instances trainData, Remove removeFilter)
        throws Exception
    {
        Instances filtered = Filter.useFilter(trainData, removeFilter);
        filtered.setClassIndex(trainData.classIndex());
        // swap attributes to fit MEKA
        MekaClassAttributes attFilter = new MekaClassAttributes();
        attFilter.setAttributeIndices(filtered.numAttributes() - trainData.classIndex() + 1
                + "-last");
        attFilter.setInputFormat(filtered);
        filtered = Filter.useFilter(filtered, attFilter);
        int newClassindex = filtered.classIndex();
        filtered.setRelationName(filtered.relationName().replaceAll("\\-C\\s[\\d]+",
                "-C " + newClassindex));

        return filtered;
    }
}
