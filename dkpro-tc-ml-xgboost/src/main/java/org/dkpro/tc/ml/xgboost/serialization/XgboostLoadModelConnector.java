/**
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
 */
package org.dkpro.tc.ml.xgboost.serialization;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatLoadModelConnector;
import org.dkpro.tc.ml.xgboost.core.XgboostPredictor;

public class XgboostLoadModelConnector
    extends LibsvmDataFormatLoadModelConnector
{

    @Override
    protected File runPrediction(File testFile) throws Exception
    {

        File model = new File(tcModelLocation, Constants.MODEL_CLASSIFIER);
        
        XgboostPredictor predictor = new XgboostPredictor();
        List<String> predict = predictor.predict(testFile, model);
        
        File predictions = FileUtil.createTempFile("xgboostPrediction", ".txt");
        FileUtils.writeLines(predictions, "utf-8", predict);

        predictions.deleteOnExit();
        return predictions;
    }

}