/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.ml.libsvm.core;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.ml.base.TcPredictor;
import org.dkpro.tc.ml.libsvm.api._Prediction;

import libsvm.svm;
import libsvm.svm_model;

public class LibsvmPredictor
    implements TcPredictor
{
    @Override
    public List<String> predict(File data, File model) throws Exception
    {
        File predTmp = FileUtil.createTempFile("libsvmPrediction", ".txt");
        predTmp.deleteOnExit();

        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(predTmp));
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(new FileInputStream(data), UTF_8))) {

            svm_model svmModel = svm.svm_load_model(model.getAbsolutePath());

            _Prediction predictor = new _Prediction();
            predictor.predict(input, output, svmModel, 0);
        }

        List<String> predictions = FileUtils.readLines(predTmp, UTF_8);

        return predictions;
    }

}
