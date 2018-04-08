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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.tc.ml.base.TcTrainer;
import org.dkpro.tc.ml.libsvm.api._Training;

public class LibsvmTrainer
    implements TcTrainer
{
    @Override
    public void train(File data, File model, List<String> parameters) throws Exception
    {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }

        SvmType svm = getSVMType(parameters);
        KernelType kernel = getKernelType(parameters);

        List<String> miscParameters = getRemaininParameters(parameters);

        train(data, model, svm, kernel, miscParameters);
    }

    private List<String> getRemaininParameters(List<String> parameters)
    {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i += 2) {
            if (parameters.get(i).equals("-s") || parameters.get(i).equals("-t")) {
                continue;
            }
            out.add(parameters.get(i));
            out.add(parameters.get(i + 1));
        }

        return out;
    }

    private KernelType getKernelType(List<String> parameters)
    {
        for (int i = 0; i < parameters.size(); i += 2) {
            if (parameters.get(i).equals("-t")) {
                return KernelType.getByName(parameters.get(i + 1));
            }
        }

        return KernelType.getByName("2");
    }

    private SvmType getSVMType(List<String> parameters)
    {
        for (int i = 0; i < parameters.size(); i += 2) {
            if (parameters.get(i).equals("-s")) {
                return SvmType.getByName(parameters.get(i + 1));
            }
        }

        return SvmType.getByName("0");
    }

    public void train(File data, File model, SvmType type, KernelType kernel,
            List<String> miscParameters)
        throws Exception
    {
        List<String> l = new ArrayList<>();

        l.add("-s");
        l.add(type.toString());
        l.add("-t");
        l.add(kernel.toString());

        for (String p : miscParameters) {
            l.add(p);
        }
        l.add(data.getAbsolutePath());
        l.add(model.getAbsolutePath());

        _Training ltm = new _Training();
        ltm.run(l.toArray(new String[0]));
    }
}
