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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import meka.classifiers.multilabel.MultilabelClassifier;
import meka.core.Result;

import org.apache.commons.lang.StringUtils;

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
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue;
import de.tudarmstadt.ukp.dkpro.tc.weka.AttributeStore;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;
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
        filter.setAttributeName(TestTask.PREDICTION_CLASS_LABEL_NAME);
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
                    + TestTask.PREDICTION_CLASS_LABEL_NAME);
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
     * Removes the OutcomeId attribute, iff present
     * 
     * @param data
     *            data set with or without OutcomeId attribute
     * @return the data set without OutcomeId attribute
     * @throws Exception
     */
    public static Instances removeOutcomeId(Instances data, boolean multilabel)
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
     * Copies the OutcomeId attribute and its values from an existing data set, iff present. It will
     * be indexed right before the class attribute
     * 
     * @param newData
     *            data set without OutcomeId attribute
     * @param oldData
     *            data set with or without OutcomeId attribute
     * @return a data set with or without OutcomeId attribute
     * @throws Exception
     */
    public static Instances addOutcomeId(Instances newData, Instances oldData, boolean isMultilabel)
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
}
