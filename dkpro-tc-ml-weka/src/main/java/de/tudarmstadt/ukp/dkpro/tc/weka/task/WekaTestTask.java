/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.io.File;

import meka.core.Result;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.MultilabelResult;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Base class for test task and save model tasks
 */
public class WekaTestTask
    extends WekaTestTask_ImplBase
    implements Constants
{

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        File arffFileTrain = new File(aContext.getStorageLocation(
                TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY).getPath()
                + "/" + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile));
        File arffFileTest = new File(aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TEST_DATA,
                AccessMode.READONLY).getPath()
                + "/" + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile));

        Instances trainData = WekaUtils.getInstances(arffFileTrain, multiLabel);
        Instances testData = WekaUtils.getInstances(arffFileTest, multiLabel);

        // do not balance in regression experiments
        if (!learningMode.equals(Constants.LM_REGRESSION)) {
            testData = WekaUtils.makeOutcomeClassesCompatible(trainData, testData, multiLabel);
        }

        Instances copyTestData = new Instances(testData);
        trainData = WekaUtils.removeInstanceId(trainData, multiLabel);
        testData = WekaUtils.removeInstanceId(testData, multiLabel);

        // FEATURE SELECTION
        featureSelection(aContext, trainData);
        
        // build classifier
        Classifier cl = getClassifier();
        
        // file to hold prediction results
        File evalOutput = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                AccessMode.READWRITE).getPath()
                + "/" + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.evaluationFile));

        // evaluation & prediction generation
        if (multiLabel) {
            // we don't need to build the classifier - meka does this
            // internally
            Result r = WekaUtils.getEvaluationMultilabel(cl, trainData, testData, threshold);
            WekaUtils.writeMlResultToFile(new MultilabelResult(r.allActuals(), r.allPredictions(), threshold), evalOutput);
            testData = WekaUtils.getPredictionInstancesMultiLabel(testData, cl, WekaUtils.getMekaThreshold(threshold, r, trainData));
            testData = WekaUtils.addInstanceId(testData, copyTestData, true);
        }
        else {
            // train the classifier on the train set split - not necessary in multilabel setup, but
            // in single label setup
            cl.buildClassifier(trainData);
            weka.core.SerializationHelper.write(evalOutput.getAbsolutePath(),
                    WekaUtils.getEvaluationSinglelabel(cl, trainData, testData));
            testData = WekaUtils.getPredictionInstancesSingleLabel(testData, cl);
            testData = WekaUtils.addInstanceId(testData, copyTestData, false);
        }

        // Write out the predictions
        DataSink.write(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
                .getAbsolutePath() + "/" + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.predictionsFile), testData);
    }
}