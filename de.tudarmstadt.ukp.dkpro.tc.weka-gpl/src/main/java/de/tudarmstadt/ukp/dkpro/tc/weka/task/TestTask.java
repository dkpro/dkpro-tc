package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.io.File;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;

import weka.attributeSelection.AttributeSelection;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.multilabel.Evaluation;
import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Result;
import weka.core.SelectedTag;
import weka.core.SparseInstance;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
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

        Instances filteredTrainData;
        Instances filteredTestData;

        if (trainData.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME) != null) {

            int instanceIdOffset = // TaskUtils.getInstanceIdAttributeOffset(trainData);

            trainData.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME).index() + 1;

            Remove remove = new Remove();
            remove.setAttributeIndices(Integer.toString(instanceIdOffset));
            remove.setInvertSelection(false);
            remove.setInputFormat(trainData);

            filteredTrainData = Filter.useFilter(trainData, remove);
            filteredTestData = Filter.useFilter(testData, remove);
        }
        else {
            filteredTrainData = new Instances(trainData);
            filteredTestData = new Instances(testData);
        }

        // FEATURE SELECTION
        if (!multiLabel && featureSearcher != null && attributeEvaluator != null) {
            try {
                AttributeSelection selector = TaskUtils.singleLabelAttributeSelection(
                        filteredTrainData, featureSearcher, attributeEvaluator);
                // Write the results of attribute selection
                FileUtils.writeStringToFile(
                        new File(aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE)
                                .getAbsolutePath() + "/" + FEATURE_SELECTION_DATA_KEY),
                        selector.toResultsString());
                if (applySelection) {
                    filteredTrainData = selector.reduceDimensionality(filteredTrainData);
                    filteredTestData = selector.reduceDimensionality(filteredTestData);
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
                Remove removeFilter = TaskUtils.multiLabelAttributeSelection(filteredTrainData,
                        labelTransformationMethod, attributeEvaluator, numLabelsToKeep,
                        fsResultsFile);
                if (removeFilter != null && applySelection) {
                    filteredTrainData = TaskUtils.applyAttributeSelectionFilter(filteredTrainData,
                            removeFilter);
                    filteredTestData = TaskUtils.applyAttributeSelectionFilter(filteredTestData,
                            removeFilter);
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

        // evaluation
        if (multiLabel) {
            // we don't need to build the classifier - multi label evaluation does this
            // automatically

            Result r = Evaluation.evaluateModel((MultilabelClassifier) cl, filteredTrainData,
                    filteredTestData, threshold);
            Result.writeResultToFile(r, evalOutput.getAbsolutePath());
            // add predictions for test set
            double[] t = TaskUtils.getMekaThreshold(threshold, r, trainData);
            for (int i = 0; i < filteredTestData.numInstances(); i++) {
                Instance predicted = new SparseInstance(testData.instance(i));
                // multi-label classification results
                double[] labelPredictions = cl
                        .distributionForInstance(filteredTestData.instance(i));
                for (int j = 0; j < labelPredictions.length; j++) {
                    predicted.setValue(j, labelPredictions[j] >= t[j] ? 1. : 0.);
                }
                testData.add(predicted);
            }
            int numLabels = testData.classIndex();

            // add attributes to store gold standard classification
            Add filter = new Add();
            for (int i = 0; i < numLabels; i++) {
                filter.setAttributeIndex(new Integer(numLabels + i + 1).toString());
                filter.setNominalLabels("0,1");
                filter.setAttributeName(testData.attribute(i).name() + "_classification");
                filter.setInputFormat(testData);
                testData = Filter.useFilter(testData, filter);
            }
            // fill values of gold standard classification with original values from test set
            for (int i = 0; i < testData.size(); i++) {
                for (int j = 0; j < numLabels; j++) {
                    testData.instance(i).setValue(j + numLabels, testData.instance(i).value(j));
                }
            }
        }
        else {
            // train the classifier on the train set split - not necessary in multilabel setup, but
            // in single label setup
            cl.buildClassifier(filteredTrainData);

            weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(filteredTrainData);
            eval.evaluateModel(cl, filteredTestData);
            weka.core.SerializationHelper.write(evalOutput.getAbsolutePath(), eval);

            Enumeration enumAtt = testData.classAttribute().enumerateValues();
            StringBuffer classVals = new StringBuffer();
            while (enumAtt.hasMoreElements()) {
                if (classVals.length() > 0) {
                    classVals.append(",");
                }
                classVals.append(enumAtt.nextElement());
            }

            // add an attribute with the predicted values at the end off the attributes
            Add filter = new Add();
            filter.setAttributeName(PREDICTION_CLASS_LABEL_NAME);
            filter.setAttributeType(new SelectedTag(Attribute.NOMINAL, Add.TAGS_TYPE));
            filter.setNominalLabels(classVals.toString());
            filter.setInputFormat(testData);
            testData = Filter.useFilter(testData, filter);

            // fill predicted values for each instance
            for (int i = 0; i < testData.size(); i++) {
                double prediction = cl.classifyInstance(filteredTestData.instance(i));
                testData.instance(i).setValue(testData.classIndex() + 1, prediction);
            }
        }

        // Write out the predictions
        DataSink.write(aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE)
                .getAbsolutePath() + "/" + PREDICTIONS_KEY, testData);
    }
}