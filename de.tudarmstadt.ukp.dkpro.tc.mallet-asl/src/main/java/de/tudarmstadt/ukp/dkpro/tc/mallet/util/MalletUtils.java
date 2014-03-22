package de.tudarmstadt.ukp.dkpro.tc.mallet.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.mallet.task.TestTask;

/**
 * Utility class for the Mallet machine learning toolkit
 * 
 * @author Krish Perumal
 * 
 */
public class MalletUtils
{

    // TODO yet to decide when to call this method
    public static void writeFeatureNamesToFile(FeatureStore instanceList, File outputFile)
        throws IOException
    {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(
                new FileOutputStream(outputFile))));
        HashMap<String, Integer> featureOffsetIndex = new HashMap<String, Integer>();
        for (int i = 0; i < instanceList.size(); i++) {
            Instance instance = instanceList.getInstance(i);
            for (Feature feature : instance.getFeatures()) {
                String featureName = feature.getName();
                if (!featureOffsetIndex.containsKey(featureName)) {
                    featureOffsetIndex.put(featureName, featureOffsetIndex.size());
                    bw.write(featureName + " ");
                }
            }
        }
        bw.write(TestTask.OUTCOME_CLASS_LABEL_NAME);
        bw.close();
    }

    public static void writeFeatureValuesToFile(String featureValues[], String outcome,
            File outputFile)
        throws IOException
    {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(
                new FileOutputStream(outputFile, true))));
        bw.write("\n");
        for (String featureValue : featureValues) {
            bw.write(featureValue + " ");
        }
        bw.write(outcome);
        bw.flush();
        bw.close();
    }

    public static void writeNewLineToFile(File outputFile)
        throws IOException
    {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(
                new FileOutputStream(outputFile, true))));
        bw.write("\n");
        bw.flush();
        bw.close();
    }

    public static HashMap<String, Integer> getFeatureOffsetIndex(FeatureStore instanceList)
    {
        HashMap<String, Integer> featureOffsetIndex = new HashMap<String, Integer>();
        for (int i = 0; i < instanceList.size(); i++) {
            Instance instance = instanceList.getInstance(i);
            for (Feature feature : instance.getFeatures()) {
                String featureName = feature.getName();
                if (!featureOffsetIndex.containsKey(featureName)) {
                    featureOffsetIndex.put(featureName, featureOffsetIndex.size());
                }
            }

        }
        return featureOffsetIndex;
    }

//    public static String getInstanceSequenceId(Instance instance)
//    {
//        String instanceSequenceId;
//        String featureValue;
//        List<Feature> featList = instance.getFeatures();
//        for (Feature feature : featList) {
//            if (feature.getName().equals(AddIdFeatureExtractor.ID_FEATURE_NAME)) {
//                featureValue = feature.getValue().toString();
//                instanceSequenceId = featureValue.substring(0, featureValue.indexOf('_'));
//                return instanceSequenceId;
//            }
//        }
//        return null;
//    }

//    public static int getInstancePosition(Instance instance)
//    {
//        int instancePosition;
//        String featureValue;
//        List<Feature> featList = instance.getFeatures();
//        for (Feature feature : featList) {
//            if (feature.getName().equals(AddIdFeatureExtractor.ID_FEATURE_NAME)) {
//                featureValue = feature.getValue().toString();
//                instancePosition = Integer.parseInt(featureValue.substring(
//                        featureValue.lastIndexOf('_') + 1, featureValue.length()));
//                return instancePosition;
//            }
//        }
//        return -1;
//    }

    public static void instanceListToMalletFormatFile(File outputFile, FeatureStore instanceList,
            boolean useDenseInstances)
        throws Exception
    {
        // check for error conditions
        if (instanceList.getUniqueOutcomes().isEmpty()) {
            throw new IllegalArgumentException("List of instance outcomes is empty.");
        }

        Map<String, Integer> featureOffsetIndex = getFeatureOffsetIndex(instanceList);

        writeFeatureNamesToFile(instanceList, outputFile);

        List<Instance> instanceArrayList = new ArrayList<Instance>();

        for (int i = 0; i < instanceList.size(); i++) {
            instanceArrayList.add(instanceList.getInstance(i));
        }

        // group based on instance sequence and sort based on instance position in file
        Collections.sort(instanceArrayList, new Comparator<Instance>()
        {
            @Override
            public int compare(Instance o1, Instance o2)
            {
                int instanceSequenceId1 = o1.getSequenceId();
                int instanceSequenceId2 = o2.getSequenceId();
                int instancePosition1 = o1.getSequencePosition();
                int instancePosition2 = o2.getSequencePosition();

                if (instanceSequenceId1 == instanceSequenceId2) {
                    if (instancePosition1 == instancePosition2) {
                        return 0;
                    }
                    return instancePosition1 < instancePosition2 ? -1 : 1;
                }
                
                return 0;
                // order of sequences doesn't matter
                // order of instances within a sequence does
            }
        });

//        List<Instance> normalizedInstanceArrayList = instanceArrayList;
//        ArrayList<Instance> normalizedInstanceArrayList =
//        normalizeNumericFeatureValues(instanceArrayList);

        int currentSequenceId = 1;
        for (int i = 0; i < instanceArrayList.size(); i++) {
            Instance instance = instanceArrayList.get(i);
            if (currentSequenceId != instance.getSequenceId()) {
                writeNewLineToFile(outputFile);
                currentSequenceId = instance.getSequenceId();
            }
            
            String outcome = instance.getOutcome();
            String featureValues[] = new String[featureOffsetIndex.size()];
            for (Feature feature : instance.getFeatures()) {
                String featureName = feature.getName();
                Object value = feature.getValue();
                double doubleFeatureValue = 0.0;
                String featureValue;
                if (value instanceof Number) {
                    doubleFeatureValue = ((Number) value).doubleValue();
                    featureValue = doubleFeatureValue + "";
                }
                else if (value instanceof Boolean) {
                    doubleFeatureValue = (Boolean) value ? 1.0d : 0.0d;
                    featureValue = doubleFeatureValue + "";
                }
                else {
                    featureValue = value.toString();
                }
                if (featureOffsetIndex.containsKey(featureName)) {
                    featureValues[featureOffsetIndex.get(featureName)] = featureValue;
                }
            }
            writeFeatureValuesToFile(featureValues, outcome, outputFile);
        }
    }

