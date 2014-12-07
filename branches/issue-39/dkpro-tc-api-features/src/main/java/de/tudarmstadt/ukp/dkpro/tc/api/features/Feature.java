/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;

/**
 * Internal representation of a feature.
 *  
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * @author Philipp Wetzler
 * @author Steven Bethard
 */
//TODO Issue 119: replace with own Feature implementation?
public class Feature
    implements Serializable
{

    private static final long serialVersionUID = -3215288856677656204L;

    protected String name;

    protected Object value;

    /**
     * Empty constructor (needed for serialization)
     */
    public Feature()
    {
    }

    /**
     * Create a new feature
     * 
     * @param name
     * @param value
     */
    public Feature(String name, Object value)
    {
        this.name = FeatureUtil.escapeFeatureName(name);
        this.value = value;
    }
    
    /**
     * Convenience method for feature extractors that expect list of features
     * 
     * @return This feature as the first element of a list of features.
     */
    public List<Feature> asList() {
        List<Feature> list = new ArrayList<Feature>();
        list.add(this);
        return list;
    }

    /**
     * @return
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * @param value
     */
    public void setValue(Object value)
    {
        this.value = value;
    }

    /**
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        String className = Feature.class.getSimpleName();
        return String.format("%s(<%s>, <%s>)", className, this.name, this.value);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Feature) {
            Feature other = (Feature) obj;
            boolean nameMatch = (this.name == null && other.name == null)
                    || (this.name != null && this.name.equals(other.name));
            boolean valueMatch = (this.value == null && other.value == null)
                    || (this.value != null && this.value.equals(other.value));
            return nameMatch && valueMatch;
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 1;
        hash = hash * 31 + (this.name == null ? 0 : this.name.hashCode());
        hash = hash * 31 + (this.value == null ? 0 : this.value.hashCode());
        return hash;
    }
}