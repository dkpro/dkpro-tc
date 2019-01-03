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
package org.dkpro.tc.core.task;

public enum TcTaskType
{
    // Facade task that wraps a machine learning adapter task
    FACADE_TASK,

    // Shallow tasks
    INIT_TRAIN, INIT_TEST, COLLECTION, META, FEATURE_EXTRACTION_TRAIN, FEATURE_EXTRACTION_TEST, MACHINE_LEARNING_ADAPTER, EVALUATION, CROSS_VALIDATION, SERIALIZATION_TASK, NO_TYPE,

    // Deep tasks
    PREPARATION, VECTORIZATION_TRAIN, VECTORIZATION_TEST, EMBEDDING;

}
