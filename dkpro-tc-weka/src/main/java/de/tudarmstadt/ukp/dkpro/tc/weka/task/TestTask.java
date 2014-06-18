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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.io.File;
import java.util.List;

import meka.classifiers.multilabel.MultilabelClassifier;
import meka.core.Result;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;

import weka.attributeSelection.AttributeSelection;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
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
    implements Constants
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
    private String featureMode;
    @Discriminator
    private String learningMode;
    @Discriminator
    String threshold;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        File arffFileTrain = new File(aContext.getStorageLocation(
                TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_FILENAME);
        File arffFileTest = new File(aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TEST_DATA,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_FILENAME);

        Instances trainData = TaskUtils.getInstances(arffFileTrain, multiLabel);
        Instances testData = TaskUtils.getInstances(arffFileTest, multiLabel);

        // do not balance in regression experiments
        if (!learningMode.equals(Constants.LM_REGRESSION)) {
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
        trainData = WekaUtils.removeOutcomeId(trainData, multiLabel);
        testData = WekaUtils.removeOutcomeId(testData, multiLabel);

        // FEATURE SELECTION
        if (!multiLabel && featureSearcher != null && attributeEvaluator != null) {
            try {
                AttributeSelection selector = TaskUtils.singleLabelAttributeSelection(trainData,
                        featureSearcher, attributeEvaluator);
                // Write the results of attribute selection
                FileUtils.writeStringToFile(
                        new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                                AccessMode.READWRITE)
                                .getAbsolutePath()
                                + "/" + FEATURE_SELECTION_DATA_FILENAME),
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
                File fsResultsFile = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                        AccessMode.READWRITE).getAbsolutePath()
                        + "/" + FEATURE_SELECTION_DATA_FILENAME);
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
        File evalOutput = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                AccessMode.READWRITE)
                .getPath()
                + "/" + EVALUATION_DATA_FILENAME);

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
        DataSink.write(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
                .getAbsolutePath() + "/" + PREDICTIONS_FILENAME, testData);
    }
}