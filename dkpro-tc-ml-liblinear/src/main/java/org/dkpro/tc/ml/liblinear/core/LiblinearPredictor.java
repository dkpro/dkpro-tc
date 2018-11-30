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
package org.dkpro.tc.ml.liblinear.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.tc.ml.base.TcPredictor;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Problem;

public class LiblinearPredictor
    implements TcPredictor
{
    public List<String> predict(File data, Model model) throws Exception
    {
        List<String> predictions = new ArrayList<>();

        Problem test = Problem.readFromFile(data, 1.0);
        Feature[][] testInstances = test.x;
        for (int i = 0; i < testInstances.length; i++) {
            Feature[] instance = testInstances[i];
            Double prediction = Linear.predict(model, instance);
            predictions.add(prediction.toString());
        }

        return predictions;
    }

    @Override
    public List<String> predict(File data, File model) throws Exception
    {
        Model loadModel = Linear.loadModel(model);
        return predict(data, loadModel);
    }

}
