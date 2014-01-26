package de.tudarmstadt.ukp.dkpro.tc.mallet.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

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
import de.tudarmstadt.ukp.dkpro.tc.mallet.util.SimpleTagger.SimpleTaggerSentence2FeatureVectorSequence;

public class TaskUtils {

	public static double var = 10.0; //Gaussian Prior Variance

	public static int iterations = 500; //Number of iterations

	public static String defaultLabel = "O";

	public static ArrayList<Double> precisionValues;

	public static ArrayList<Double> recallValues;

	public static ArrayList<Double> f1Values;

	public static ArrayList<String> labels;

	public static CRF train(InstanceList training, CRF crf) {

		if (crf == null) {
			crf = new CRF(training.getPipe(), (Pipe)null);
			String startName =
					crf.addOrderNStates(training, null, null,
							defaultLabel, null, null,
							false);
			for (int i = 0; i < crf.numStates(); i++)
				crf.getState(i).setInitialWeight (Transducer.IMPOSSIBLE_WEIGHT);
			crf.getState(startName).setInitialWeight(0.0);
		}
		//		logger.info("Training on " + training.size() + " instances");

		CRFTrainerByLabelLikelihood crft = new CRFTrainerByLabelLikelihood (crf);
		crft.setGaussianPriorVariance(var);

		boolean converged;
		for (int i = 1; i <= iterations; i++) {
			converged = crft.train (training, 1);
			if (converged)
				break;
		}
		return crf;
	}

	public static void runTrain(File trainingFile, File modelFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		Reader trainingFileReader = null;
		InstanceList trainingData = null;
		trainingFileReader = new FileReader(trainingFile);
		Pipe p = null;
		CRF crf = null;
		p = new SimpleTaggerSentence2FeatureVectorSequence();
		p.getTargetAlphabet().lookupIndex(defaultLabel);
		p.setTargetProcessing(true);
		trainingData = new InstanceList(p);
		trainingData.addThruPipe(new LineGroupIterator(trainingFileReader,
				Pattern.compile("^\\s*$"), true));
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

		crf = train(trainingData, crf);

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

	public static void runTest(File testFile, File modelFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		Reader testFileReader = null;
		InstanceList testData = null;
		testFileReader = new FileReader(testFile);
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
		labels = ((PerClassEvaluator) eval).getLabels();
		precisionValues = ((PerClassEvaluator) eval).getPrecisionValues();
		recallValues = ((PerClassEvaluator) eval).getRecallValues();
		f1Values = ((PerClassEvaluator) eval).getF1Values();
	}

	public static void runTrainTest(File trainFile, File testFile, File modelFile) throws FileNotFoundException, ClassNotFoundException, IOException {
		runTrain(trainFile,modelFile);
		runTest(testFile,modelFile);
		printEvaluationMeasures();
	}

	public static void runCrossValidation(String file, String modelFile, int numFolds) throws FileNotFoundException, IOException, ClassNotFoundException {
		Reader fileReader = null;
		InstanceList data = null;
		fileReader = new FileReader(new File(file));
		Pipe p = null;
		CRF crf = null;
		TransducerEvaluator eval = null;
		p = new SimpleTaggerSentence2FeatureVectorSequence();
		p.getTargetAlphabet().lookupIndex(defaultLabel);
		p.setTargetProcessing(true);
		data = new InstanceList(p);
		data.addThruPipe(new LineGroupIterator(fileReader,
				Pattern.compile("^\\s*$"), true));
		//		logger.info
		//		("Number of features in training data: "+p.getDataAlphabet().size());

		//		logger.info ("Number of predicates: "+p.getDataAlphabet().size());

		InstanceList.CrossValidationIterator cvIter = data.crossValidationIterator(numFolds);
		double[] sumPrecision = null;
		double[] sumRecall = null;
		double[] sumF1 = null;
		int fold = 1;
		int i;
		while (cvIter.hasNext()) {
			InstanceList[] trainTestSplits = cvIter.nextSplit();
			InstanceList trainSplit = trainTestSplits[0];
			InstanceList testSplit = trainTestSplits[1];
			if (p.isTargetProcessing())
			{
				Alphabet targets = p.getTargetAlphabet();
				StringBuffer buf = new StringBuffer("Labels:");
				for (i = 0; i < targets.size(); i++)
					buf.append(" ").append(targets.lookupObject(i).toString());
				//			logger.info(buf.toString());
			}
			crf = train(trainSplit, crf);
			ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(modelFile));
			s.writeObject(crf);
			s.close();
			ObjectInputStream s_in =
					new ObjectInputStream(new FileInputStream(modelFile));
			crf = (CRF) s_in.readObject();
			s_in.close();
			p = crf.getInputPipe();
			p.setTargetProcessing(true);
			eval = new PerClassEvaluator(new InstanceList[] {testSplit}, new String[] {"Testing Fold-" + fold++});
			if (p.isTargetProcessing())
			{
				Alphabet targets = p.getTargetAlphabet();
				StringBuffer buf = new StringBuffer("Labels:");
				for (i = 0; i < targets.size(); i++)
					buf.append(" ").append(targets.lookupObject(i).toString());
				//			logger.info(buf.toString());
			}
			test(new NoopTransducerTrainer(crf), eval, testSplit);
			labels = ((PerClassEvaluator) eval).getLabels();
			if(sumPrecision == null || sumRecall == null || sumF1 == null) {
				sumPrecision = new double[labels.size()];
				sumRecall = new double[labels.size()];
				sumF1 = new double[labels.size()];
			}
			precisionValues = ((PerClassEvaluator) eval).getPrecisionValues();
			recallValues = ((PerClassEvaluator) eval).getRecallValues();
			f1Values = ((PerClassEvaluator) eval).getF1Values();
			Iterator<Double> precisionIt = precisionValues.iterator();
			i=0;
			while(precisionIt.hasNext()) {
				sumPrecision[i] += precisionIt.next().doubleValue();
				i++;
			}
			Iterator<Double> recallIt = recallValues.iterator();
			i=0;
			while(recallIt.hasNext()) {
				sumRecall[i] += recallIt.next().doubleValue();
				i++;
			}
			Iterator<Double> f1It = f1Values.iterator();
			i=0;
			while(f1It.hasNext()) {
				sumF1[i] += f1It.next().doubleValue();
				i++;
			}
		}
		//calculate cross-validation average measures
		precisionValues = new ArrayList<Double>();
		for(i=0; i<labels.size(); i++) {
			precisionValues.add(sumPrecision[i]/numFolds);
		}
		recallValues = new ArrayList<Double>();
		for(i=0; i<labels.size(); i++) {
			recallValues.add(sumRecall[i]/numFolds);
		}
		f1Values = new ArrayList<Double>();
		for(i=0; i<labels.size(); i++) {
			f1Values.add(sumF1[i]/numFolds);
		}
		printEvaluationMeasures();
	}

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
}
