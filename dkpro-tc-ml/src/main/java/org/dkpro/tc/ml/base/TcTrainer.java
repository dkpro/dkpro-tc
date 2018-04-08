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

public interface TcTrainer
{
    /**
     * Trains a classifier
     * 
     * @param data
     *          File to the training data in the classifier's data format
     * @param model
     *          Location to which the model is written
     * @param parameters
     *          The classifier-specific parametrisation
     * @throws Exception
     *          In case an error occurs
     */
    void train(File data, File model, List<String> parameters) throws Exception;
}
