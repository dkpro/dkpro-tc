/**
 * Copyright 2016
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
package org.dkpro.tc.weka.task;

import java.io.File;
import java.util.List;

import meka.core.Result;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;

import weka.attributeSelection.AttributeSelection;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.unsupervised.attribute.Remove;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.weka.util.MultilabelResult;
import org.dkpro.tc.weka.util.WekaUtils;

/**
 * Base class for test task and save model tasks
 */
public class WekaTestTask
    extends ExecutableTaskBase
    implements Constants
{

    @Discriminator
    protected List<Object> pipelineParameters;
    @Discriminator
    protected List<String> classificationArguments;
    @Discriminator
    protected List<String> featureSearcher;
    @Discriminator
    protected List<String> attributeEvaluator;
    @Discriminator
    protected String labelTransformationMethod;
    @Discriminator
    protected int numLabelsToKeep;
    @Discriminator
    protected boolean applySelection;
    @Discriminator
    protected String featureMode;
    @Discriminator
    protected List<String> featureSet;
    @Discriminator
    protected String learningMode;
    @Discriminator
    protected String threshold;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        File arffFileTrain = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AdapterNameEntries.featureVectorsFile, AccessMode.READONLY);
        File arffFileTest = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TEST_DATA,
                AdapterNameEntries.featureVectorsFile, AccessMode.READONLY);

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
        if (!learningMode.equals(Constants.LM_MULTI_LABEL)) {
            if (featureSearcher != null && attributeEvaluator != null) {
                AttributeSelection attSel = WekaUtils.featureSelectionSinglelabel(aContext,
                        trainData, featureSearcher, attributeEvaluator);
                File file = WekaUtils.getFile(aContext, TEST_TASK_OUTPUT_KEY,
                        AdapterNameEntries.featureSelectionFile, AccessMode.READWRITE);
                FileUtils.writeStringToFile(file, attSel.toResultsString());
                if (applySelection) {
                    Logger.getLogger(getClass()).info("APPLYING FEATURE SELECTION");
                    trainData = attSel.reduceDimensionality(trainData);
                    testData = attSel.reduceDimensionality(testData);
                }
            }
        }
        else {
            if (attributeEvaluator != null && labelTransformationMethod != null
                    && numLabelsToKeep > 0) {
                Remove attSel = WekaUtils.featureSelectionMultilabel(aContext, trainData,
                        attributeEvaluator, labelTransformationMethod, numLabelsToKeep);
                if (applySelection) {
                    Logger.getLogger(getClass()).info("APPLYING FEATURE SELECTION");
                    trainData = WekaUtils.applyAttributeSelectionFilter(trainData, attSel);
                    testData = WekaUtils.applyAttributeSelectionFilter(testData, attSel);
                }
            }
        }
        
        // build classifier
        Classifier cl = WekaUtils.getClassifier(learningMode, classificationArguments);

        // file to hold prediction results
        File evalOutput = WekaUtils.getFile(aContext, TEST_TASK_OUTPUT_KEY, AdapterNameEntries.evaluationFile, AccessMode.READWRITE);

        // evaluation & prediction generation
        if (multiLabel) {
            // we don't need to build the classifier - meka does this
            // internally
            Result r = WekaUtils.getEvaluationMultilabel(cl, trainData, testData, threshold);
            WekaUtils.writeMlResultToFile(new MultilabelResult(r.allActuals(), r.allPredictions(),
                    threshold), evalOutput);
            testData = WekaUtils.getPredictionInstancesMultiLabel(testData, cl,
                    WekaUtils.getMekaThreshold(threshold, r, trainData));
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
        File predictionFile = WekaUtils.getFile(aContext,TEST_TASK_OUTPUT_KEY, AdapterNameEntries.predictionsFile, AccessMode.READWRITE);
        DataSink.write(predictionFile.getAbsolutePath(), testData);
    }

}