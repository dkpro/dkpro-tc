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
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.ml.base.TcPredictor;
import org.dkpro.tc.ml.base.TcTrainer;
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
        TcTrainer trainer = new SvmHmmTrainer();
        List<String> parameters = new ArrayList<>();
        parameters.add("-x");
        parameters.add("30");
        trainer.train(data, model, parameters);

    }

    @Test
    public void testSvmHmm() throws Exception
    {
        List<String> parameters = new ArrayList<>();
        parameters.add("-c");
        parameters.add("1000");
        
        TcTrainer trainer = new SvmHmmTrainer();

        long before = model.length();
        trainer.train(data, model, parameters);
        long after = model.length();

        assertTrue(before < after);

        File tmpFile = FileUtil.createTempFile("svmHmm", ".txt");
        tmpFile.deleteOnExit();
        
        TcPredictor predictor = new SvmHmmPredictor();
        List<String> predictions = predictor.predict(data, model);
        assertEquals(31, predictions.size());
    }

}
