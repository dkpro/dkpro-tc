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
package org.dkpro.tc.core.task;

import java.util.Map;

import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.impl.DynamicDimension;
import org.dkpro.tc.api.features.TcFeature;

public class DynamicDiscriminableFunctionDimension
    extends Dimension<TcFeature>
    implements DynamicDimension
{
    private TcFeature[] closures;
    private int current = -1;
    private Map<String, Object> config;

    @SafeVarargs
    public DynamicDiscriminableFunctionDimension(String aName, TcFeature... aClosures)
    {
        super(aName);
        closures = aClosures;
    }
    
    @Override
    public void setConfiguration(Map<String, Object> aConfig)
    {
        config = aConfig;
    }

    @Override
    public boolean hasNext()
    {
        return current+1 < closures.length;
    }

    @Override
    public TcFeature next()
    {
        current++;
        return current();
    }

    @Override
    public TcFeature current()
    {
        // When calling next() after rewind() to position current() at the first
        // dimension value, no config has been set yet. At this point just
        // do nothing.
        if (config == null) {
            return null;
        }
        
        closures[current].setConfig(config);
        return closures[current];
    }

    @Override
    public void rewind()
    {
        current = -1;
    }

    @Override
    public String toString()
    {
        return "[" + getName() + ": " + (current >= 0 && current < closures.length ? current() : "?") + "]";
    }
}