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

package org.dkpro.tc.ml.libsvm.serialization;

import java.io.BufferedReader;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.apache.uima.UimaContext;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.io.libsvm.serialization.LibsvmDataFormatLoadModelConnector;
import org.dkpro.tc.ml.libsvm.api._Prediction;

import libsvm.svm;
import libsvm.svm_model;

public class LibsvmLoadModelConnector
    extends LibsvmDataFormatLoadModelConnector
{

    private svm_model model;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            model = svm
                    .svm_load_model(new File(tcModelLocation, MODEL_CLASSIFIER).getAbsolutePath());
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    protected File runPrediction(File tempFile) throws Exception
    {
        File prediction = FileUtil.createTempFile("libsvmPrediction", ".libsvm");
        prediction.deleteOnExit();

        _Prediction predictor = new _Prediction();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(tempFile), UTF_8));
                DataOutputStream output = new DataOutputStream(new FileOutputStream(prediction))) {
            predictor.predict(r, output, model, 0);
        }

        return prediction;
    }
}