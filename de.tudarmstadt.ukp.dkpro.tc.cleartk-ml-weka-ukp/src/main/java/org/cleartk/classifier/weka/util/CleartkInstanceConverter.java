package org.cleartk.classifier.weka.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.weka.ReplaceMissingValuesWithZeroFilter;
import org.cleartk.classifier.weka.WekaFeaturesEncoder;
import org.cleartk.classifier.weka.multilabel.DefaultMekaDataWriter;
import org.cleartk.classifier.weka.singlelabel.AbstractWekaDataWriter;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;

public class CleartkInstanceConverter
{

    public static weka.core.Instance toWekaInstance(Instance<String> instance,
            List<String> outcomeValues)
        throws Exception
    {
        WekaFeaturesEncoder encoder = new WekaFeaturesEncoder();
        encoder.encodeAll(instance.getFeatures());

        List<Feature> features = instance.getFeatures();
        Object outcome = instance.getOutcome();
        String outcomeString = null;
        if (outcome != null) {
            outcomeString = outcome.toString();
        }

        ArrayList<Attribute> attributes = encoder.getWekaAttributes();
        Map<String, Attribute> attributeMap = encoder.getWekaAttributeMap();

        // Make sure "outcome" is not the name of an attribute
        Attribute outcomeAttribute = createOutcomeAttribute(outcomeValues);
        if (attributeMap.containsKey("outcome")) {
            System.err
                    .println("A feature with name \"outcome\" was found. Renaming outcome attribute");
            outcomeAttribute = outcomeAttribute.copy(AbstractWekaDataWriter.classAttributePrefix
                    + "outcome");
        }
        attributes.add(outcomeAttribute);

        Map<Attribute, Integer> attIdxLookupMap = new HashMap<Attribute, Integer>(attributes.size());
        for (int idx = 0; idx < attributes.size(); idx++) {
            attIdxLookupMap.put(attributes.get(idx), idx);
        }

        Instances instances = new Instances("cleartk-generated", attributes, 1);
        instances.setClass(outcomeAttribute);

        // replace missing values with zero because zero is compressed in SparseInstance, missing
        // values are not
        ReplaceMissingValuesWithZeroFilter filter = new ReplaceMissingValuesWithZeroFilter();
        filter.setInputFormat(instances);

        double[] featureValues = new double[attributes.size()];

        for (Feature feature : features) {
            Attribute attribute = attributeMap.get(feature.getName());
            double attributeValue = getAttributeValue(feature, attributeMap, attribute);
            featureValues[attIdxLookupMap.get(attribute)] = attributeValue;
        }
        SparseInstance sparseInstance = new SparseInstance(1.0, featureValues);
        sparseInstance.setDataset(instances);
        if (outcomeString != null) {
            sparseInstance.setClassValue(outcomeString);
        }
        else {
            sparseInstance.setClassMissing();
        }

        filter.input(sparseInstance);
        return filter.output();
    }

    private static Attribute createOutcomeAttribute(List<String> outcomeValues)
    {
        Collections.sort(outcomeValues); // make the order of the attributes predictable
        return new Attribute("outcome", outcomeValues);
    }

    public static weka.core.Instance toMekaInstance(Instance<String[]> instance,
            List<String> allClassLabels)
        throws Exception
    {
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        List<Attribute> outcomeAttributes = new ArrayList<Attribute>(
                DefaultMekaDataWriter.createOutcomeAttributes(allClassLabels));
        // in Meka, class label attributes have to go on top
        attributes.addAll(outcomeAttributes);

        WekaFeaturesEncoder encoder = new WekaFeaturesEncoder();
        encoder.encodeAll(instance.getFeatures());

        List<Feature> features = instance.getFeatures();

        attributes.addAll(encoder.getWekaAttributes());
        Map<String, Attribute> attributeMap = encoder.getWekaAttributeMap();

        Map<Attribute, Integer> attIdxLookupMap = new HashMap<Attribute, Integer>(attributes.size());
        for (int idx = 0; idx < attributes.size(); idx++) {
            attIdxLookupMap.put(attributes.get(idx), idx);
        }

        // for Meka-internal use
        Instances instances = new Instances(DefaultMekaDataWriter.relationTag + ": -C "
                + outcomeAttributes.size() + " ", attributes, features.size());

        instances.setClassIndex(outcomeAttributes.size());

        // replace missing values with zero because zero is compressed in SparseInstance, missing
        // values are not
        ReplaceMissingValuesWithZeroFilter filter = new ReplaceMissingValuesWithZeroFilter();
        filter.setInputFormat(instances);

        double[] featureValues = new double[attributes.size()];

        for (Feature feature : features) {
            Attribute attribute = attributeMap.get(feature.getName());
            double attValue = getAttributeValue(feature, attributeMap, attribute);
            featureValues[attIdxLookupMap.get(attribute)] = attValue;
        }
        SparseInstance sparseInstance = new SparseInstance(1.0, featureValues);
        sparseInstance.setDataset(instances);

        filter.input(sparseInstance);
        return filter.output();
    }

    private static double getAttributeValue(Feature feature, Map<String, Attribute> attributeMap,
            Attribute attribute)
    {
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
        return attributeValue;
    }
}