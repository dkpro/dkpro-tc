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
package org.dkpro.tc.ml.svmhmm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SvmHmmBackendTest
{
    File data;
    File model;

    @Before
    public void setup() throws IOException
    {
        data = new File("src/test/resources/data/featureFile.txt");
        model = FileUtil.createTempFile("svmhmmModel", ".model");
    }

    @After
    public void tearDown()
    {
        model.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSvmHmmException() throws Exception
    {
        SvmHmmTrainer trainer = new SvmHmmTrainer();
        trainer.train(data, model, "-x", "30");

    }

    @Test
    public void testSvmHmm() throws Exception
    {
        SvmHmmTrainer trainer = new SvmHmmTrainer();

        long before = model.length();
        File trainModel = trainer.train(data, model, "-c", "1000");
        long after = model.length();

        assertTrue(before < after);

        File tmpFile = FileUtil.createTempFile("svmHmm", ".txt");
        tmpFile.deleteOnExit();
        
        SvmHmmPredictor predictor = new SvmHmmPredictor();
        List<String> predictions = predictor.predict(data, trainModel);
        assertEquals(31, predictions.size());
    }

}
