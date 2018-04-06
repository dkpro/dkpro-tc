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
package org.dkpro.tc.ml.liblinear;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.ml.liblinear.core.LiblinearPredict;
import org.dkpro.tc.ml.liblinear.core.LiblinearTrain;
import org.junit.Before;
import org.junit.Test;

import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.SolverType;

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

        LiblinearTrain trainer = new LiblinearTrain();
        long modelBefore = model.length();
        Model liblinearModel = trainer.train(SolverType.L2R_L2LOSS_SVC, 100.0, 0.01, data, model);
        long modelAfter = model.length();
        assertTrue(modelBefore < modelAfter);

        LiblinearPredict predicter = new LiblinearPredict();
        List<Double[]> predict = predicter.predict(data, liblinearModel);

        // make sure that predicted and gold value is within the range of values found in the data
        // file
        for (Double[] v : predict) {
            assertTrue(v[0] >= 0 && v[0] < 32);
            assertTrue(v[1] >= 0 && v[1] < 32);
        }

    }

}
