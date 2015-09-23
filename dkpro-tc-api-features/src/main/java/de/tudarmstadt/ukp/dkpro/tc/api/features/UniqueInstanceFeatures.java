/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;

public class UniqueInstanceFeatures
{

    private List<Feature> uniqueFeatures;
    private Set<String> seenFeatureNames;

    public void addFeature(Feature feature)
        throws TextClassificationException
    {

        String name = feature.getName();
        if (seenFeatureNames.contains(name)) {
            throw new TextClassificationException("The feature with the name [" + name
                    + "] has been added more than once - dropped duplicate!");
        }

        seenFeatureNames.add(name);
        uniqueFeatures.add(feature);

    }

    public UniqueInstanceFeatures(Collection<Feature> features)
        throws TextClassificationException
    {
        uniqueFeatures = new ArrayList<Feature>();
        seenFeatureNames = new TreeSet<String>();

        for (Feature f : features) {
            addFeature(f);
        }
    }

    public UniqueInstanceFeatures()
        throws TextClassificationException
    {
        uniqueFeatures = new ArrayList<Feature>();
        seenFeatureNames = new TreeSet<String>();
    }

    public void addFeatures(Collection<Feature> features)
        throws TextClassificationException
    {
        for (Feature f : features) {
            addFeature(f);
        }
    }

    public Collection<Feature> getFeatures()
    {
        return uniqueFeatures;
    }

    public void setFeatures(Collection<Feature> features)
        throws TextClassificationException
    {
        uniqueFeatures = new ArrayList<Feature>();
        seenFeatureNames = new TreeSet<String>();

        for (Feature f : features) {
            addFeature(f);
        }
    }

}
