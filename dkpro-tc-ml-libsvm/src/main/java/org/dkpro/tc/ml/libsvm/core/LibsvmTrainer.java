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

import org.dkpro.tc.ml.libsvm.api._Training;

public class LibsvmTrainer
{
    public File train(File data, File model, List<String> parameters) throws Exception
    {
        List<String> l = new ArrayList<>();
        for (String p : parameters) {
            l.add(p);
        }
        l.add(data.getAbsolutePath());
        l.add(model.getAbsolutePath());

        _Training ltm = new _Training();
        ltm.run(l.toArray(new String[0]));

        return model;
    }

    public File train(File data, File model, SvmType type, KernelType kernel,
            String... miscParameters)
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

        return model;
    }
}
