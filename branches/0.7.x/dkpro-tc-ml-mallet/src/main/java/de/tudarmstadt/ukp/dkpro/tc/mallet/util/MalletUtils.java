/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.mallet.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MissingValue;
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.MalletReportConstants;
import de.tudarmstadt.ukp.dkpro.tc.mallet.task.MalletTestTask;
import de.tudarmstadt.ukp.dkpro.tc.mallet.writer.MalletFeatureEncoder;

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
                new FileOutputStream(outputFile)), "UTF-8"));
        HashMap<String, Integer> featureOffsetIndex = new HashMap<String, Integer>();
        for (int i = 0; i < instanceList.getNumberOfInstances(); i++) {
            Instance instance = instanceList.getInstance(i);
            for (Feature feature : instance.getFeatures()) {
                String featureName = feature.getName();
                if (!featureOffsetIndex.containsKey(featureName)) {
                    featureOffsetIndex.put(featureName, featureOffsetIndex.size());
                    bw.write(featureName + " ");
                }
            }
        }
        bw.write(MalletTestTask.OUTCOME_CLASS_LABEL_NAME);
        bw.close();
    }

    public static void writeFeatureValuesToFile(String featureValues[], String outcome,
            File outputFile)
        throws IOException
    {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(
                new FileOutputStream(outputFile, true)), "UTF-8"));
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
                new FileOutputStream(outputFile, true)), "UTF-8"));
        bw.write("\n");
        bw.flush();
        bw.close();
    }

    public static HashMap<String, Integer> getFeatureOffsetIndex(FeatureStore instanceList)
    {
        HashMap<String, Integer> featureOffsetIndex = new HashMap<String, Integer>();
        for (int i = 0; i < instanceList.getNumberOfInstances(); i++) {
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

    // public static String getInstanceSequenceId(Instance instance)
    // {
    // String instanceSequenceId;
    // String featureValue;
    // List<Feature> featList = instance.getFeatures();
    // for (Feature feature : featList) {
    // if (feature.getName().equals(AddIdFeatureExtractor.ID_FEATURE_NAME)) {
    // featureValue = feature.getValue().toString();
    // instanceSequenceId = featureValue.substring(0, featureValue.indexOf('_'));
    // return instanceSequenceId;
    // }
    // }
    // return null;
    // }

    // public static int getInstancePosition(Instance instance)
    // {
    // int instancePosition;
    // String featureValue;
    // List<Feature> featList = instance.getFeatures();
    // for (Feature feature : featList) {
    // if (feature.getName().equals(AddIdFeatureExtractor.ID_FEATURE_NAME)) {
    // featureValue = feature.getValue().toString();
    // instancePosition = Integer.parseInt(featureValue.substring(
    // featureValue.lastIndexOf('_') + 1, featureValue.length()));
    // return instancePosition;
    // }
    // }
    // return -1;
    // }

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

        for (int i = 0; i < instanceList.getNumberOfInstances(); i++) {
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

        // List<Instance> normalizedInstanceArrayList = instanceArrayList;
        // ArrayList<Instance> normalizedInstanceArrayList =
        // normalizeNumericFeatureValues(instanceArrayList);

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
                    featureValue = String.valueOf(doubleFeatureValue);
                }
                else if (value instanceof Boolean) {
                    doubleFeatureValue = (Boolean) value ? 1.0d : 0.0d;
                    featureValue = String.valueOf(doubleFeatureValue);
                }
                else if (value instanceof MissingValue) {
                    // missing value
                    featureValue = MalletFeatureEncoder.getMissingValueConversionMap().get(
                            ((MissingValue) value).getType());
                }
                else if (value == null) {
                    // null
                    throw new IllegalArgumentException(
                            "You have an instance which doesn't specify a value for the feature "
                                    + feature.getName());
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

    // public static ArrayList<Instance> normalizeNumericFeatureValues(
    // ArrayList<Instance> instanceArrayList)
    // {
    // ArrayList<Instance> normalizedInstanceArrayList = new ArrayList<Instance>();
    // double[] maxNumericFeatureValues = null;
    // double[] minNumericFeatureValues = null;
    // int featureIndex = 0;
    // for (int i = 0; i < instanceArrayList.size(); i++) {
    // Instance instance = instanceArrayList.get(i);
    // featureIndex = 0;
    // if (maxNumericFeatureValues == null || minNumericFeatureValues != null) {
    // maxNumericFeatureValues = new double[instance.getFeatures().size()];
    // minNumericFeatureValues = new double[instance.getFeatures().size()];
    // }
    // for (Feature feature : instance.getFeatures()) {
    // Object value = feature.getValue();
    // double doubleFeatureValue = 0.0;
    // if (value instanceof Number) {
    // doubleFeatureValue = ((Number) value).doubleValue();
    // if (doubleFeatureValue > maxNumericFeatureValues[featureIndex]) {
    // maxNumericFeatureValues[featureIndex] = doubleFeatureValue;
    // }
    // if (doubleFeatureValue < minNumericFeatureValues[featureIndex]) {
    // minNumericFeatureValues[featureIndex] = doubleFeatureValue;
    // }
    // featureIndex++;
    // }
    // }
    // }
    // for (int i = 0; i < instanceArrayList.size(); i++) {
    // Instance instance = instanceArrayList.get(i);
    // Instance normalizedInstance = instance;
    // List<Feature> normalizedFeatures = new ArrayList<Feature>();
    // double normalizedDoubleFeatureValues[] = new double[instance.getFeatures().size()];
    // featureIndex = 0;
    // for (Feature feature : instance.getFeatures()) {
    // Object value = feature.getValue();
    // double doubleFeatureValue = 0.0;
    // if (value instanceof Number) {
    // // normalize and add
    // doubleFeatureValue = ((Number) value).doubleValue();
    // if ((maxNumericFeatureValues[featureIndex] - minNumericFeatureValues[featureIndex]) != 0) {
    // normalizedDoubleFeatureValues[featureIndex] = (doubleFeatureValue -
    // minNumericFeatureValues[featureIndex])
    // / (maxNumericFeatureValues[featureIndex] - minNumericFeatureValues[featureIndex]);
    // }
    // else {
    // normalizedDoubleFeatureValues[featureIndex] = 0;
    // }
    // Feature normalizedFeature = new Feature(
    // normalizedDoubleFeatureValues[featureIndex]);
    // normalizedFeature.setName(feature.getName());
    // normalizedFeatures.add(normalizedFeature);
    // featureIndex++;
    // }
    // else {
    // // add without any modification
    // normalizedFeatures.add(feature);
    // }
    // }
    // normalizedInstance.setFeatures(normalizedFeatures);
    // normalizedInstanceArrayList.add(normalizedInstance);
    // }
    // return normalizedInstanceArrayList;
    // }
    

	public static CRF trainCRF(InstanceList training, CRF crf, double gaussianPriorVariance, int iterations, String defaultLabel,
			boolean fullyConnected, int[] orders) {

		if (crf == null) {
			crf = new CRF(training.getPipe(), (Pipe)null);
			String startName =
					crf.addOrderNStates(training, orders, null,
							defaultLabel, null, null,
							fullyConnected);
			for (int i = 0; i < crf.numStates(); i++) {
                crf.getState(i).setInitialWeight (Transducer.IMPOSSIBLE_WEIGHT);
            }
			crf.getState(startName).setInitialWeight(0.0);
		}
		//		logger.info("Training on " + training.size() + " instances");

		CRFTrainerByLabelLikelihood crft = new CRFTrainerByLabelLikelihood (crf);
		crft.setGaussianPriorVariance(gaussianPriorVariance);

		boolean converged;
		for (int i = 1; i <= iterations; i++) {
			converged = crft.train (training, 1);
			if (converged) {
                break;
            }
		}
		return crf;
	}

	public static void runTrainCRF(File trainingFile, File modelFile, double var, int iterations, String defaultLabel,
			boolean fullyConnected, int[] orders, boolean denseFeatureValues) throws FileNotFoundException, IOException, ClassNotFoundException {
		Reader trainingFileReader = null;
		InstanceList trainingData = null;
		//trainingFileReader = new FileReader(trainingFile);
		trainingFileReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(trainingFile)), "UTF-8");
		Pipe p = null;
		CRF crf = null;
		p = new ConversionToFeatureVectorSequence(denseFeatureValues); //uses first line of file to identify DKProInstanceID feature and discard
		p.getTargetAlphabet().lookupIndex(defaultLabel);
		p.setTargetProcessing(true);
		trainingData = new InstanceList(p);
		trainingData.addThruPipe(new LineGroupIterator(trainingFileReader,
				Pattern.compile("^\\s*$"), true)); //if you want to skip the line containing feature names, add "|^[A-Za-z]+.*$"
		//		logger.info
		//		("Number of features in training data: "+p.getDataAlphabet().size());

		//		logger.info ("Number of predicates: "+p.getDataAlphabet().size());


		if (p.isTargetProcessing())
		{
			Alphabet targets = p.getTargetAlphabet();
			StringBuffer buf = new StringBuffer("Labels:");
			for (int i = 0; i < targets.size(); i++)
             {
                buf.append(" ").append(targets.lookupObject(i).toString());
			//			logger.info(buf.toString());
            }
		}

		crf = trainCRF(trainingData, crf, var, iterations, defaultLabel, fullyConnected, orders);

		ObjectOutputStream s =
				new ObjectOutputStream(new FileOutputStream(modelFile));
		s.writeObject(crf);
		s.close();
	}

	public static void test(TransducerTrainer tt, TransducerEvaluator eval,
			InstanceList testing)
	{
		eval.evaluateInstanceList(tt, testing, "Testing");
	}

	public static TransducerEvaluator runTestCRF(File testFile, File modelFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		Reader testFileReader = null;
		InstanceList testData = null;
		//testFileReader = new FileReader(testFile);
		testFileReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(testFile)), "UTF-8");
		Pipe p = null;
		CRF crf = null;
		TransducerEvaluator eval = null;
		ObjectInputStream s =
				new ObjectInputStream(new FileInputStream(modelFile));
		crf = (CRF) s.readObject();
		s.close();
		p = crf.getInputPipe();
		p.setTargetProcessing(true);
		testData = new InstanceList(p);
		testData.addThruPipe(
				new LineGroupIterator(testFileReader,
						Pattern.compile("^\\s*$"), true));
		//	logger.info ("Number of predicates: "+p.getDataAlphabet().size());

		eval = new PerClassEvaluator(new InstanceList[] {testData}, new String[] {"Testing"});

		if (p.isTargetProcessing())
		{
			Alphabet targets = p.getTargetAlphabet();
			StringBuffer buf = new StringBuffer("Labels:");
			for (int i = 0; i < targets.size(); i++)
             {
                buf.append(" ").append(targets.lookupObject(i).toString());
			//			logger.info(buf.toString());
            }
		}
		
		test(new NoopTransducerTrainer(crf), eval, testData);
		
		List<String> labels = ((PerClassEvaluator) eval).getLabelNames();
		List<Double> precisionValues = ((PerClassEvaluator) eval).getPrecisionValues();
		List<Double> recallValues = ((PerClassEvaluator) eval).getRecallValues();
		List<Double> f1Values = ((PerClassEvaluator) eval).getF1Values();
		
		printEvaluationMeasures(labels, precisionValues, recallValues, f1Values);
		
		return eval;
	}

	public static TransducerEvaluator runTrainTest(File trainFile, File testFile, File modelFile,
			double var, int iterations, String defaultLabel,
			boolean fullyConnected, int[] orders, String tagger, boolean denseFeatureValues) throws FileNotFoundException, ClassNotFoundException, IOException, TextClassificationException {
		TransducerEvaluator eval = null;
		if (tagger.equals("CRF")) {
			runTrainCRF(trainFile,modelFile, var, iterations, defaultLabel, fullyConnected, orders, denseFeatureValues);
			eval = runTestCRF(testFile, modelFile);
		}
		else if (tagger.equals("HMM")){
			throw new TextClassificationException("'HMM' is not currently supported.");
			//runTrainHMM(trainFile,modelFile, defaultLabel, iterations, denseFeatureValues);
			//eval = runTestHMM(testFile, modelFile);
		}
		else {
			throw new TextClassificationException("Unsupported tagger name for sequence tagging. Supported taggers are 'CRF' and 'HMM'.");
		}
		return eval;
	}

	//FIXME HMM is not currently supported (uncomment and use a different vector sequence compatible to HMM utilities
	//in Mallet) @author krishperumal11

