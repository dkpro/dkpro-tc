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

public class LibsvmBackendTest
{
    File data;
    File model;

    @Before
    public void setup() throws IOException
    {
        data = new File("src/test/resources/data/featureFile.txt");
        model = FileUtil.createTempFile("libsvm", ".model");
    }
    
    @After
    public void cleanup() {
        model.delete();
    }

    @Test
    public void testTraining() throws Exception
    {
        TcTrainer trainer = new LibsvmTrainer();
        
        long modelSizeBefore = model.length();
        
        List<String> parameters = new ArrayList<>();
        parameters.add("-s");
        parameters.add(SvmType.C_SVM.toString());
        parameters.add("-t");
        parameters.add(KernelType.RadialBasis.toString());
        
        trainer.train(data, model, parameters);
        long modelSizeAfter = model.length();
        assertTrue(modelSizeBefore < modelSizeAfter);
        
        TcPredictor prediction = new LibsvmPredictor();
        List<String> predictions = prediction.predict(data, model);

        assertEquals(163, predictions.size());

    }

}