//    public static ArrayList<Instance> normalizeNumericFeatureValues(
//            ArrayList<Instance> instanceArrayList)
//    {
//        ArrayList<Instance> normalizedInstanceArrayList = new ArrayList<Instance>();
//        double[] maxNumericFeatureValues = null;
//        double[] minNumericFeatureValues = null;
//        int featureIndex = 0;
//        for (int i = 0; i < instanceArrayList.size(); i++) {
//            Instance instance = instanceArrayList.get(i);
//            featureIndex = 0;
//            if (maxNumericFeatureValues == null || minNumericFeatureValues != null) {
//                maxNumericFeatureValues = new double[instance.getFeatures().size()];
//                minNumericFeatureValues = new double[instance.getFeatures().size()];
//            }
//            for (Feature feature : instance.getFeatures()) {
//                Object value = feature.getValue();
//                double doubleFeatureValue = 0.0;
//                if (value instanceof Number) {
//                    doubleFeatureValue = ((Number) value).doubleValue();
//                    if (doubleFeatureValue > maxNumericFeatureValues[featureIndex]) {
//                        maxNumericFeatureValues[featureIndex] = doubleFeatureValue;
//                    }
//                    if (doubleFeatureValue < minNumericFeatureValues[featureIndex]) {
//                        minNumericFeatureValues[featureIndex] = doubleFeatureValue;
//                    }
//                    featureIndex++;
//                }
//            }
//        }
//        for (int i = 0; i < instanceArrayList.size(); i++) {
//            Instance instance = instanceArrayList.get(i);
//            Instance normalizedInstance = instance;
//            List<Feature> normalizedFeatures = new ArrayList<Feature>();
//            double normalizedDoubleFeatureValues[] = new double[instance.getFeatures().size()];
//            featureIndex = 0;
//            for (Feature feature : instance.getFeatures()) {
//                Object value = feature.getValue();
//                double doubleFeatureValue = 0.0;
//                if (value instanceof Number) {
//                    // normalize and add
//                    doubleFeatureValue = ((Number) value).doubleValue();
//                    if ((maxNumericFeatureValues[featureIndex] - minNumericFeatureValues[featureIndex]) != 0) {
//                        normalizedDoubleFeatureValues[featureIndex] = (doubleFeatureValue - minNumericFeatureValues[featureIndex])
//                                / (maxNumericFeatureValues[featureIndex] - minNumericFeatureValues[featureIndex]);
//                    }
//                    else {
//                        normalizedDoubleFeatureValues[featureIndex] = 0;
//                    }
//                    Feature normalizedFeature = new Feature(
//                            normalizedDoubleFeatureValues[featureIndex]);
//                    normalizedFeature.setName(feature.getName());
//                    normalizedFeatures.add(normalizedFeature);
//                    featureIndex++;
//                }
//                else {
//                    // add without any modification
//                    normalizedFeatures.add(feature);
//                }
//            }
//            normalizedInstance.setFeatures(normalizedFeatures);
//            normalizedInstanceArrayList.add(normalizedInstance);
//        }
//        return normalizedInstanceArrayList;
//    }
}
