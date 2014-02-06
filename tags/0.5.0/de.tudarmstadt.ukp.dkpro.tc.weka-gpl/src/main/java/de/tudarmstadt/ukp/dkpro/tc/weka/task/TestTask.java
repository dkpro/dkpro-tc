package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;

import weka.attributeSelection.AttributeSelection;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Instances;
import weka.core.Result;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Builds the classifier from the training data and performs classification on the test data.
 * 
 * @author zesch
 * @author Artem Vovk
 * @author daxenberger
 * 
 */
public class TestTask
    extends ExecutableTaskBase
{
    @Discriminator
    private List<String> classificationArguments;

    @Discriminator
    private List<String> featureSearcher;

    @Discriminator
    private List<String> attributeEvaluator;

    @Discriminator
    private String labelTransformationMethod;

    @Discriminator
    private int numLabelsToKeep;

    @Discriminator
    private boolean applySelection;

    @Discriminator
    private boolean multiLabel;

    @Discriminator
    private boolean isRegressionExperiment;

    @Discriminator
    String threshold;

    public static final String INPUT_KEY_TRAIN = "input.train";
    public static final String INPUT_KEY_TEST = "input.test";
    public static final String OUTPUT_KEY = "output";
    public static final String RESULTS_KEY = "results.prop";
    public static final String PREDICTIONS_KEY = "predictions.arff";
    public static final String TRAINING_DATA_KEY = "training-data.arff.gz";
    public static final String EVALUATION_DATA_KEY = "evaluation.bin";
    public static final String FEATURE_SELECTION_DATA_KEY = "attributeEvaluationResults.txt";
    public static final String PREDICTION_CLASS_LABEL_NAME = "prediction";
    // public static final String GOLD_STANDARD_CLASS_LABEL_NAME = "goldstandard";

    public static boolean MULTILABEL;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {

        MULTILABEL = multiLabel;

        File arffFileTrain = new File(aContext.getStorageLocation(INPUT_KEY_TRAIN,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_KEY);
        File arffFileTest = new File(aContext.getStorageLocation(INPUT_KEY_TEST,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_KEY);

        Instances trainData = TaskUtils.getInstances(arffFileTrain, multiLabel);
        Instances testData = TaskUtils.getInstances(arffFileTest, multiLabel);

        // do not balance in regression experiments
        if (!isRegressionExperiment) {
            testData = WekaUtils.makeOutcomeClassesCompatible(trainData, testData, multiLabel);
        }

        Classifier cl;
        List<String> mlArgs;

        if (multiLabel) {
            mlArgs = classificationArguments.subList(1, classificationArguments.size());
            cl = AbstractClassifier.forName(classificationArguments.get(0), new String[] {});
            ((MultilabelClassifier) cl).setOptions(mlArgs.toArray(new String[0]));
        }
        else {
            cl = AbstractClassifier.forName(classificationArguments.get(0), classificationArguments
                    .subList(1, classificationArguments.size()).toArray(new String[0]));
        }

        Instances copyTestData = new Instances(testData);
        trainData = WekaUtils.removeOutcomeId(trainData);
        testData = WekaUtils.removeOutcomeId(testData);

        // FEATURE SELECTION
        if (!multiLabel && featureSearcher != null && attributeEvaluator != null) {
            try {
                AttributeSelection selector = TaskUtils.singleLabelAttributeSelection(trainData,
                        featureSearcher, attributeEvaluator);
                // Write the results of attribute selection
                FileUtils.writeStringToFile(
                        new File(aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE)
                                .getAbsolutePath() + "/" + FEATURE_SELECTION_DATA_KEY),
                        selector.toResultsString());
                if (applySelection) {
                    trainData = selector.reduceDimensionality(trainData);
                    testData = selector.reduceDimensionality(testData);
                }
            }
            catch (Exception e) {
                LogFactory.getLog(getClass()).warn("Could not apply feature selection.", e);
            }
        }
        if (multiLabel && attributeEvaluator != null && labelTransformationMethod != null
                && numLabelsToKeep != 0) {
            try {
                // file to hold the results of attribute selection
                File fsResultsFile = new File(aContext.getStorageLocation(OUTPUT_KEY,
                        AccessMode.READWRITE).getAbsolutePath()
                        + "/" + FEATURE_SELECTION_DATA_KEY);
                // filter for reducing dimension of attributes
                Remove removeFilter = TaskUtils.multiLabelAttributeSelection(trainData,
                        labelTransformationMethod, attributeEvaluator, numLabelsToKeep,
                        fsResultsFile);
                if (removeFilter != null && applySelection) {
                    trainData = TaskUtils.applyAttributeSelectionFilter(trainData, removeFilter);
                    testData = TaskUtils.applyAttributeSelectionFilter(testData, removeFilter);
                }
            }
            catch (Exception e) {
                LogFactory.getLog(getClass()).warn(
                        "Could not apply multi-label feature selection.", e);
            }
        }

        // file to hold prediction results
        File evalOutput = new File(aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE)
                .getPath() + "/" + EVALUATION_DATA_KEY);

        // evaluation & prediction generation
        if (multiLabel) {
            // we don't need to build the classifier - multi label evaluation does this
            // automatically
            Result r = WekaUtils.getEvaluationMultilabel(cl, trainData, testData, threshold);
            Result.writeResultToFile(r, evalOutput.getAbsolutePath());
            double[] t = TaskUtils.getMekaThreshold(threshold, r, trainData);
            testData = WekaUtils.getPredictionInstancesMultiLabel(testData, cl, t);
            testData = WekaUtils.addOutcomeId(testData, copyTestData, true);
        }
        else {
            // train the classifier on the train set split - not necessary in multilabel setup, but
            // in single label setup
            cl.buildClassifier(trainData);
            weka.core.SerializationHelper.write(evalOutput.getAbsolutePath(),
                    WekaUtils.getEvaluationSinglelabel(cl, trainData, testData));
            testData = WekaUtils.getPredictionInstancesSingleLabel(testData, cl);
            testData = WekaUtils.addOutcomeId(testData, copyTestData, false);
        }

        // Write out the predictions
        DataSink.write(aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE)
                .getAbsolutePath() + "/" + PREDICTIONS_KEY, testData);
    }
}