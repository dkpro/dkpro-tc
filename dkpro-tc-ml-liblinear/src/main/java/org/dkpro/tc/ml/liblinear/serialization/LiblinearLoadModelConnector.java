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

package org.dkpro.tc.ml.liblinear.serialization;

import java.io.File;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.io.libsvm.serialization.LibsvmDataFormatLoadModelConnector;
import org.dkpro.tc.ml.liblinear.LiblinearTestTask;
import org.dkpro.tc.ml.liblinear.core.LiblinearPredictor;

import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;

public class LiblinearLoadModelConnector
    extends LibsvmDataFormatLoadModelConnector
{
    protected Model liblinearModel;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            liblinearModel = Linear.loadModel(new File(tcModelLocation, MODEL_CLASSIFIER));
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    protected File runPrediction(File infile) throws Exception
    {

        File tmp = File.createTempFile("libLinearePrediction", ".txt");
        tmp.deleteOnExit();

        LiblinearPredictor predicter = new LiblinearPredictor();
        List<String> predict = predicter.predict(infile, liblinearModel);
        LiblinearTestTask.writePredictions(tmp, predict, false);

        return tmp;
    }

}