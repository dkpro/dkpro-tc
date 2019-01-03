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
package org.dkpro.tc.core;

/**
 * Basic constants that are used throughout the project
 */
public interface DeepLearningConstants
{
    String FILENAME_TOKEN = "occurringToken.txt";

    String FILENAME_PRUNED_EMBEDDING = "prunedEmbedding.txt";

    String FILENAME_INSTANCE_VECTOR = "instanceVectors.txt";
    String FILENAME_OUTCOME_VECTOR = "outcomeVectors.txt";

    String FILENAME_INSTANCE_MAPPING = "instanceMapping.txt";
    String FILENAME_OUTCOME_MAPPING = "outcomeMapping.txt";
    String FILENAME_VOCABULARY = "vocabulary.txt";
    String FILENAME_OUTCOMES = "outcomes.txt";

    String FILENAME_MAXIMUM_LENGTH = "maxLen.txt";
    String FILENAME_PREDICTION_OUT = "prediction.txt";

    String DIM_MAXIMUM_LENGTH = "dimMaximumLength";
    String DIM_PRETRAINED_EMBEDDINGS = "dimEmbedding";

    /**
     * Path to the Python installation which should be used to execute the Python code.
     */
    String DIM_PYTHON_INSTALLATION = "dimPythonPath";

    /**
     * File that holds the mapping of the input unit, i.e. which document, word or sequence to the
     * position within the processing sequence. This file assumes that the later result output is
     * FIFO and thus, the first result is the first unit of the processing pipeline, which allows
     * later on to identify which unit/target was classified
     */
    String FILENAME_TARGET_ID_TO_INDEX = "targetName2Index.txt";

    /**
     * File that stores the parameter list the user code is expected to read and configure the user
     * code accordingly.
     */
    String FILENAME_USERCODE_PARAMETERS = "userCodeParameters.txt";

    /**
     * When creating the vectorized representation of the input data one can directly vectorize and
     * output integer-mapped vectors. Depending on the framework used this allows reducing the
     * boiler plate code for preparing the vectors.
     */
    String DIM_VECTORIZE_TO_INTEGER = "dimVectorizeToInteger";

    /**
     * For specifying a certain seed initialization value. If not provided, this value is
     * initialized with a fixed constant value which is passed to the deep learning code. Depending
     * on the framework used, the user code is responsible to call the initialization of the
     * framework with this value.
     */
    String DIM_SEED_VALUE = "randomSeed";

    /**
     * For specifying the working memory. Depending on the framework used, the user code is
     * responsible to call the initialization of the framework with this value.
     */
    String DIM_RAM_WORKING_MEMORY = "workingMemory";

    /**
     * Words which are not contained in the embedding file will be removed. The embedding, thus,
     * defines which words will remain. Small embeddings will consequently lead to a drastically
     * reduction of the vocabulary size if this parameter is used.
     */
    String DIM_USE_ONLY_VOCABULARY_COVERED_BY_EMBEDDING = "useOnlyVocabularyContainedInEmbedding";

    /**
     * Path to tab-separated lookup lists
     */
    String DIM_DICTIONARY_PATHS = "dictionaryPaths";

}