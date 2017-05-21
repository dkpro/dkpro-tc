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
 * Basic constants that are used throughout the project
 */
public interface DeepLearningConstants
{

    String FILENAME_TOKEN = "occurringToken.txt";
    

    String LM_DOCUMENT_TO_LABEL = "document2label";
    String LM_SEQUENCE_TO_SEQUENCE_OF_LABELS = "sequence2sequenceOfLabels";

    String FILENAME_PRUNED_EMBEDDING = "prunedEmbedding.txt";

	String FILENAME_INSTANCE_VECTOR = "instanceVectors.txt";
	String FILENAME_OUTCOME_VECTOR = "outcomeVectors.txt";

	String FILENAME_INSTANCE_MAPPING = "instanceMapping.txt";
	String FILENAME_OUTCOME_MAPPING = "outcomeMapping.txt";

    String FILENAME_MAXIMUM_LENGTH = "maxLen.txt";

    String DIM_MAXIMUM_LENGTH = "dimMaximumLength";
    String DIM_PRETRAINED_EMBEDDINGS = "dimEmbedding";
    String DIM_PYTHON_INSTALLATION = "dimPythonPath";
    String DIM_USER_CODE = "dimPythonUserCode";
    String FILENAME_TARGET_ID_TO_INDEX = "targetName2Index.txt";
       
}