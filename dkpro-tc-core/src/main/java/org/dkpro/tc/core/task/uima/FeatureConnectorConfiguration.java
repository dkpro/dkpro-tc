/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.core.task.uima;

import java.io.File;
import java.util.List;

import org.apache.uima.resource.ExternalResourceDescription;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

public class FeatureConnectorConfiguration
{

    boolean applyWeighting;
    List<String> featureFilters;
    boolean isTesting;
    boolean developerMode;
    boolean useSparseFeatures;

    String featureMode;
    String name;
    File outputDir;
    String learningMode;
    File trainFolder;
    boolean setInstanceId;

    public boolean isApplyWeighting()
    {
        return applyWeighting;
    }

    public void setApplyWeighting(boolean applyWeighting)
    {
        this.applyWeighting = applyWeighting;
    }

    public List<String> getFeatureFilters()
    {
        return featureFilters;
    }

    public void setFeatureFilters(List<String> featureFilters)
    {
        this.featureFilters = featureFilters;
    }

    public boolean isTesting()
    {
        return isTesting;
    }

    public void setTesting(boolean isTesting)
    {
        this.isTesting = isTesting;
    }

    public boolean isDeveloperMode()
    {
        return developerMode;
    }

    public void setDeveloperMode(boolean developerMode)
    {
        this.developerMode = developerMode;
    }

    public boolean isUseSparseFeatures()
    {
        return useSparseFeatures;
    }

    public void setUseSparseFeatures(boolean useSparseFeatures)
    {
        this.useSparseFeatures = useSparseFeatures;
    }

    public String getFeatureMode()
    {
        return featureMode;
    }

    public void setFeatureMode(String featureMode)
    {
        this.featureMode = featureMode;
    }

    public String getName()
    {
        return name;
    }

    public void setDataWriter(String name)
    {
        this.name = name;
    }

    public File getOutputDir()
    {
        return outputDir;
    }

    public void setOutputDir(File outputDir)
    {
        this.outputDir = outputDir;
    }

    public String getLearningMode()
    {
        return learningMode;
    }

    public void setLearningMode(String learningMode)
    {
        this.learningMode = learningMode;
    }

    /**
     * Carries the location of the training folder into the test task. This file pointer is
     * identical to the output directory during the training task
     */
    public void setTrainFolder(File trainFile)
    {
        this.trainFolder = trainFile;
    }

    public File getTrainFolder()
    {
        return trainFolder;
    }

    public void setInstanceId(boolean b)
    {
        this.setInstanceId = b;

    }

    public boolean getSetInstanceId()
    {
        return setInstanceId;
    }

}
