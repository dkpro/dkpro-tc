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

import java.util.Set;
import java.util.TreeSet;

import org.dkpro.tc.api.features.util.FeatureUtil;

/**
 * Internal representation of a feature.
 */
public class Feature
    implements Comparable<Feature>
{

    protected String name;
    protected Object value;
    private boolean isDefaultValue;

    public Feature(String name, Object value)
    {
        // TODO should we cache the feature espacing? this is called quite often ...
        this.name = FeatureUtil.escapeFeatureName(name).intern();
        this.value = value;
        this.isDefaultValue = false;
    }

    /**
     * Creates a feature that is aware if the value is a <b>default value</b>. This information is
     * used when filling the feature store. A sparse feature store would not add a feature if it is
     * a default value i.e. means a feature is <i>not set</i>
     * 
     * @param name
     *            Name of the feature
     * @param value
     *            The feature value
     * @param isDefaultValue
     *            A boolean if the feature value is a default value i.e. means this feature is
     *            <i>not set</i> for an instance
     */
    public Feature(String name, Object value, boolean isDefaultValue)
    {
        // TODO should we cache the feature espacing? this is called quite often ...
        this.name = FeatureUtil.escapeFeatureName(name).intern();
        this.value = value;
        this.isDefaultValue = isDefaultValue;
    }

    public Set<Feature> asSet()
    {
        Set<Feature> set = new TreeSet<Feature>();
        set.add(this);
        return set;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name.intern();
    }

    public boolean isDefaultValue()
    {
        return isDefaultValue;
    }

    @Override
    public String toString()
    {
        String className = Feature.class.getSimpleName();
        return String.format("%s(<%s>, <%s>)", className, this.name, this.value);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Feature other = (Feature) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        }
        else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public int compareTo(Feature o)
    {
        return this.getName().compareTo(o.getName());
    }

}