//	public static void runTrainHMM(File trainingFile, File modelFile, String defaultLabel, int iterations, boolean denseFeatureValues) throws FileNotFoundException, IOException {
//		Reader trainingFileReader = null;
//		InstanceList trainingData = null;
//		//trainingFileReader = new FileReader(trainingFile);
//		trainingFileReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(trainingFile)));
//		Pipe p = null;
//		p = new ConversionToFeatureVectorSequence(denseFeatureValues); //uses first line of file to identify DKProInstanceID feature and discard
//		p.getTargetAlphabet().lookupIndex(defaultLabel);
//		p.setTargetProcessing(true);
//		trainingData = new InstanceList(p);
//		trainingData.addThruPipe(new LineGroupIterator(trainingFileReader,
//				Pattern.compile("^\\s*$"), true)); //if you want to skip the line containing feature names, add "|^[A-Za-z]+.*$"
//		//		logger.info
//		//		("Number of features in training data: "+p.getDataAlphabet().size());
//
//		//		logger.info ("Number of predicates: "+p.getDataAlphabet().size());
//
//		if (p.isTargetProcessing())
//		{
//			Alphabet targets = p.getTargetAlphabet();
//			StringBuffer buf = new StringBuffer("Labels:");
//			for (int i = 0; i < targets.size(); i++)
//				buf.append(" ").append(targets.lookupObject(i).toString());
//			//			logger.info(buf.toString());
//		}
//
//		HMM hmm = null;
//		hmm = trainHMM(trainingData, hmm, iterations);
//		ObjectOutputStream s =
//				new ObjectOutputStream(new FileOutputStream(modelFile));
//		s.writeObject(hmm);
//		s.close();
//	}
//
//	public static HMM trainHMM(InstanceList training, HMM hmm, int numIterations) throws IOException {
//		if (hmm == null) {
//			hmm = new HMM(training.getPipe(), null);
//			hmm.addStatesForLabelsConnectedAsIn(training);
//			//hmm.addStatesForBiLabelsConnectedAsIn(trainingInstances);
//
//			HMMTrainerByLikelihood trainer =
//					new HMMTrainerByLikelihood(hmm);
//
//			trainer.train(training, numIterations);
//
//			//trainingEvaluator.evaluate(trainer);
//		}
//		return hmm;
//	}
//
//	public static TransducerEvaluator runTestHMM(File testFile, File modelFile) throws FileNotFoundException, IOException, ClassNotFoundException {
//		ArrayList<Pipe> pipes = new ArrayList<Pipe>();
//
//		pipes.add(new SimpleTaggerSentence2TokenSequence());
//		pipes.add(new TokenSequence2FeatureSequence());
//
//		Pipe pipe = new SerialPipes(pipes);
//
//		InstanceList testData = new InstanceList(pipe);
//
//		testData.addThruPipe(new LineGroupIterator(new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(testFile)))), Pattern.compile("^\\s*$"), true));
//
//		TransducerEvaluator eval =
//				new PerClassEvaluator(testData, "testing");
//
//		ObjectInputStream s =
//				new ObjectInputStream(new FileInputStream(modelFile));
//		HMM hmm = (HMM) s.readObject();
//
//		test(new NoopTransducerTrainer(hmm), eval, testData);
//		labels = ((PerClassEvaluator) eval).getLabelNames();
//		precisionValues = ((PerClassEvaluator) eval).getPrecisionValues();
//		recallValues = ((PerClassEvaluator) eval).getRecallValues();
//		f1Values = ((PerClassEvaluator) eval).getF1Values();
//		return eval;
//	}


	public static void printEvaluationMeasures(List<String> labels, List<Double> precisionValues, List<Double> recallValues, List<Double> f1Values) {
		double values[][] = new double[labels.size()][3];
		Iterator<Double> itPrecision = precisionValues.iterator();
		Iterator<Double> itRecall = recallValues.iterator();
		Iterator<Double> itF1 = f1Values.iterator();
		int i = 0;
		while(itPrecision.hasNext()) {
			values[i++][0] = itPrecision.next();
		}
		i = 0;
		while(itRecall.hasNext()) {
			values[i++][1] = itRecall.next();
		}
		i = 0;
		while(itF1.hasNext()) {
			values[i++][2] = itF1.next();
		}
		Iterator<String> itLabels = labels.iterator();
		for(i=0; i<values.length; i++) {
			System.out.println("--" + itLabels.next() + "--");
			System.out.println("Precision: " + values[i][0]);
			System.out.println("Recall: " + values[i][1]);
			System.out.println("F1: " + values[i][2]);
		}
	}

	public static void outputPredictions(TransducerEvaluator eval, File fileTest, File filePredictions,
			String predictionClassLabelName) throws IOException {
		ArrayList<String> predictedLabels = ((PerClassEvaluator) eval).getPredictedLabels();
		BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileTest)), "UTF-8"));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filePredictions)), "UTF-8"));
		String line;
		boolean header = false;
		int i = 0;
		while ((line = br.readLine()) != null) {
			if (!header) {
				bw.write(line + " " + predictionClassLabelName);
				bw.flush();
				header = true;
				continue;
			}
			if (!line.isEmpty()) {
				bw.write("\n" + line + " " + predictedLabels.get(i++));
				bw.flush();
			}
			else {
				bw.write("\n");
				bw.flush();
			}
		}
		br.close();
		bw.close();
	}

	public static void outputEvaluation(TransducerEvaluator eval, File fileEvaluation) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileEvaluation), "UTF-8"));

		ArrayList<String> labelNames = ((PerClassEvaluator) eval).getLabelNames();

		ArrayList<Double> precisionValues = ((PerClassEvaluator) eval).getPrecisionValues();
		ArrayList<Double> recallValues = ((PerClassEvaluator) eval).getRecallValues();
		ArrayList<Double> f1Values = ((PerClassEvaluator) eval).getF1Values();

		int numLabels = labelNames.size();
		bw.write("Measure,Value");
		bw.write("\n" + MalletReportConstants.CORRECT + "," + ((PerClassEvaluator) eval).getNumberOfCorrectPredictions());
		bw.write("\n" + MalletReportConstants.INCORRECT + "," + ((PerClassEvaluator) eval).getNumberOfIncorrectPredictions());
		bw.write("\n" + MalletReportConstants.NUMBER_EXAMPLES + "," + ((PerClassEvaluator) eval).getNumberOfExamples());
		bw.write("\n" + MalletReportConstants.PCT_CORRECT + "," + ((PerClassEvaluator) eval).getPercentageOfCorrectPredictions());
		bw.write("\n" + MalletReportConstants.PCT_INCORRECT + "," + ((PerClassEvaluator) eval).getPercentageOfIncorrectPredictions());

		for (int i = 0; i < numLabels; i++) {
			String label = labelNames.get(i);
			bw.write("\n" + MalletReportConstants.PRECISION + "_" + label + "," + precisionValues.get(i));
			bw.write("\n" + MalletReportConstants.RECALL  + "_" + label + "," + recallValues.get(i));
			bw.write("\n" + MalletReportConstants.FMEASURE + "_" + label + "," + f1Values.get(i));
			bw.flush();
		}
		bw.write("\n" + MalletReportConstants.MACRO_AVERAGE_FMEASURE + "," + ((PerClassEvaluator) eval).getMacroAverage());
		bw.flush();
		bw.close();
	}

	public static void outputConfusionMatrix(TransducerEvaluator eval, File fileConfusionMatrix) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileConfusionMatrix), "UTF-8"));

		ArrayList<String> labelNames = ((PerClassEvaluator) eval).getLabelNames();
		int numLabels = labelNames.size();

		HashMap<String, Integer> labelNameToIndexMap = new HashMap<String, Integer>();
		for (int i = 0; i < numLabels; i++) {
			labelNameToIndexMap.put(labelNames.get(i), i);
		}

		ArrayList<String> goldLabels = ((PerClassEvaluator) eval).getGoldLabels();
		ArrayList<String> predictedLabels = ((PerClassEvaluator) eval).getPredictedLabels();

		Integer[][] confusionMatrix = new Integer[numLabels][numLabels];

		//initialize to 0
		for (int i = 0; i < confusionMatrix.length; i++) {
			for (int j = 0; j < confusionMatrix.length; j++) {
				confusionMatrix[i][j] = 0;
			}
		}

		for (int i = 0; i < goldLabels.size(); i++) {
			confusionMatrix[labelNameToIndexMap.get(goldLabels.get(i))][labelNameToIndexMap.get(predictedLabels.get(i))]++;
		}

		String[][] confusionMatrixString = new String[numLabels + 1][numLabels + 1];
		confusionMatrixString[0][0] = " ";
		for (int i = 1; i < numLabels + 1; i++) {
			confusionMatrixString[i][0] = labelNames.get(i-1) + "_actual";
			confusionMatrixString[0][i] = labelNames.get(i-1) + "_predicted";
		}
		for (int i = 1; i < numLabels + 1; i++) {
			for (int j = 1; j < numLabels + 1; j++) {
				confusionMatrixString[i][j] = confusionMatrix[i-1][j-1].toString();
			}
		}

		for (String[] element : confusionMatrixString) {
			bw.write(StringUtils.join(element, ","));
			bw.write("\n");
			bw.flush();
		}
		bw.close();
	}
}
