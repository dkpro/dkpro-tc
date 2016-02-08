/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.core.task;

import java.io.File;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.util.SaveModelUtils;

public abstract class ModelSerializationTask
    extends ExecutableTaskBase
    implements Constants
{
    
    @Discriminator
    protected List<Object> pipelineParameters;
    @Discriminator
    protected List<String> featureSet;
    @Discriminator
    protected String featureMode;
    @Discriminator
    protected String learningMode;
    @Discriminator
    protected String threshold;

	protected File outputFolder;

	public void setOutputFolder(File outputFolder)
    {
        this.outputFolder = outputFolder;
        this.outputFolder.mkdirs();
    }

	
	public void writeModelConfiguration(TaskContext aContext, String mlAdapter) throws Exception{
		
		// bipartition threshold is optional
		threshold = "0.5";
		
        SaveModelUtils.writeFeatureInformation(outputFolder, featureSet);
        SaveModelUtils.writeFeatureClassFiles(outputFolder, featureSet);
        SaveModelUtils.writeModelParameters(aContext, outputFolder, featureSet, pipelineParameters);
        SaveModelUtils.writeModelAdapterInformation(outputFolder, mlAdapter);
        SaveModelUtils.writeCurrentVersionOfDKProTC(outputFolder);
        SaveModelUtils.writeFeatureMode(outputFolder, featureMode);
        SaveModelUtils.writeLearningMode(outputFolder, learningMode);
        SaveModelUtils.writeBipartitionThreshold(outputFolder, threshold);
	}
   
}