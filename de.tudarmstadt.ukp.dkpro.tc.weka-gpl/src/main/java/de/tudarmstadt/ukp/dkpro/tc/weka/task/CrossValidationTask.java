package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.multilabel.Evaluation;
import weka.classifiers.multilabel.MultilabelClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Result;
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

public class CrossValidationTask
    extends ExecutableTaskBase
{
    @Discriminator
    private String[] classificationArguments;

    @Discriminator
    private int folds;

    @Discriminator
    private boolean multiLabel;

    @Discriminator
    String threshold = "1.";

    public static final String INPUT_KEY = "input";
    public static final String OUTPUT_KEY = "output";
    public static final String RESULTS_KEY = "results.prop";
    public static final String PREDICTIONS_KEY = "predictions_fold#.arff";
    public static final String TRAINING_DATA_KEY = "training-data.arff.gz";
    public static final String EVALUATION_DATA_KEY = "evaluation_fold#.txt";
    public static int FOLDS;
    public static boolean MULTILABEL;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        MULTILABEL = multiLabel;

        File arffFile = new File(aContext.getStorageLocation(INPUT_KEY, AccessMode.READONLY)
                .getPath() + "/" + TRAINING_DATA_KEY);

        Instances data = TaskUtils.getInstances(arffFile, multiLabel);

        Instances randData = new Instances(data);

        FOLDS = getFolds(randData);

        // this will make the Crossvalidation outcome unpredictable
        randData.randomize(new Random(new Date().getTime()));
        randData.stratify(FOLDS);

        Classifier cl;
        String[] mlArgs;

        for (int n = 0; n < FOLDS; n++) {
            aContext.message("Performing " + FOLDS + "-fold cross-validation (" + n + ", with "
                    + Arrays.asList(classificationArguments) + ")");

            // Train-Test-split
            Instances train = randData.trainCV(FOLDS, n);
            Instances test = randData.testCV(FOLDS, n);

            if (multiLabel) {
                mlArgs = Arrays.copyOfRange(classificationArguments, 1,
                        classificationArguments.length);
                cl = AbstractClassifier.forName(classificationArguments[0], new String[] {});
                ((MultilabelClassifier) cl).setOptions(mlArgs);
            }
            else {
                cl = AbstractClassifier.forName(classificationArguments[0], Arrays.copyOfRange(
                        classificationArguments, 1, classificationArguments.length));
            }

            Instances filteredTrainData;
            Instances filteredTestData;

            if (train.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME) != null) {

                int instanceIdOffset = // TaskUtils.getInstanceIdAttributeOffset(trainData);
                train.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME).index() + 1;

                Remove remove = new Remove();
                remove.setAttributeIndices(Integer.toString(instanceIdOffset));
                remove.setInvertSelection(false);
                remove.setInputFormat(train);

                filteredTrainData = Filter.useFilter(train, remove);
                filteredTestData = Filter.useFilter(test, remove);
            }
            else {
                filteredTrainData = new Instances(train);
                filteredTestData = new Instances(test);
            }

            // train the classifier on the train set split
            cl.buildClassifier(filteredTrainData);

            // file to hold Crossvalidation results
            File evalOutput = new File(aContext
                    .getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE).getPath()
                    + "/"
                    + StringUtils.replace(EVALUATION_DATA_KEY, "#", String.valueOf(n)));

            // evaluation
            if (multiLabel) {
                Result r = Evaluation.evaluateModel((MultilabelClassifier) cl, filteredTrainData,
                        filteredTestData, threshold);
                Result.writeResultToFile(r, evalOutput.getAbsolutePath());
                // add predictions for test set
                double[] t = TaskUtils.getMekaThreshold(threshold, r, filteredTrainData);
                for (int j = 0; j < filteredTestData.numInstances(); j++) {
                    Instance predicted = filteredTestData.instance(j);
                    // multi-label classification results
                    double[] labelPredictions = cl.distributionForInstance(predicted);
                    for (int i = 0; i < labelPredictions.length; i++) {
                        predicted.setValue(i, labelPredictions[i] >= t[i] ? 1. : 0.);
                    }
                    test.add(predicted);
                }
                int numLabels = filteredTestData.classIndex();

                // add attributes to store gold standard classification
                Add filter = new Add();
                for (int i = 0; i < numLabels; i++) {
                    filter.setAttributeIndex(new Integer(numLabels + i + 1).toString());
                    filter.setNominalLabels("0,1");
                    filter.setAttributeName(test.attribute(i).name() + "_classification");
                    filter.setInputFormat(test);
                    test = Filter.useFilter(test, filter);
                }
                // fill values of gold standard classification with original values from test set
                for (int i = 0; i < test.size(); i++) {
                    for (int j = 0; j < numLabels; j++) {
                        test.instance(i).setValue(j + numLabels, test.instance(i).value(j));
                    }
                }
            }
            else {
                weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(
                        filteredTrainData);
                eval.evaluateModel(cl, filteredTestData);
                weka.core.SerializationHelper.write(evalOutput.getAbsolutePath(), eval);

                for (int i = 0; i < filteredTestData.numInstances(); i++) {
                    double prediction = cl.classifyInstance(filteredTestData.instance(i));
                    Instance instance = test.instance(i);
                    instance.setClassValue(prediction);
                }
            }

            // Write out the predictions
            DataSink.write(
                    aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE).getAbsolutePath()
                            + "/" + StringUtils.replace(PREDICTIONS_KEY, "#", String.valueOf(n)),
                    test);
        }
    }

    /**
     * @param data
     *            The dataset. May affect the number of folds.
     * @return Number of folds for cross-validation.
     */
    protected int getFolds(Instances data)
    {
        return folds;
    }
}