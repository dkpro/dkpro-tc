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
package org.dkpro.tc.ml.crfsuite.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.ml.base.TcPredictor;
import org.dkpro.tc.ml.base.TcTrainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CrfSuiteBackendTest
{
    File data;
    File modelOut;

    @Before
    public void setup() throws Exception
    {
        data = new File("src/test/resources/crfTestfile/featureFile.txt");
        modelOut = FileUtil.createTempFile("crfsuiteModelOut", ".txt");
    }

    @After
    public void tearDown()
    {
        modelOut.delete();
    }

    @Test
    public void testPrediction() throws Exception
    {
        trainModel();

        TcPredictor predict = new CrfSuitePredictor();
        List<String> predictions = predict.predict(data, modelOut);

        assertTrue(predictions != null && !predictions.isEmpty());

        assertEquals(167, predictions.size());

        for (String s : predictions) {
            if(s.isEmpty()) {
                continue;
            }
            assertTrue(s.matches("[A-Za-z]+\t[A-Za-z]+"));
        }

    }

    private void trainModel() throws Exception
    {
        
        long modelBefore = modelOut.length();
        
        TcTrainer train = new CrfSuiteTrainer();
        
        List<String> parameters = new ArrayList<>();
        parameters.add(CrfSuiteAlgo.ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTORS.toString());
        
        train.train(data, modelOut, parameters);
        
        long modelAfter = modelOut.length();
        
        assertTrue(modelBefore < modelAfter);
    }

}
