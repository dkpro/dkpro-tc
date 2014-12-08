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

import static de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask.META_KEY;

import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Writes the model
 * 
 */
public class WekaSaveModelTask
    extends WekaTestTask_ImplBase
    implements Constants
{

    @Discriminator
    protected List<Object> pipelineParameters;
    
    private File outputFolder;
    
    public void setOutputFolder(File outputFolder) {
    	this.outputFolder = outputFolder;
    }
    
    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean isMultiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
        boolean isRegression = learningMode.equals(Constants.LM_REGRESSION);

        File arffFileTrain = new File(aContext.getStorageLocation(
                TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY).getPath()
                + "/" + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile));
        
        Instances trainData = WekaUtils.getInstances(arffFileTrain, isMultiLabel);
        trainData = WekaUtils.removeInstanceId(trainData, isMultiLabel);

        featureSelection(aContext, trainData);

//        File outputFolder = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
//                AccessMode.READWRITE)
//                .getPath());
        
        // write model file
        Classifier cl = getClassifier();
        cl.buildClassifier(trainData);
        weka.core.SerializationHelper.write(new File(outputFolder, "classifier.ser").getAbsolutePath(), cl);
        
        // write attribute file
        StringBuilder attributes = new StringBuilder();
        Enumeration<Attribute> atts = trainData.enumerateAttributes();
        while (atts.hasMoreElements()) {
        	attributes.append(atts.nextElement().name());
        	attributes.append("\n");
        }
        attributes.append(trainData.classAttribute().name());
        attributes.append("\n");
                
        FileUtils.writeStringToFile(new File(outputFolder, "attributes.txt"), attributes.toString());
        
        // write class labels file
        List<String> classLabels;
        if (!isRegression) {
            classLabels = WekaUtils.getClassLabels(trainData, isMultiLabel);       
            String classLabelsString = StringUtils.join(classLabels, "\n");
            FileUtils.writeStringToFile(new File(outputFolder, "classLabels.txt"), classLabelsString);        
        }
        
        // write feature extractors
        String featureExtractorString = StringUtils.join(featureSet, "\n");
        FileUtils.writeStringToFile(new File(outputFolder, "features.txt"), featureExtractorString);        

        // write meta collector data
        // automatically determine the required metaCollector classes from the provided feature
        // extractors
        Set<Class<? extends MetaCollector>> metaCollectorClasses;
        Set<String> requiredTypes;
        try {
            metaCollectorClasses = TaskUtils.getMetaCollectorsFromFeatureExtractors(featureSet);
            requiredTypes = TaskUtils.getRequiredTypesFromFeatureExtractors(featureSet);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
        catch (InstantiationException e) {
            throw new ResourceInitializationException(e);
        }
        catch (IllegalAccessException e) {
            throw new ResourceInitializationException(e);
        }

        // collect parameter/key pairs that need to be set
        Map<String, String> metaParameterKeyPairs = new HashMap<String, String>();
        for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
            try {
                metaParameterKeyPairs.putAll(metaCollectorClass.newInstance().getParameterKeyPairs());
            }
            catch (InstantiationException e) {
                throw new ResourceInitializationException(e);
            }
            catch (IllegalAccessException e) {
                throw new ResourceInitializationException(e);
            }
        }

        Properties parameterProperties = new Properties();
        for (Entry<String, String> entry : metaParameterKeyPairs.entrySet()) {
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READWRITE),
                    entry.getValue());
            parameterProperties.put(entry.getKey(), file.getAbsolutePath());
        }
        
        // TODO re-add also the other parameters. Otherwise feature extractors might e.g. work in default mode and produce different results
//        // add all other pipeline parameters
//        for (int i=0; i<pipelineParameters.size(); i=i+2) {
//            parameterProperties.put((String) pipelineParameters.get(i), pipelineParameters.get(i+1));	
//        }

        FileWriter writer = new FileWriter(new File(outputFolder, "parameter.txt"));
        parameterProperties.store(writer, "");
        writer.close();
    }
}