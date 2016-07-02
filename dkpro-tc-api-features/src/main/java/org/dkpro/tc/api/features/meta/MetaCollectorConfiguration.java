/*******************************************************************************
 * Copyright 2016
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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

public class MetaCollectorConfiguration
{
    public final AnalysisEngineDescription descriptor;
    public final Map<String, String> collectorOverrides = new HashMap<>();
    public final Map<String, String> extractorOverrides = new HashMap<>();
    
    public MetaCollectorConfiguration(AnalysisEngineDescription aDescriptor)
    {
        descriptor = aDescriptor;
    }

    public MetaCollectorConfiguration(Class<? extends AnalysisComponent> aClass)
        throws ResourceInitializationException
    {
        descriptor = createEngineDescription(aClass);
    }

    public MetaCollectorConfiguration addStorageMapping(String aCollectorParameter,
            String aExtractorParameter, String aPreferredLocation)
    {
        collectorOverrides.put(aCollectorParameter, aPreferredLocation);
        extractorOverrides.put(aExtractorParameter, aPreferredLocation);
        return this;
    }
}
