/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.core;

/**
 * Parameter used that are expected by the DeepLearning Python code to be handled
 */
public interface PythonConstants
{

    String SEED = "--seed";
    String TRAIN_DATA  = "--trainData";
    String TRAIN_OUTCOME = "--trainOutcome";
    String TEST_DATA = "--testData";
    String TEST_OUTCOME = "--testOutcome";
    String EMBEDDING = "--embedding";
    String MAX_LEN = "--maxLen";
    String PREDICTION_OUT = "--predictionOut";
    

}