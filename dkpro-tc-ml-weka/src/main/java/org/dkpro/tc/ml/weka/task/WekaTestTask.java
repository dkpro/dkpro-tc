/**
 * Copyright 2018
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
package org.dkpro.tc.ml.weka.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.weka.core.WekaTrainer;
import org.dkpro.tc.ml.weka.util.MultilabelResult;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import meka.core.Result;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;

/**
 * Base class for test task and save model tasks
 */
public class WekaTestTask
    extends ExecutableTaskBase
    implements Constants
{

    public final static String featureSelectionFile = "featureSelection.txt";

    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    protected List<Object> classificationArguments;

    @Discriminator(name = DIM_FEATURE_SEARCHER_ARGS)
    protected List<String> featureSearcher;

    @Discriminator(name = DIM_ATTRIBUTE_EVALUATOR_ARGS)
    protected List<String> attributeEvaluator;

    @Discriminator(name = DIM_LABEL_TRANSFORMATION_METHOD)
    protected String labelTransformationMethod;

    @Discriminator(name = DIM_NUM_LABELS_TO_KEEP)
    protected int numLabelsToKeep;

    @Discriminator(name = DIM_APPLY_FEATURE_SELECTION)
    protected boolean applySelection;

    @Discriminator(name = DIM_FEATURE_MODE)
    protected String featureMode;

    @Discriminator(name = DIM_LEARNING_MODE)
    protected String learningMode;

    @Discriminator(name = DIM_BIPARTITION_THRESHOLD)
    protected String threshold;

    public static final String evaluationBin = "evaluation.bin";

    @Override
    public void execute(TaskContext aContext) throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        File arffFileTrain = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA,
                Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT, AccessMode.READONLY);
        File arffFileTest = WekaUtils.getFile(aContext, TEST_TASK_INPUT_KEY_TEST_DATA,
                Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT, AccessMode.READONLY);

        Instances trainData = WekaUtils.getInstances(arffFileTrain, multiLabel);
        Instances testData = WekaUtils.getInstances(arffFileTest, multiLabel);

        WekaOutcomeHarmonizer harmonizer = new WekaOutcomeHarmonizer(trainData, testData,
                multiLabel);
        testData = harmonizer.harmonize();

        Instances copyTestData = new Instances(testData);
        trainData = WekaUtils.removeInstanceId(trainData, multiLabel);
        testData = WekaUtils.removeInstanceId(testData, multiLabel);

        WekaFeatureSelector selector = new WekaFeatureSelector(trainData, testData, multiLabel,
                attributeEvaluator, featureSearcher, labelTransformationMethod, numLabelsToKeep,
                aContext.getFile("featureSelection", AccessMode.READWRITE));
        selector.apply();
        trainData = selector.getTrainingInstances();
        testData = selector.getTestingInstances();

        // build classifier
        Classifier cl = WekaUtils.getClassifier(learningMode, classificationArguments);

        // file to hold prediction results
        File evalOutput = getFile(aContext, "", evaluationBin, AccessMode.READWRITE);

        // evaluation & prediction generation
        if (multiLabel) {
            // we don't need to build the classifier - meka does this
            // internally
            Result r = WekaUtils.getEvaluationMultilabel(cl, trainData, testData, threshold);
            WekaUtils.writeMlResultToFile(
                    new MultilabelResult(r.allTrueValues(), r.allPredictions(), threshold),
                    evalOutput);
            testData = WekaUtils.getPredictionInstancesMultiLabel(testData, cl,
                    WekaUtils.getMekaThreshold(threshold, r, trainData));
            testData = WekaUtils.addInstanceId(testData, copyTestData, true);
        }
        else {

            WekaTrainer trainer = new WekaTrainer();
            File model = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READWRITE);
            Classifier classifier = trainer.train(trainData, model,
                    getParameters(classificationArguments));

            createWekaEvaluationObject(classifier, evalOutput, trainData, testData);
            
            testData = getPredictionInstancesSingleLabel(testData, classifier);
            testData = WekaUtils.addInstanceId(testData, copyTestData, false);
        }

        // Write out the prediction - the data sink expects an .arff ending file so we game it a bit
        // and rename the file afterwards to .txt
        File predictionFile = WekaUtils.getFile(aContext, "", Constants.FILENAME_PREDICTIONS,
                AccessMode.READWRITE);
        File arffDummy = new File(predictionFile.getParent(), "prediction.arff");
        DataSink.write(arffDummy.getAbsolutePath(), testData);
        FileUtils.moveFile(arffDummy, predictionFile);
    }

    private void createWekaEvaluationObject(Classifier classifier, File evalOutput, Instances trainData,
            Instances testData)
        throws Exception
    {
        Evaluation eval = new Evaluation(trainData);
        eval.evaluateModel(classifier, testData);
        
        weka.core.SerializationHelper.write(evalOutput.getAbsolutePath(), eval);
        
    }

    private List<String> getParameters(List<Object> classificationArguments)
    {
        List<String> o = new ArrayList<>();

        for (int i = 1; i < classificationArguments.size(); i++) {
            o.add((String) classificationArguments.get(i));
        }

        return o;
    }

    private File getFile(TaskContext aContext, String key, String entry, AccessMode mode)
    {
        String path = aContext.getFolder(key, mode).getPath();
        String pathToArff = path + "/" + entry;

        return new File(pathToArff);
    }
    
    public Instances getPredictionInstancesSingleLabel(Instances testData, Classifier cl)
        throws Exception
    {

        StringBuffer classVals = new StringBuffer();
        for (int i = 0; i < testData.classAttribute().numValues(); i++) {
            if (classVals.length() > 0) {
                classVals.append(",");
            }
            classVals.append(testData.classAttribute().value(i));
        }

        // get predictions
        List<Double> labelPredictionList = new ArrayList<Double>();
        for (int i = 0; i < testData.size(); i++) {
            labelPredictionList.add(cl.classifyInstance(testData.instance(i)));
        }

        // add an attribute with the predicted values at the end off the attributes
        Add filter = new Add();
        filter.setAttributeName(WekaTestTask.PREDICTION_CLASS_LABEL_NAME);
        if (classVals.length() > 0) {
            filter.setAttributeType(new SelectedTag(Attribute.NOMINAL, Add.TAGS_TYPE));
            filter.setNominalLabels(classVals.toString());
        }
        filter.setInputFormat(testData);
        testData = Filter.useFilter(testData, filter);

        // fill predicted values for each instance
        for (int i = 0; i < labelPredictionList.size(); i++) {
            testData.instance(i).setValue(testData.classIndex() + 1, labelPredictionList.get(i));
        }
        return testData;
    }
    
}
