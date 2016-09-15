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
package org.dkpro.tc.ml.report;

import java.util.Map;

import org.dkpro.tc.ml.Experiment_ImplBase;

public class ReportUtil
{
    public static TcTaskType getTaskType(Map<String, String> map){
        
        String string = map.get(Experiment_ImplBase.TC_TASK_TYPE);
        if(string == null){
            return TcTaskType.NO_TYPE;
        }
        
        return TcTaskType.valueOf(string);
    }

}
