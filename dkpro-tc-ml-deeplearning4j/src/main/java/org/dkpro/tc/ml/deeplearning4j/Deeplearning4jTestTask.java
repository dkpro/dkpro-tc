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
package org.dkpro.tc.ml.deeplearning4j;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;

public class Deeplearning4jTestTask
    extends ExecutableTaskBase
    implements Constants
{
    public static final String PREDICTION_FILE = "prediction.txt";
            
    @Discriminator(name = DeepLearningConstants.DIM_PYTHON_USER_CODE)
    private String userCode;
    
    @Discriminator(name = DeepLearningConstants.DIM_MAXIMUM_LENGTH)
    private Integer maximumLength;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
       throw new UnsupportedOperationException("Not yet implemented");
    }

}
