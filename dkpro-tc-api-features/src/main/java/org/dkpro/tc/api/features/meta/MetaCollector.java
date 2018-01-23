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
package org.dkpro.tc.api.features.meta;

import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Interface for meta collectors that collect document-level information for feature extractors.
 */
public abstract class MetaCollector
    extends JCasAnnotator_ImplBase
{
    /**
     * Each feature extractor needs a unique id to know which feature extractors and which meta collector correspond to each other.
     * This value has to be set for extractors which do net use meta collection, too. 
     */
    public static final String PARAM_UNIQUE_EXTRACTOR_NAME = FeatureExtractorResource_ImplBase.PARAM_UNIQUE_EXTRACTOR_NAME;
    @ConfigurationParameter(name = PARAM_UNIQUE_EXTRACTOR_NAME, mandatory = true)
    protected String featureExtractorName;
}
