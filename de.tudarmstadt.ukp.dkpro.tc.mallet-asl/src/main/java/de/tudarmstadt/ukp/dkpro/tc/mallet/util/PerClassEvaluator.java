package de.tudarmstadt.ukp.dkpro.tc.mallet.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

import cc.mallet.fst.TokenAccuracyEvaluator;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.MatrixOps;
import cc.mallet.types.Sequence;
import cc.mallet.util.MalletLogger;
import de.tudarmstadt.ukp.dkpro.tc.mallet.task.TestTask;

/**
 * Determines the precision, recall and F1 on a per-class basis.
 * 
 * @author Krish Perumal
 */

public class PerClassEvaluator extends TransducerEvaluator {

	private static Logger logger = MalletLogger.getLogger(TokenAccuracyEvaluator.class.getName());

	private Integer numberOfCorrectPredictions = 0;
	private Integer numberOfIncorrectPredictions = 0;
	private Integer numberOfExamples = 0;

	private Double percentageOfCorrectPredictions = 0.0;
	private Double percentageOfIncorrectPredictions = 0.0;

	private ArrayList<Double> precisionValues = new ArrayList<Double>();
	private ArrayList<Double> recallValues = new ArrayList<Double>();
	private ArrayList<Double> f1Values = new ArrayList<Double>();

	private Double macroAverage = 0.0;

	public Integer getNumberOfCorrectPredictions() {
		return numberOfCorrectPredictions;
	}

	public void setNumberOfCorrectPredictions(Integer numberOfCorrectPredictions) {
		this.numberOfCorrectPredictions = numberOfCorrectPredictions;
	}

	public Integer getNumberOfIncorrectPredictions() {
		return numberOfIncorrectPredictions;
	}

	public void setNumberOfIncorrectPredictions(Integer numberOfIncorrectPredictions) {
		this.numberOfIncorrectPredictions = numberOfIncorrectPredictions;
	}

	public Double getPercentageOfCorrectPredictions() {
		return percentageOfCorrectPredictions;
	}

	public void setPercentageOfCorrectPredictions(
			Double percentageOfCorrectPredictions) {
		this.percentageOfCorrectPredictions = percentageOfCorrectPredictions;
	}

	public Integer getNumberOfExamples() {
		return numberOfExamples;
	}

	public void setNumberOfExamples(Integer numberOfExamples) {
		this.numberOfExamples = numberOfExamples;
	}

	public Double getPercentageOfIncorrectPredictions() {
		return percentageOfIncorrectPredictions;
	}

	public void setPercentageOfIncorrectPredictions(
			Double percentageOfIncorrectPredictions) {
		this.percentageOfIncorrectPredictions = percentageOfIncorrectPredictions;
	}
	
	public Double getMacroAverage() {
		return macroAverage;
	}

	public void setMacroAverage(Double macroAverage) {
		this.macroAverage = macroAverage;
	}

	private static ArrayList<String> labelNames = new ArrayList<String>();
	private static ArrayList<String> predictedLabels = new ArrayList<String>();
	private static ArrayList<String> goldLabels = new ArrayList<String>();

	public PerClassEvaluator (InstanceList[] instanceLists, String[] descriptions) {
		super (instanceLists, descriptions);
	}

	public PerClassEvaluator (InstanceList i1, String d1) {
		this (new InstanceList[] {i1}, new String[] {d1});
	}

	public PerClassEvaluator (InstanceList i1, String d1, InstanceList i2, String d2) {
		this (new InstanceList[] {i1, i2}, new String[] {d1, d2});
	}

	public void evaluateInstanceList (TransducerTrainer tt, InstanceList data, String description)
	{
		Transducer model = tt.getTransducer();
		Alphabet dict = model.getInputPipe().getTargetAlphabet();
		int numLabels = dict.size();
		int[] numCorrectTokens = new int [numLabels];
		int[] numPredTokens = new int [numLabels];
		int[] numTrueTokens = new int [numLabels];

		logger.info("Per-token results for " + description);
		for (int i = 0; i < data.size(); i++) {
			Instance instance = data.get(i);
			Sequence input = (Sequence) instance.getData();
			Sequence trueOutput = (Sequence) instance.getTarget();
			assert (input.size() == trueOutput.size());
			Sequence predOutput = model.transduce (input);
			assert (predOutput.size() == trueOutput.size());
			for (int j = 0; j < trueOutput.size(); j++) {
				int idx = dict.lookupIndex(trueOutput.get(j));
				numTrueTokens[idx]++;
				numPredTokens[dict.lookupIndex(predOutput.get(j))]++;
				if (trueOutput.get(j).equals(predOutput.get(j))) {
					numCorrectTokens[idx]++;
					numberOfCorrectPredictions++;
				}
				else {
					numberOfIncorrectPredictions++;
				}
				goldLabels.add(trueOutput.get(j).toString());
				predictedLabels.add(predOutput.get(j).toString());
			}
		}

		setNumberOfExamples(numberOfCorrectPredictions + numberOfIncorrectPredictions);
		setPercentageOfCorrectPredictions((numberOfCorrectPredictions*100)/(double) numberOfExamples);
		setPercentageOfIncorrectPredictions((numberOfIncorrectPredictions*100)/(double) numberOfExamples);
		
		precisionValues = new ArrayList<Double>();
		recallValues = new ArrayList<Double>();
		f1Values = new ArrayList<Double>();
		labelNames = new ArrayList<String>();

		DecimalFormat f = new DecimalFormat ("0.####");
		double[] allf = new double [numLabels];
		for (int i = 0; i < numLabels; i++) {
			Object label = dict.lookupObject(i);
			double precision = ((double) numCorrectTokens[i]) / numPredTokens[i];
			double recall = ((double) numCorrectTokens[i]) / numTrueTokens[i];
			double f1 = (2 * precision * recall) / (precision + recall);
			if (!Double.isNaN (f1)) allf [i] = f1;
			logger.info(description +" label " + label + " P " + f.format (precision)
					+ " R " + f.format(recall) + " F1 "+ f.format (f1));
			precisionValues.add(precision);
			recallValues.add(recall);
			f1Values.add(f1);
			labelNames.add(label.toString());
		}

		logger.info ("Macro-average F1 "+f.format (MatrixOps.mean (allf)));
		setMacroAverage(MatrixOps.mean (allf));

	}

	public ArrayList<Double> getPrecisionValues() {
		return precisionValues;
	}

	public ArrayList<Double> getRecallValues() {
		return recallValues;
	}

	public ArrayList<Double> getF1Values() {
		return f1Values;
	}

	public ArrayList<String> getLabelNames() {
		return labelNames;
	}

	public ArrayList<String> getGoldLabels() {
		return goldLabels;
	}

	public ArrayList<String> getPredictedLabels() {
		return predictedLabels;
	}

}
