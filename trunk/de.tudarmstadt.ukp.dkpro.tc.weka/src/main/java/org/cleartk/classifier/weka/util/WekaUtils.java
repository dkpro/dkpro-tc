package org.cleartk.classifier.weka.util;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.cleartk.classifier.weka.ReplaceMissingValuesWithZeroFilter;
import org.cleartk.classifier.weka.test.AttributeStore;
import org.cleartk.classifier.weka.test.Feature;
import org.cleartk.classifier.weka.test.Instance;
import org.cleartk.classifier.weka.test.InstanceList;
import org.cleartk.classifier.weka.test.WekaFeatureEncoder;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import weka.filters.Filter;

public class WekaUtils
{
    public static final String classAttributePrefix = "__";
    public static final String classAttributeName = "outcome";
    public static final String relationName = "dkpro-tc-generated";

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
    
    private static Attribute createOutcomeAttribute(List<String> outcomeValues)
    {
        // make the order of the attributes predictable
        Collections.sort(outcomeValues); 

        return new Attribute(classAttributeName, outcomeValues);
    }
}
