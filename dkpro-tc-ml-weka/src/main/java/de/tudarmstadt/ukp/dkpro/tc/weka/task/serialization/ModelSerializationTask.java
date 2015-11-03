/**
 * Copyright 2015
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
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.core.util.SaveModelUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaTestTask_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Knows what to do in order to serialize a model - is called as task by the main class.
 * 
 * Requires MetaTask and the output for the feature extraction stage.
 * Should be called after instantiation, using addImport(...), e.g.:
 * 
 * 	ModelSerializationTask saveModelTask = new ModelSerializationTask(...);
 *  saveModelTask.addImport(metaTask, MetaInfoTask.META_KEY);
 *  saveModelTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY, Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
 */
public class ModelSerializationTask
		extends WekaTestTask_ImplBase
	    implements Constants
{
    private File outputFolder;
    
    public ModelSerializationTask() {
    	// required for groovy (?)
    }

    public ModelSerializationTask(String type, File outputFolder) {
    	this.setType(type);
    	this.setOutputFolder(outputFolder);
    }
    
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
        
        SaveModelUtils.writeCurrentVersionOfDKProTC(outputFolder);
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
        File model = new File(outputFolder, MODEL_CLASSIFIER);
        model.getParentFile().mkdir();
        weka.core.SerializationHelper.write(model.getAbsolutePath(), cl);

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
