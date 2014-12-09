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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.savemodel;

import static de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask.META_KEY;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;

/**
 * Writes the model
 * 
 */
public class CRFSuiteModelSerializationDescription
    extends ExecutableTaskBase
    implements Constants
{

    private static final String CRFSUITE_MODEL = "crfsuite.model";

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
        commandTrainModel.add(outputFolder.getAbsolutePath()+"/" + CRFSUITE_MODEL);
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