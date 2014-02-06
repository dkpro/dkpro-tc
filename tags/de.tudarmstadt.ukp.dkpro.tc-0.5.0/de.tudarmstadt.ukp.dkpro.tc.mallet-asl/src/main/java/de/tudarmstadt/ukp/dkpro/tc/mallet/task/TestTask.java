package de.tudarmstadt.ukp.dkpro.tc.mallet.task;

import java.io.File;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.mallet.util.TaskUtils;

/**
 * Builds the classifier from the training data and performs classification on the test data.
 * 
 * @author Krish Perumal
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
    private boolean sequenceTagging;
    
    @Discriminator
    String threshold;

    public static final String INPUT_KEY_TRAIN = "input.train";
    public static final String INPUT_KEY_TEST = "input.test";
    public static final String OUTPUT_KEY = "output";
    public static final String RESULTS_KEY = "results.prop";
    public static final String PREDICTIONS_KEY = "predictions.arff";
    public static final String TRAINING_DATA_KEY = "training-data.txt.gz"; //TODO add from Constants
    public static final String EVALUATION_DATA_KEY = "evaluation.bin";
//    public static final String FEATURE_SELECTION_DATA_KEY = "attributeEvaluationResults.txt";
    public static final String PREDICTION_CLASS_LABEL_NAME = "prediction";
    public static final String MALLET_MODEL_KEY = "mallet-model";
    // public static final String GOLD_STANDARD_CLASS_LABEL_NAME = "goldstandard";

    public static boolean MULTILABEL;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {

        File fileTrain = new File(aContext.getStorageLocation(INPUT_KEY_TRAIN,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_KEY);
        File fileTest = new File(aContext.getStorageLocation(INPUT_KEY_TEST,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_KEY);
     
        File fileModel = new File(aContext.getStorageLocation(OUTPUT_KEY, AccessMode.READWRITE)
                .getPath() + "/" + MALLET_MODEL_KEY);
        
        TaskUtils.runTrainTest(fileTrain, fileTest, fileModel);
        
    }
}