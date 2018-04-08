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
package org.dkpro.tc.ml.xgboost.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class XgboostTrainer extends Xgboost
{
    public XgboostTrainer()
    {
        //Groovy
    }

    /**
     * Trains a model with Xgboost
     * 
     * @param data
     *          The training data file
     * @param model
     *         File descriptor for the location at which the model shall be stored
     * @param parameters
     *          The parametrization
     * @return
     *         file path to the trained model
     * @throws Exception
     *      In case of an error
     */
    public File train(File data,
            File model, List<String> parameters)
        throws Exception
    {
        File trainConfiguration = writeTrainConfigurationFile(parameters, data, model);
        
        List<String> command = new ArrayList<>();
        command.add(flipBackslash(getExecutable().getAbsolutePath()));
        command.add(flipBackslash(trainConfiguration.getAbsolutePath()));

        runCommand(command);

        return model;
    }
    
    public File writeTrainConfigurationFile(List<String> parameters, File data, File model) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append("task=train" + "\n");
        sb.append("data=\"" + flipBackslash(data.getAbsolutePath()) + "\"" + "\n");
        sb.append("model_out=\"" + flipBackslash(model.getAbsolutePath()) + "\"" + "\n");

        for (String p : parameters) {
            sb.append(p + "\n");
        }
        
        File config = new File(getExecutable().getParentFile(), "train.conf");
        config.deleteOnExit();
        FileUtils.writeStringToFile(config, sb.toString(), "utf-8");
        return config;
    }
}
