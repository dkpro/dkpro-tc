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
package de.tudarmstadt.ukp.dkpro.tc.weka.task.serialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ValidityCheckTask;
import de.tudarmstadt.ukp.dkpro.tc.core.util.SaveModelUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaTestTask_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Save model batch
 * 
 */
public class SaveModelWekaBatchTask
    extends BatchTask
{

    private String experimentName;
    private AnalysisEngineDescription preprocessingPipeline;
    private List<String> operativeViews;
    private TCMachineLearningAdapter mlAdapter;
    private File outputFolder;

    // tasks
    private ValidityCheckTask checkTask;
    private PreprocessTask preprocessTaskTrain;
    private MetaInfoTask metaTask;
    private ExtractFeaturesTask featuresTrainTask;
    private ModelSerializationDescription saveModelTask;

    public SaveModelWekaBatchTask()
    {/* needed for Groovy */
    }

    public SaveModelWekaBatchTask(String aExperimentName, File outputFolder,
            Class<? extends TCMachineLearningAdapter> mlAdapter,
            AnalysisEngineDescription preprocessingPipeline)
        throws TextClassificationException
    {
        setExperimentName(aExperimentName);
        setPreprocessingPipeline(preprocessingPipeline);
        // set name of overall batch task
        setType("Evaluation-" + experimentName);
        setTcMachineLearningAdapter(mlAdapter);
        setOutputFolder(outputFolder);
    }

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        init();
        super.execute(aContext);
    }

    /**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * three-argument constructor.
     * 
     * @throws IllegalStateException
     *             if not all necessary arguments have been set.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void init()
    {
        if (experimentName == null || preprocessingPipeline == null)

        {
            throw new IllegalStateException("You must set Experiment Name and Aggregate.");
        }

        // check the validity of the experiment setup first
        checkTask = new ValidityCheckTask();
        checkTask.setMlAdapter(mlAdapter);

        // preprocessing on training data
        preprocessTaskTrain = new PreprocessTask();
        preprocessTaskTrain.setPreprocessing(preprocessingPipeline);
        preprocessTaskTrain.setOperativeViews(operativeViews);
        preprocessTaskTrain.setTesting(false);
        preprocessTaskTrain.setType(preprocessTaskTrain.getType() + "-Train-" + experimentName);

        // get some meta data depending on the whole document collection that we need for training
        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        metaTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN,
                MetaInfoTask.INPUT_KEY);

        // feature extraction on training data
        featuresTrainTask = new ExtractFeaturesTask();
        featuresTrainTask.setType(featuresTrainTask.getType() + "-Train-" + experimentName);
        featuresTrainTask.setMlAdapter(mlAdapter);
        featuresTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);
        featuresTrainTask.addImport(preprocessTaskTrain, PreprocessTask.OUTPUT_KEY_TRAIN,
                ExtractFeaturesTask.INPUT_KEY);

        // feature extraction and prediction on test data
        saveModelTask = new ModelSerializationDescription();
        saveModelTask.setType(saveModelTask.getType() + "-" + experimentName);
        saveModelTask.addImport(metaTask, MetaInfoTask.META_KEY);
        saveModelTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        saveModelTask.setOutputFolder(outputFolder);

        // DKPro Lab issue 38: must be added as *first* task
        addTask(checkTask);
        addTask(preprocessTaskTrain);
        addTask(metaTask);
        addTask(featuresTrainTask);
        addTask(saveModelTask);
    }

    public void setExperimentName(String experimentName)
    {
        this.experimentName = experimentName;
    }

    public void setPreprocessingPipeline(AnalysisEngineDescription preprocessingPipeline)
    {
        this.preprocessingPipeline = preprocessingPipeline;
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }

    public void setTcMachineLearningAdapter(Class<? extends TCMachineLearningAdapter> mlAdapter)
        throws TextClassificationException
    {
        try {
            this.mlAdapter = mlAdapter.newInstance();
        }
        catch (InstantiationException e) {
            throw new TextClassificationException(e);
        }
        catch (IllegalAccessException e) {
            throw new TextClassificationException(e);
        }
    }

    public void setOutputFolder(File outputFolder)
    {
        this.outputFolder = outputFolder;
    }
}

/**
 * Knows what to do in order to serialize a model - is called as task by the main class
 */
class ModelSerializationDescription
    extends WekaTestTask_ImplBase
    implements Constants
{

    @Discriminator
    protected List<Object> pipelineParameters;

    private File outputFolder;

    public void setOutputFolder(File outputFolder)
    {
        this.outputFolder = outputFolder;
    }

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {

        serializeWekaModel(aContext);

        // write feature extractors
        SaveModelUtils.writeFeatureInformation(outputFolder, featureSet);

        // write meta collector data
        // automatically determine the required metaCollector classes from the provided feature
        // extractors
        SaveModelUtils.writeModelParameters(aContext, outputFolder, featureSet,
                pipelineParameters);

        // as a marker for the type, write the name of the ml adapter class
        // write feature extractors
        SaveModelUtils.writeModelAdapterInformation(outputFolder,
                WekaClassificationAdapter.class.getName());
    }

    private void serializeWekaModel(TaskContext aContext)
        throws Exception
    {
        boolean isMultiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
        boolean isRegression = learningMode.equals(Constants.LM_REGRESSION);

        File arffFileTrain = new File(aContext.getStorageLocation(
                TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY).getPath()
                + "/"
                + WekaClassificationAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.featureVectorsFile));

        Instances trainData = WekaUtils.getInstances(arffFileTrain, isMultiLabel);
        trainData = WekaUtils.removeInstanceId(trainData, isMultiLabel);

        featureSelection(aContext, trainData);

        // File outputFolder = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
        // AccessMode.READWRITE)
        // .getPath());

        // write model file
        Classifier cl = getClassifier();
        cl.buildClassifier(trainData);
        weka.core.SerializationHelper.write(
                new File(outputFolder, MODEL_CLASSIFIER).getAbsolutePath(), cl);

        // write attribute file
        StringBuilder attributes = new StringBuilder();
        Enumeration<Attribute> atts = trainData.enumerateAttributes();
        while (atts.hasMoreElements()) {
            attributes.append(atts.nextElement().name());
            attributes.append("\n");
        }
        attributes.append(trainData.classAttribute().name());
        attributes.append("\n");

        FileUtils.writeStringToFile(new File(outputFolder, MODEL_FEATURE_NAMES),
                attributes.toString());

        // write class labels file
        List<String> classLabels;
        if (!isRegression) {
            classLabels = WekaUtils.getClassLabels(trainData, isMultiLabel);
            String classLabelsString = StringUtils.join(classLabels, "\n");
            FileUtils.writeStringToFile(new File(outputFolder, MODEL_CLASS_LABELS),
                    classLabelsString);
        }

    }
}