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
package org.dkpro.tc.ml.liblinear.core;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.ml.base.TcPredictor;
import org.dkpro.tc.ml.base.TcTrainer;
import org.junit.Before;
import org.junit.Test;

public class LiblinearBackendTest
{
    File data;
    File model;

    @Before
    public void setup() throws IOException
    {
        data = new File("src/test/resources/data/featureFile.txt");
        model = FileUtil.createTempFile("liblinearModel", ".model");
    }

    @Test
    public void testTraining() throws Exception
    {

        TcTrainer trainer = new LiblinearTrainer();
        long modelBefore = model.length();
        
        List<String> parameters = new ArrayList<>();
        parameters.add("-c");
        parameters.add("100.0");
        
        trainer.train(data, model, parameters);
        long modelAfter = model.length();
        assertTrue(modelBefore < modelAfter);

        TcPredictor predicter = new LiblinearPredictor();
        List<String> predict = predicter.predict(data, model);

        // make sure that predicted and gold value is within the range of values found in the data
        // file
        for (String v : predict) {
            Double d = Double.parseDouble(v);
            assertTrue(d >= 0 && d < 32);
        }

    }

}
