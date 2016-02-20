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
package de.tudarmstadt.ukp.dkpro.tc.weka.task.serialization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ModelSerializationTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
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
public class WekaModelSerializationDescription
		extends ModelSerializationTask
	    implements Constants
{
    
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
    
    public WekaModelSerializationDescription() {
    	// required for groovy (?)
    }

    public WekaModelSerializationDescription(String type, File outputFolder) {
    	this.setType(type);
    	this.setOutputFolder(outputFolder);
    }

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        writeWekaSpecificInformation(aContext);
        writeModelConfiguration(aContext, WekaClassificationAdapter.class.getName());
    }

    private void writeWekaSpecificInformation(TaskContext aContext)
        throws Exception
    {
        boolean isMultiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
        boolean isRegression = learningMode.equals(Constants.LM_REGRESSION);

        File arffFileTrain = new File(aContext.getFolder(
                TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY).getPath()
                + "/"
                + WekaClassificationAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.featureVectorsFile));

        Instances trainData = WekaUtils.getInstances(arffFileTrain, isMultiLabel);
        trainData = WekaUtils.removeInstanceId(trainData, isMultiLabel);

        
     // FEATURE SELECTION
        WekaUtils.featureSelection(aContext, trainData, learningMode, featureSearcher, attributeEvaluator,applySelection, labelTransformationMethod, numLabelsToKeep);

        // write attribute file
        List<Attribute> attributes = new ArrayList<Attribute>();
        Enumeration<Attribute> atts = trainData.enumerateAttributes();
        
        while (atts.hasMoreElements()) {
            attributes.add(atts.nextElement());
        }
        attributes.add(trainData.classAttribute());
        
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(outputFolder, MODEL_FEATURE_NAMES_SERIALIZED)));
        out.writeObject(attributes);
        out.close();
        
        // write model file
        Classifier cl = WekaUtils.getClassifier(learningMode, classificationArguments);
        cl.buildClassifier(trainData);
        File model = new File(outputFolder, MODEL_CLASSIFIER);
        model.getParentFile().mkdir();
        weka.core.SerializationHelper.write(model.getAbsolutePath(), cl);

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
