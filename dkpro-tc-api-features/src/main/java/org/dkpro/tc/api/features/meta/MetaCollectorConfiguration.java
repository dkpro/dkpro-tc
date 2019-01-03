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
package org.dkpro.tc.api.features.meta;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.math.NumberUtils;
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

    public MetaCollectorConfiguration(Class<? extends AnalysisComponent> aClass,
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        List<Object> param = new ArrayList<>();

        for (Entry<String, Object> e : parameterSettings.entrySet()) {
            param.add(e.getKey());
            Object object = e.getValue();

            if (NumberUtils.isNumber(object.toString())) {
                if (object.toString().matches("[0-9]+\\.[0-9]+")) {
                    param.add(Double.valueOf(object.toString()));
                }
                else {
                    param.add(Integer.valueOf(object.toString()));
                }
            }
            else {
                param.add(e.getValue());
            }
        }

        descriptor = createEngineDescription(aClass, param.toArray());
    }

    public MetaCollectorConfiguration addStorageMapping(String aCollectorParameter,
            String aExtractorParameter, String aPreferredLocation)
    {
        collectorOverrides.put(aCollectorParameter, aPreferredLocation);
        extractorOverrides.put(aExtractorParameter, aPreferredLocation);
        return this;
    }
}
