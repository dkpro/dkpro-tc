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
package org.dkpro.tc.core;

/**
 * Parameter used that are expected by the DeepLearning Python code to be handled
 */
public interface PythonConstants
{
    static final String SEED = "--seed";
    static final String TRAIN_DATA  = "--trainData";
    static final String TRAIN_OUTCOME = "--trainOutcome";
    static final String TEST_DATA = "--testData";
    static final String TEST_OUTCOME = "--testOutcome";
    static final String EMBEDDING = "--embedding";
    static final String MAX_LEN = "--maxLen";
    static final String PREDICTION_OUT = "--predictionOut";
}