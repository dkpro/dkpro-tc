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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.ml.liblinear.core.LiblinearTrain;
import org.junit.Before;
import org.junit.Test;

import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.SolverType;

public class LiblinearTrainingTest
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
    public void testTraining() throws IOException, InvalidInputDataException
    {

        long before = model.length();

        LiblinearTrain trainer = new LiblinearTrain();
        Model liblinearModel = trainer.train(SolverType.L2R_L2LOSS_SVC, 100.0, 0.01, data, model);

        long after = model.length();

        assertNotNull(liblinearModel);
        assertTrue(before < after);

    }

}
