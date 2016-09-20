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
package org.dkpro.tc.ml.keras;

import java.util.Collection;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.task.ModelSerializationTask;

public class KerasAdapter
    implements TCMachineLearningAdapter
{

    @Override
    public TaskBase getTestTask()
    {
        return null;
    }

    @Override
    public Class<? extends DataWriter> getDataWriterClass()
    {
        return null;
    }

    @Override
    public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass()
    {
        return null;
    }

    @Override
    public Class<? extends ReportBase> getOutcomeIdReportClass()
    {
        return null;
    }

    @Override
    public Class<? extends ReportBase> getBatchTrainTestReportClass()
    {
        return null;
    }

    @Override
    public <T extends DimensionBundle<Collection<String>>> T getFoldDimensionBundle(String[] files,
            int folds)
    {
        return null;
    }

    @Override
    public String getFrameworkFilename(AdapterNameEntries name)
    {
        return null;
    }

    @Override
    public Class<? extends ModelSerializationTask> getSaveModelTask()
    {
        return null;
    }

    @Override
    public String getFeatureStore()
    {
        return null;
    }

}
