package de.tudarmstadt.ukp.dkpro.tc.mallet.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.HMM;
import cc.mallet.fst.HMMTrainerByLikelihood;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.ReportConstants;

public class TaskUtils {

	public static ArrayList<Double> precisionValues;

	public static ArrayList<Double> recallValues;

	public static ArrayList<Double> f1Values;

	public static ArrayList<String> labels;

	public static ArrayList<String> predictions;

	public static CRF trainCRF(InstanceList training, CRF crf, double gaussianPriorVariance, int iterations, String defaultLabel,
			boolean fullyConnected, int[] orders) {

		if (crf == null) {
			crf = new CRF(training.getPipe(), (Pipe)null);
			String startName =
					crf.addOrderNStates(training, orders, null,
							defaultLabel, null, null,
							fullyConnected);
			for (int i = 0; i < crf.numStates(); i++)
				crf.getState(i).setInitialWeight (Transducer.IMPOSSIBLE_WEIGHT);
			crf.getState(startName).setInitialWeight(0.0);
		}
		//		logger.info("Training on " + training.size() + " instances");

		CRFTrainerByLabelLikelihood crft = new CRFTrainerByLabelLikelihood (crf);
		crft.setGaussianPriorVariance(gaussianPriorVariance);

		boolean converged;
		for (int i = 1; i <= iterations; i++) {
			converged = crft.train (training, 1);
			if (converged)
				break;
		}
		return crf;
	}

	public static void runTrainCRF(File trainingFile, File modelFile, double var, int iterations, String defaultLabel,
			boolean fullyConnected, int[] orders, boolean denseFeatureValues) throws FileNotFoundException, IOException, ClassNotFoundException {
		Reader trainingFileReader = null;
		InstanceList trainingData = null;
		//trainingFileReader = new FileReader(trainingFile);
		trainingFileReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(trainingFile)));
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
				buf.append(" ").append(targets.lookupObject(i).toString());
			//			logger.info(buf.toString());
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
		testFileReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(testFile)));
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
				buf.append(" ").append(targets.lookupObject(i).toString());
			//			logger.info(buf.toString());
		}
		test(new NoopTransducerTrainer(crf), eval, testData);
		labels = ((PerClassEvaluator) eval).getLabelNames();
		precisionValues = ((PerClassEvaluator) eval).getPrecisionValues();
		recallValues = ((PerClassEvaluator) eval).getRecallValues();
		f1Values = ((PerClassEvaluator) eval).getF1Values();
		return eval;
	}

	public static TransducerEvaluator runTrainTest(File trainFile, File testFile, File modelFile,
			double var, int iterations, String defaultLabel,
			boolean fullyConnected, int[] orders, String tagger, boolean denseFeatureValues) throws FileNotFoundException, ClassNotFoundException, IOException, TextClassificationException {
		TransducerEvaluator eval = null;
		if (tagger.equals("CRF")) {
			runTrainCRF(trainFile,modelFile, var, iterations, defaultLabel, fullyConnected, orders, denseFeatureValues);
			eval = runTestCRF(testFile, modelFile);
			printEvaluationMeasures();
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


	public static void printEvaluationMeasures() {
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
		BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileTest))));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filePredictions))));
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
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileEvaluation));

		ArrayList<String> labelNames = ((PerClassEvaluator) eval).getLabelNames();

		ArrayList<Double> precisionValues = ((PerClassEvaluator) eval).getPrecisionValues();
		ArrayList<Double> recallValues = ((PerClassEvaluator) eval).getRecallValues();
		ArrayList<Double> f1Values = ((PerClassEvaluator) eval).getF1Values();

		int numLabels = labelNames.size();
		bw.write("Measure,Value");
		bw.write("\n" + ReportConstants.CORRECT + "," + ((PerClassEvaluator) eval).getNumberOfCorrectPredictions());
		bw.write("\n" + ReportConstants.INCORRECT + "," + ((PerClassEvaluator) eval).getNumberOfIncorrectPredictions());
		bw.write("\n" + ReportConstants.NUMBER_EXAMPLES + "," + ((PerClassEvaluator) eval).getNumberOfExamples());
		bw.write("\n" + ReportConstants.PCT_CORRECT + "," + ((PerClassEvaluator) eval).getPercentageOfCorrectPredictions());
		bw.write("\n" + ReportConstants.PCT_INCORRECT + "," + ((PerClassEvaluator) eval).getPercentageOfIncorrectPredictions());

		for (int i = 0; i < numLabels; i++) {
			String label = labelNames.get(i);
			bw.write("\n" + ReportConstants.PRECISION + "_" + label + "," + precisionValues.get(i));
			bw.write("\n" + ReportConstants.RECALL  + "_" + label + "," + recallValues.get(i));
			bw.write("\n" + ReportConstants.FMEASURE + "_" + label + "," + f1Values.get(i));
			bw.flush();
		}
		bw.write("\n" + ReportConstants.MACRO_AVERAGE_FMEASURE + "," + ((PerClassEvaluator) eval).getMacroAverage());
		bw.flush();
		bw.close();
	}

	public static void outputConfusionMatrix(TransducerEvaluator eval, File fileConfusionMatrix) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileConfusionMatrix));

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

		for (int i = 0; i < confusionMatrixString.length; i++) {
			bw.write(StringUtils.join(confusionMatrixString[i], ","));
			bw.write("\n");
			bw.flush();
		}
		bw.close();
	}
}
