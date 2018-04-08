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

package org.dkpro.tc.ml.xgboost;

import java.io.File;
import java.util.List;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatSerializeModelConnector;
import org.dkpro.tc.ml.xgboost.core.XgboostTrainer;

public class XgboostSerializeModelConnector
    extends LibsvmDataFormatSerializeModelConnector
    implements Constants
{

    @Override
    protected void trainModel(TaskContext aContext, File fileTrain) throws Exception
    {

        File model = new File(outputFolder, Constants.MODEL_CLASSIFIER);
        List<String> parameters = XgboostTestTask.getClassificationParameters(aContext,
                classificationArguments, learningMode);

        XgboostTrainer trainer = new XgboostTrainer();
        trainer.train(fileTrain, model, parameters);
    }

    @Override
    protected void writeAdapter() throws Exception
    {
        writeModelAdapterInformation(outputFolder, XgboostAdapter.class.getName());
    }

}