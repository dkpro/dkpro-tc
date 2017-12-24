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
package org.dkpro.tc.api.features;

import java.util.Arrays;
import java.util.Map;

import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.dkpro.lab.task.Discriminable;

public class TcFeature
    implements Discriminable
{
    private String id;
    protected Map<String, Object> config;
    private String fullFeatureName;
    private Object[] params;
    private Class<? extends Resource> classOfFeat;

    /**
     * Creates a new feature instance
     * 
     * @param classOfFeat
     *            A feature extractor class
     * @param id
     *            A unique identification string for this feature
     * @param params
     *            Configuration parameters for this feature extractor
     */
    public TcFeature(Class<? extends Resource> classOfFeat, String id, Object[] params)
    {
        this.classOfFeat = classOfFeat;
        this.id = id;
        this.fullFeatureName = classOfFeat.getName();
        this.params = Arrays.<Object>copyOf(params, params.length);
    }

    public void setConfig(Map<String, Object> aConfig)
    {
        config = aConfig;
    }

    @Override
    public Object getDiscriminatorValue()
    {
        StringBuilder desc = new StringBuilder();
        desc.append("[");
        desc.append(fullFeatureName);
        if (params != null && params.length > 0) {
            desc.append("| ");
            for (int i = 0; i < params.length; i++) {
                Object object = params[i];
                if (object instanceof ExternalResourceDescription) {
                    ExternalResourceDescription erd = (ExternalResourceDescription) object;
                    desc.append("ExtResDesc[" + erd.getName() + "]");
                }
                else {
                    desc.append(object.toString());
                }
                if (i + 1 < params.length) {
                    desc.append(", ");
                }
            }
        }
        desc.append("]");
        return desc.toString();
    }

    public String getId()
    {
        return id;
    }

    @Override
    public ExternalResourceDescription getActualValue()
    {
        return ExternalResourceFactory.createExternalResourceDescription(classOfFeat, params);
    }

    public String getFeatureName()
    {
        return fullFeatureName;
    }

    @Override
    public String toString()
    {
        return (String) getDiscriminatorValue();
    }
}