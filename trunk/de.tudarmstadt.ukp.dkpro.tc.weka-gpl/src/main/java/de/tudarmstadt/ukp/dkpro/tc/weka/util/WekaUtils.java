package de.tudarmstadt.ukp.dkpro.tc.weka.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.features.InstanceList;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.AttributeStore;
import de.tudarmstadt.ukp.dkpro.tc.weka.filter.ReplaceMissingValuesWithZeroFilter;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaFeatureEncoder;

public class WekaUtils
{
    public static final String classAttributePrefix = "__";
    public static final String classAttributeName = "outcome";
    public static final String relationName = "dkpro-tc-generated";
    public static final String COMPATIBLE_OUTCOME_CLASS = "_Comp";

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
                            testData.attribute(Constants.CLASS_ATTRIBUTE_NAME + COMPATIBLE_OUTCOME_CLASS), label);
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
            compTestData.setClass(compTestData.attribute(Constants.CLASS_ATTRIBUTE_NAME + COMPATIBLE_OUTCOME_CLASS));
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
                //numTestLabels +i (because index starts from 0)
                filter.setAttributeIndex(new Integer(numTestLabels + i +1 ).toString());
                filter.setNominalLabels("0,1");
                filter.setAttributeName(trainData.attribute(i).name() + COMPATIBLE_OUTCOME_CLASS);
                filter.setInputFormat(testData);
                testData = Filter.useFilter(testData, filter);
            }

            // fill NEW test data with values from old test data plus the new class attributes
            compTestData = new Instances(testData, testData.numInstances());
            for (int i = 0; i < testData.numInstances(); i++) {
                weka.core.Instance instance = testData.instance(i);
                //fullfill with 0.
                for (int j=0;j<numTrainLabels;j++){
                    instance.setValue(j + numTestLabels,0.);
                }
                //fill the real values:
                for (int j = 0; j < numTestLabels; j++) {
                    // part of train data
                    if (trainLabels.indexOf(instance.attribute(j).name()) != -1) {
                        // class label found in test data
                        int index=trainLabels.indexOf(instance.attribute(j).name());
                        instance.setValue(index + numTestLabels,instance.value(j));
                    }
                }
                compTestData.add(instance);
            }

            // remove old class attributes
            Remove remove = new Remove();
            for (int i = 0; i < numTestLabels; i++) {
                remove.setAttributeIndices(Integer.toString(1));
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
    
    public static void instanceListToArffFile(File outputFile, InstanceList instanceList)
        throws Exception
    {
        instanceListToArffFile(outputFile, instanceList, false);
    }

    public static void instanceListToArffFile(File outputFile, InstanceList instanceList, boolean useDenseInstances)
        throws Exception
    {
                    
        Filter preprocessingFilter = new ReplaceMissingValuesWithZeroFilter();
        
        AttributeStore attributeStore = WekaFeatureEncoder.getAttributeStore(instanceList);

        // Make sure "outcome" is not the name of an attribute
        Attribute outcomeAttribute = createOutcomeAttribute(instanceList.getUniqueOutcomes());
        if (attributeStore.containsAttributeName(classAttributeName)) {
            System.err.println("A feature with name \"outcome\" was found. Renaming outcome attribute");
            outcomeAttribute = outcomeAttribute.copy(classAttributePrefix + classAttributeName);
        }
        attributeStore.addAttribute(outcomeAttribute.name(), outcomeAttribute);

        Instances wekaInstances = new Instances(relationName, attributeStore.getAttributes(), instanceList.size());
        wekaInstances.setClass(outcomeAttribute);

        if (!outputFile.exists()) {
            outputFile.mkdirs();
            outputFile.createNewFile();
        }
        
        ArffSaver saver = new ArffSaver();
        preprocessingFilter.setInputFormat(wekaInstances);
        saver.setRetrieval(Saver.INCREMENTAL);
        saver.setFile(outputFile);
        saver.setCompressOutput(true);
        saver.setInstances(wekaInstances);

        for (int i = 0; i < instanceList.size(); i++) {
            Instance instance = instanceList.getInstance(i);

            double[] featureValues = new double[attributeStore.size()];

            for (Feature feature : instance.getFeatures()) {
                Attribute attribute = attributeStore.getAttribute(feature.getName());
                Object featureValue = feature.getValue();

                double attributeValue;
                if (featureValue instanceof Number) {
                    attributeValue = ((Number) feature.getValue()).doubleValue();
                }
                else if (featureValue instanceof Boolean) {
                    attributeValue = (Boolean) featureValue ? 1.0d : 0.0d;
                }
                else { // this branch is unsafe - the code is copied from SparseInstance (can it be
                       // done safer?)
                    Object stringValue = feature.getValue();
                    if (!attribute.isNominal() && !attribute.isString()) {
                        throw new IllegalArgumentException("Attribute neither nominal nor string!");
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
            
            weka.core.Instance wekaInstance;

            if (useDenseInstances) {
                wekaInstance = new DenseInstance(1.0, featureValues);
            }
            else {
                wekaInstance = new SparseInstance(1.0, featureValues);
            }

            wekaInstance.setDataset(wekaInstances);
            wekaInstance.setClassValue(instanceList.getOutcome(i));

            preprocessingFilter.input(wekaInstance);
            saver.writeIncremental(preprocessingFilter.output());        
        }

        // finishes the incremental saving process
        saver.writeIncremental(null);
    }
    
    public static void instanceListToArffFileMultiLabel(File outputFile, InstanceList instanceList, boolean useDenseInstances)
    {
// TODO TZ: ich hab hier schonmal den code der alten Version hinkopiert, das muss angepasst werden wie direkt oben drueber
        
//            ArrayList<Attribute> attributes = new ArrayList<Attribute>();
//            List<Attribute> outcomeAttributes = createOutcomeAttributes(new ArrayList<String>(
//                    this.outcomeValues));
//            // in Meka, class label attributes have to go on top
//            attributes.addAll(outcomeAttributes);
//
//            attributes.addAll(((WekaFeaturesEncoder) this.classifierBuilder.getFeaturesEncoder())
//                    .getWekaAttributes());
//            Map<String, Attribute> attributeMap = ((WekaFeaturesEncoder) this.classifierBuilder
//                    .getFeaturesEncoder()).getWekaAttributeMap();
//
//            Map<Attribute, Integer> attIdxLookupMap = new HashMap<Attribute, Integer>(attributes.size());
//            for (int idx = 0; idx < attributes.size(); idx++) {
//                attIdxLookupMap.put(attributes.get(idx), idx);
//            }
//
//            // for Meka-internal use
//            Instances instances = new Instances(relationTag + ": -C " + outcomeAttributes.size() + " ",
//                    attributes, instanceFeatures.size());
//            instances.setClassIndex(outcomeAttributes.size());
//
//            ArffSaver saver = new ArffSaver();
//            try {
//                preprocessingFilter.setInputFormat(instances);
//                saver.setRetrieval(Saver.INCREMENTAL);
//                saver.setFile(trainingDataFile);
//                saver.setInstances(instances);
//            }
//            catch (Exception e) {
//                throw new CleartkProcessingException(e);
//            }
//
//            for (int i = 0; i < instanceFeatures.size(); i++) {
//                Iterable<Feature> features = instanceFeatures.get(i);
//
//                double[] featureValues = new double[attributes.size()];
//
//                // set class label values
//                List<String> instanceOutcome = Arrays.asList(instanceOutcomes.get(i));
//                for (Attribute label : outcomeAttributes) {
//                    featureValues[attIdxLookupMap.get(label)] = instanceOutcome.contains(label.name()
//                            .substring(2)) ? 1.0d : 0.0d;
//                }
//
//                // set feature values
//                for (Feature feature : features) {
//                    Attribute attribute = attributeMap.get(feature.getName());
//                    Object featureValue = feature.getValue();
//
//                    double attributeValue;
//                    if (featureValue instanceof Number) {
//                        attributeValue = ((Number) feature.getValue()).doubleValue();
//                    }
//                    else if (featureValue instanceof Boolean) {
//                        attributeValue = (Boolean) featureValue ? 1.0d : 0.0d;
//                    }
//                    else { // this branch is unsafe - the code is copied from SparseInstance (can it be
//                           // done safer?)
//                        Object stringValue = feature.getValue();
//                        if (!attribute.isNominal() && !attribute.isString()) {
//                            throw new IllegalArgumentException("Attribute neither nominal nor string!");
//                        }
//                        int valIndex = attribute.indexOfValue(stringValue.toString());
//                        if (valIndex == -1) {
//                            if (attribute.isNominal()) {
//                                throw new IllegalArgumentException(
//                                        "Value not defined for given nominal attribute!");
//                            }
//                            else {
//                                attribute.addStringValue(stringValue.toString());
//                                valIndex = attribute.indexOfValue(stringValue.toString());
//                            }
//                        }
//                        attributeValue = valIndex;
//                    }
//                    featureValues[attIdxLookupMap.get(attribute)] = attributeValue;
//                }
//                Instance instance;
//
//                if (useDenseInstances) {
//                    instance = new DenseInstance(1.0, featureValues);
//                }
//                else {
//                    instance = new SparseInstance(1.0, featureValues);
//                }
//
//                instance.setDataset(instances);
//
//                try {
//                    preprocessingFilter.input(instance);
//                    saver.writeIncremental(preprocessingFilter.output());
//                }
//                catch (Exception e) {
//                    throw new CleartkProcessingException(e);
//                }
//            }
//
//            try {
//                // finishes the incremental saving process
//                saver.writeIncremental(null);
//            }
//            catch (IOException e) {
//                throw new CleartkProcessingException(e);
//            }
    }
    
    private static Attribute createOutcomeAttribute(List<String> outcomeValues)
    {
        // make the order of the attributes predictable
        Collections.sort(outcomeValues); 

        return new Attribute(classAttributeName, outcomeValues);
    }
}
