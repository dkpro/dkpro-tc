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
import java.io.IOException;
import java.util.List;

import org.dkpro.tc.ml.base.TcTrainer;

import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LiblinearTrainer
    implements TcTrainer
{
    @Override
    public void train(File data, File model, List<String> parameters)
        throws IOException, InvalidInputDataException
    {
        SolverType s = getSolverFromParameters(parameters);
        double c = getCvalue(parameters);
        double eps = getEps(s, parameters);

        train(s, c, eps, data, model);
    }

    private double getEps(SolverType s, List<String> parameters)
    {
        for (int i = 0; i < parameters.size(); i += 2) {

            if (parameters.get(i).equals("-e")) {
                return Double.parseDouble(parameters.get(i + 1));
            }

        }

        if (s.getId() == 1 || s.getId() == 3 || s.getId() == 4 || s.getId() == 7) {
            return 0.1;
        }

        return 0.01;
    }

    private double getCvalue(List<String> parameters)
    {
        for (int i = 0; i < parameters.size(); i += 2) {

            if (parameters.get(i).equals("-c")) {
                return Double.parseDouble(parameters.get(i + 1));
            }

        }
        return 1.0;
    }

    private SolverType getSolverFromParameters(List<String> parameters)
    {
        for (int i = 0; i < parameters.size(); i += 2) {

            if (parameters.get(i).equals("-s")) {
                return SolverType.getById(Integer.parseInt(parameters.get(i + 1)));
            }

        }

        return SolverType.getById(1);
    }

    public void train(SolverType solver, double c, double eps, File data, File model)
        throws IOException, InvalidInputDataException
    {
        Problem train = Problem.readFromFile(data, 1.0);
        Linear.setDebugOutput(null);
        Parameter parameter = new Parameter(solver, c, eps);
        Model trainedModel = Linear.train(train, parameter);
        trainedModel.save(model);
    }

}
