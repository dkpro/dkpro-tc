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
package org.dkpro.tc.core.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.core.task.TcFeature;

public class TcFeatureFactory
{
    public static TcFeature create(
            Class<? extends Resource> featureName, Object... parameters)
    {

        /*
         * Each feature has to set a unique name which is build from the simple name of the feature
         * class and extended with a random value
         */
        String fullFeatureName = featureName.getName(); 
        String id = featureName.getSimpleName() + System.nanoTime();
        List<Object> params = getParameterAsString(parameters);
        params.add(FeatureExtractorResource_ImplBase.PARAM_UNIQUE_EXTRACTOR_NAME);
        params.add(id);

        return new TcFeature(id, fullFeatureName)
        {
            @Override
            public ExternalResourceDescription getActualValue()
            {
                return ExternalResourceFactory.createExternalResourceDescription(featureName,
                        params.toArray());
            }
        };
    }

    private static List<Object> getParameterAsString(Object[] parameters)
    {
        List<Object> out = new ArrayList<>();
        for (Object object : parameters) {
            if (object instanceof ExternalResourceDescription) {
                out.add(object);
            }
            else {
                out.add(object.toString());
            }
        }
        return out;
    }

}
