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

package org.dkpro.tc.ml.liblinear.serialization;

import java.io.File;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.serialization.LibsvmDataFormatSerializeModelConnector;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.liblinear.LiblinearTestTask;
import org.dkpro.tc.ml.liblinear.core.LiblinearTrainer;

import de.bwaldvogel.liblinear.SolverType;

public class LiblinearSerializeModelConnector
    extends LibsvmDataFormatSerializeModelConnector
    implements Constants
{

    @Override
    protected void trainModel(TaskContext aContext, File fileTrain) throws Exception
    {
        SolverType solver = LiblinearTestTask.getSolver(classificationArguments);
        double C = LiblinearTestTask.getParameterC(classificationArguments);
        double eps = LiblinearTestTask.getParameterEpsilon(classificationArguments);

        File modelLocation = new File(outputFolder, MODEL_CLASSIFIER);
        LiblinearTrainer trainer = new LiblinearTrainer();
        trainer.train(solver, C, eps, fileTrain, modelLocation);
    }

    @Override
    protected void writeAdapter() throws Exception
    {
        writeModelAdapterInformation(outputFolder, LiblinearAdapter.class.getName());
    }
}