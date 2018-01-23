/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.ml.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;

public abstract class ShallowLearningExperiment_ImplBase
    extends Experiment_ImplBase
{
    protected TcShallowLearningAdapter mlAdapter;

    Log log = LogFactory.getLog(ShallowLearningExperiment_ImplBase.class);

    @Override
    public void initialize(TaskContext aContext)
    {
        super.initialize(aContext);
       
    }
 
    public void setMachineLearningAdapter(Class<? extends TcShallowLearningAdapter> mlAdapter)
        throws IllegalArgumentException
    {
        try {
            this.mlAdapter = mlAdapter.newInstance();
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}