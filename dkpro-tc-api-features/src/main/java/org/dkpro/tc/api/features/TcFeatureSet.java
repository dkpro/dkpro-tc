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
package org.dkpro.tc.api.features;

import java.util.ArrayList;

import org.dkpro.lab.task.Discriminable;

public class TcFeatureSet
    extends ArrayList<TcFeature> // Lists are just so much more convenient iterating and so on...so
                                 // the set is actually implemented as list
    implements Discriminable
{
    private String featureSetName = null;

    /**
     * 
     */
    private static final long serialVersionUID = 2065704241626247807L;

    public TcFeatureSet(TcFeature... features)
    {
        for (TcFeature f : features) {
            add(f);
        }
    }

    public TcFeatureSet(String featureSetName, TcFeature... features)
    {
        this.featureSetName = featureSetName;

        for (TcFeature f : features) {
            add(f);
        }
    }

    /**
     * Allows setting an user-defined name which is used when {@link #getDiscriminatorValue()
     * getDiscriminatorValue} is called by default the feature set name is a list of the names of
     * the individual features
     * 
     * @param featureSetName
     *            The name of the feature set
     */
    public void setFeatureSetName(String featureSetName)
    {
        this.featureSetName = featureSetName;
    }

    public void addFeature(TcFeature f)
    {
        add(f);
    }

    @Override
    public Object getDiscriminatorValue()
    {
        if (featureSetName != null) {
            return featureSetName;
        }

        StringBuilder sb = new StringBuilder();

        int size = this.size();
        for (int i = 0; i < size; i++) {
            TcFeature tcFeature = get(i);
            sb.append(tcFeature.getDiscriminatorValue());
            if (i + 1 < size()) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    @Override
    public Object getActualValue()
    {
        throw new UnsupportedOperationException("Method is not implemented");
    }

}