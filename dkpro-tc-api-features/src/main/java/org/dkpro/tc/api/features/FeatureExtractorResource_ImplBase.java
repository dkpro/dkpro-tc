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
package org.dkpro.tc.api.features;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

/**
 * Abstract base class for all feature extractors.
 * 
 * Feature extractors are implemented as UIMA external resources.
 */
public abstract class FeatureExtractorResource_ImplBase
    extends Resource_ImplBase
{
    public static final String PARAM_UNIQUE_EXTRACTOR_NAME = "uniqueFeatureExtractorName";
    @ConfigurationParameter(name = PARAM_UNIQUE_EXTRACTOR_NAME, mandatory = true)
    protected String featureExtractorName;
}