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
package org.dkpro.tc.ml.libsvm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.pear.util.FileUtil;
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
        LibsvmTrain trainer = new LibsvmTrain();
        
        long modelSizeBefore = model.length();
        File train = trainer.train(data, model, SvmType.C_SVM, KernelType.RadialBasis);
        long modelSizeAfter = model.length();
        assertTrue(modelSizeBefore < modelSizeAfter);
        
        LibsvmPrediction prediction = new LibsvmPrediction();
        File predictionFile = prediction.prediction(data, train);

        List<String> readLines = FileUtils.readLines(predictionFile, "utf-8");
        
        assertEquals(163, readLines.size());

    }

}
