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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class XgboostBackendTest
{
    File data;
    File model;

    @Before
    public void setup() throws IOException
    {
        data = new File("src/test/resources/data/featureFile.txt");
        model = FileUtil.createTempFile("xgboostModel", ".model");
    }

    @After
    public void tearDown()
    {
        model.delete();
    }

    @Test
    public void testTraining() throws Exception
    {
        train();
        predict();
    }

    private void predict() throws Exception
    {
        XgboostPredictor predictor = new XgboostPredictor();
        List<String> predict = predictor.predict(data, model);

        assertTrue(!predict.isEmpty());

        for (String l : predict) {
            int parseInt = Integer.parseInt(l);
            assertTrue(parseInt >= 0 && parseInt <= 31);
        }
    }

    private void train() throws Exception
    {
        XgboostTrainer trainer = new XgboostTrainer();
        long sizeBefore = model.length();
        File train = trainer.train(
                Arrays.asList(new String[] { "objective=multi:softmax", "num_class=32" }), data,
                model);
        long sizeAfter = model.length();

        assertTrue(train != null && train.exists());
        assertTrue(sizeBefore < sizeAfter);
    }

}
