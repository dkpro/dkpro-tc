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
package org.dkpro.tc.ml.base;

import java.io.File;
import java.util.List;

public interface TcPredictor
{
    /**
     * Instantiates a classifier using the provided file path to the model and returns the
     * prediction on the provided dataset.
     * 
     * @param data
     *          The data that shall be predicted
     * @param model
     *          The model that shall be loaded
     * @return
     *          the predictions of the data
     * @throws Exception
     *          In case of an error
     */
    List<String> predict(File data, File model) throws Exception;
}
