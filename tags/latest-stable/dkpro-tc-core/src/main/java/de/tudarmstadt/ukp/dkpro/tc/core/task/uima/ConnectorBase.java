/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.core.task.uima;

import org.apache.uima.fit.component.JCasAnnotator_ImplBase;

/**
 * Defines names of parameters that are defined in more than one task should be defined here
 */
public abstract class ConnectorBase
    extends JCasAnnotator_ImplBase
{
    /**
     * Array of feature extractors to be used
     */
    public static final String PARAM_FEATURE_EXTRACTORS = "featureExtractors";

    /**
     * The data writer class to be used for writing features
     */
    public static final String PARAM_DATA_WRITER_CLASS = "dataWriterClass";

    /**
     * The learning mode, e.g. single-label, multi-label or regression
     */
    public static final String PARAM_LEARNING_MODE = "learningMode";

    /**
     * The feature mode, e.g. document, pair, unit, or sequence
     */
    public static final String PARAM_FEATURE_MODE = "featureMode";

    /**
     * Switches to developer mode with less restrictive settings. For example, using document level
     * feature extractors in unit mode will not cause an exception
     */
    public static final String PARAM_DEVELOPER_MODE = "developerMode";
}