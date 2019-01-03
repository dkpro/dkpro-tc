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
package org.dkpro.tc.api.features;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;

public class TcFeatureFactory
{
    /**
     * Creates a new instance of a TcFeature
     * 
     * @param featureName
     *            The class of a feature extractor
     * @param parameters
     *            The configuration parameters for this feature extractor
     * @return Configured feature which has a randomly assigned unique identification
     */
    public static TcFeature create(Class<? extends Resource> featureName, Object... parameters)
    {

        /*
         * Each feature has to set a unique name which is build from the simple name of the feature
         * class and extended with a random value
         */
        String id = featureName.getSimpleName() + System.nanoTime();
        List<Object> params = getParameterAsString(parameters);
        params.add(FeatureExtractorResource_ImplBase.PARAM_UNIQUE_EXTRACTOR_NAME);
        params.add(id);

        TcFeature tcFeature = new TcFeature(featureName, id, params.toArray());
        return tcFeature;

    }

    /**
     * Creates a new instance of a TcFeature which allows the user to set an own id
     * 
     * @param id
     *            The id of the feature which must be unique among all used features
     * @param featureName
     *            The class of the feature extractor that shall be instantiated
     * @param parameters
     *            The list of the parameters for this feature extractor
     * @return A configured feature which is identified by a user provided identification string
     */
    public static TcFeature create(String id, Class<? extends Resource> featureName,
            Object... parameters)
    {

        /*
         * Each feature has to set a unique name which is build from the simple name of the feature
         * class and extended with a random value
         */
        List<Object> params = getParameterAsString(parameters);
        params.add(FeatureExtractorResource_ImplBase.PARAM_UNIQUE_EXTRACTOR_NAME);
        params.add(id);

        TcFeature tcFeature = new TcFeature(featureName, id, params.toArray());
        return tcFeature;
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
