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
package de.tudarmstadt.ukp.dkpro.tc.mallet.task;

import java.io.File;
import java.util.ArrayList;

import cc.mallet.fst.TransducerEvaluator;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.mallet.util.TaskUtils;

/**
 * Builds the classifier from the training data and performs classification on the test data.
 * Currently, this task also generates various results in the form of reports. These need to be moved.
 * 
 * @author krishperumal11
 * 
 */
public class MalletTestTask
    extends ExecutableTaskBase
{
	@Discriminator
	private String tagger = "CRF"; //added to configure other taggers like HMM, although these are not supported
	
    @Discriminator
    private double gaussianPriorVariance = 10.0; //Gaussian Prior Variance

    @Discriminator
	private int iterations = 1000; //Number of iterations

    @Discriminator
	private String defaultLabel = "O";
    
    @Discriminator
    private int[] orders = new int[]{0, 1, 2, 3, 4};
    
    @Discriminator
    private boolean denseFeatureValues = true;
    
	public static ArrayList<Double> precisionValues;

	public static ArrayList<Double> recallValues;

	public static ArrayList<Double> f1Values;

	public static ArrayList<String> labels;

	// TODO - most of that should be in Constants
    public static final String PREDICTIONS_KEY = "predictions.txt.gz";
    public static final String TRAINING_DATA_KEY = "training-data.txt.gz"; //TODO Issue 127: add from Constants
    public static final String EVALUATION_DATA_KEY = "evaluation.csv";
    public static final String CONFUSION_MATRIX_KEY = "confusionMatrix.csv";
//    public static final String FEATURE_SELECTION_DATA_KEY = "attributeEvaluationResults.txt";
    public static final String PREDICTION_CLASS_LABEL_NAME = "PredictedOutcome";
    public static final String OUTCOME_CLASS_LABEL_NAME = "Outcome";
    public static final String MALLET_MODEL_KEY = "mallet-model";

    public static boolean MULTILABEL;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {

        File fileTrain = new File(aContext.getStorageLocation(Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_KEY);
        File fileTest = new File(aContext.getStorageLocation(Constants.TEST_TASK_INPUT_KEY_TEST_DATA,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_KEY);
     
        File fileModel = new File(aContext.getStorageLocation(Constants.TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
                .getPath() + "/" + MALLET_MODEL_KEY);
        
        TransducerEvaluator eval = TaskUtils.runTrainTest(fileTrain, fileTest, fileModel, gaussianPriorVariance, iterations, defaultLabel,
    			false, orders, tagger, denseFeatureValues);
        
        //TODO move to reports (@author krishperumal11)
        
        File filePredictions = new File(aContext.getStorageLocation(Constants.TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
                .getPath() + "/" + PREDICTIONS_KEY);
        
        TaskUtils.outputPredictions(eval, fileTest, filePredictions, PREDICTION_CLASS_LABEL_NAME);
        
        File fileEvaluation = new File(aContext.getStorageLocation(Constants.TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
                .getPath() + "/" + EVALUATION_DATA_KEY);
        
        TaskUtils.outputEvaluation(eval, fileEvaluation);
        
        File fileConfusionMatrix = new File(aContext.getStorageLocation(Constants.TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
                .getPath() + "/" + CONFUSION_MATRIX_KEY);
        
        TaskUtils.outputConfusionMatrix(eval, fileConfusionMatrix);
    }
}