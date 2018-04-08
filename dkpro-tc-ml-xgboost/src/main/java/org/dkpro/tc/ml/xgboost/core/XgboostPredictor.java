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
import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.ml.base.TcPredictor;

public class XgboostPredictor extends Xgboost implements TcPredictor
{
    public XgboostPredictor()
    {
        //Groovy
    }

    @Override
    public List<String> predict(File data, File model) throws Exception
    {
        File tmpPredictionOut = FileUtil.createTempFile("xgboostPredictionOut", ".txt");
        tmpPredictionOut.deleteOnExit();
        
        File config = buildTestConfigFile(data, model, tmpPredictionOut);
        
        List<String> command = new ArrayList<>();
        command.add(flipBackslash(getExecutable().getAbsolutePath()));
        command.add(flipBackslash(config.getAbsolutePath()));
        
        runCommand(command);
        
        List<String> predictions = FileUtils.readLines(tmpPredictionOut, "utf-8");
        
        return predictions;
    }

 
    public File buildTestConfigFile(File data, File model, File predictionOut) throws Exception
    {
        
        StringBuilder sb = new StringBuilder();
        sb.append("task=pred" + "\n");
        sb.append("test:data=\"" + flipBackslash(data.getAbsolutePath()) + "\"" + "\n");
        sb.append("model_in=\"" + flipBackslash(model.getAbsolutePath()) + "\"" + "\n");
        sb.append("name_pred=\"" + flipBackslash(predictionOut.getAbsolutePath()) + "\"" + "\n");
        
        File config = new File(getExecutable().getParentFile(), "test.conf");
        config.deleteOnExit();
        FileUtils.writeStringToFile(config, sb.toString(), "utf-8");
        return config;
    }
}
