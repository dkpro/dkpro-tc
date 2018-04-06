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
package org.dkpro.tc.ml.crfsuite.core;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.uima.pear.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CrfSuiteTrainingTest
{
    File data;
    File modelOut;

    @Before
    public void setup() throws IOException
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
    public void testTrain() throws Exception
    {
        train("ap");
        train("lbfgs");
        train("l2sgd");
        train("arow");

    }
    
    private void train(String algo) throws Exception
    {
        long before = modelOut.length();

        CrfSuiteTrain trainer = new CrfSuiteTrain();
        trainer.train(algo, Collections.emptyList(), data, modelOut);

        long after = modelOut.length();

        assertTrue(after > before);
        modelOut.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrainUnknownAlgo() throws Exception {
        CrfSuiteTrain trainer = new CrfSuiteTrain();
        trainer.train("abc", Collections.emptyList(), data, modelOut);
    }
}
