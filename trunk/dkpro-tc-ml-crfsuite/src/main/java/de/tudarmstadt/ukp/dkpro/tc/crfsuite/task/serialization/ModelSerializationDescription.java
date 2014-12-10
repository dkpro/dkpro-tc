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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.serialization;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;

/**
 * Writes the model
 * 
 */
public class ModelSerializationDescription
    extends ExecutableTaskBase
    implements Constants
{

    @Discriminator
    protected List<Object> pipelineParameters;
    @Discriminator
    protected List<String> featureSet;
    
    private File outputFolder;
    
    public void setOutputFolder(File outputFolder) {
    	this.outputFolder = outputFolder;
    }
    
    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        trainAndStoreModel(aContext);
        
        SaveModelUtils.writeFeatureInformation(outputFolder, featureSet);
        SaveModelUtils.writeMetaCollectorInformation(aContext, outputFolder, featureSet);
        SaveModelUtils.writeModelAdapterInformation(outputFolder, CRFSuiteAdapter.class.getName());

    }
    
    private void trainAndStoreModel(TaskContext aContext) throws Exception
    {
        File train = new File(aContext.getStorageLocation(
                TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY).getPath()
                + "/" + CRFSuiteAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile));
        
        
        List<String> commandTrainModel = new ArrayList<String>();
        commandTrainModel.add(getExecutablePath());
        commandTrainModel.add("learn");
        commandTrainModel.add("-m");
        commandTrainModel.add(outputFolder.getAbsolutePath()+"/" + MODEL_CLASSIFIER);
        commandTrainModel.add(train.getAbsolutePath());
        
        Process process = new ProcessBuilder().inheritIO()
                .command(commandTrainModel).start();
        process.waitFor();        
    }

    private String getExecutablePath() throws Exception {
        return new RuntimeProvider(
                "classpath:/de/tudarmstadt/ukp/dkpro/tc/crfsuite/").getFile(
                "crfsuite").getAbsolutePath();
    }
}