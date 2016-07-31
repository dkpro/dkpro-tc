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
package org.dkpro.tc.core.task;

import java.io.File;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.SaveModelUtils;

public abstract class ModelSerializationTask
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator(name=DIM_FEATURE_SET)
    protected TcFeatureSet featureSet;
    @Discriminator(name=DIM_FEATURE_MODE)
    protected String featureMode;
    @Discriminator(name=DIM_LEARNING_MODE)
    protected String learningMode;
    @Discriminator(name=DIM_BIPARTITION_THRESHOLD)
    protected String threshold;

	protected File outputFolder;

	public void setOutputFolder(File outputFolder)
    {
        this.outputFolder = outputFolder;
        this.outputFolder.mkdirs();
    }

	
	public void writeModelConfiguration(TaskContext aContext, String mlAdapter) throws Exception{

	    SaveModelUtils.writeModelParameters(aContext, outputFolder, featureSet);
        SaveModelUtils.writeFeatureMode(outputFolder, featureMode);
        SaveModelUtils.writeLearningMode(outputFolder, learningMode);
        SaveModelUtils.writeModelAdapterInformation(outputFolder, mlAdapter);
        SaveModelUtils.writeCurrentVersionOfDKProTC(outputFolder);

	}
   
}