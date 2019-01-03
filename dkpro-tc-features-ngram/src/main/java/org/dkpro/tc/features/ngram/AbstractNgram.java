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
package org.dkpro.tc.features.ngram;

import java.util.HashSet;
import java.util.Set;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.features.ngram.meta.base.LuceneFeatureExtractorBase;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public abstract class AbstractNgram extends LuceneFeatureExtractorBase
implements FeatureExtractor
{
    protected Set<Feature> prepFeatSet;

    protected Set<Feature> getFeatureSet(FrequencyDistribution<String> fd) throws TextClassificationException {
        /*
         * Instead of iterating all top-k ngrams comparing them to all document ngrams for each
         * iteration (expensive for large top-Ks),we build all features that might be created only once.
         * We copy this feature map then for each call, which is cheaper and update only the values of those ngrams that are found.
         * (TH 2018-09-23) 
         */
        Set<Feature> features = new HashSet<>(prepFeatSet);
        
        for (String ng : fd.getKeys()) {
            if (topKSet.contains(ng)) {
                // remove default value from set, i.e. feature name and value are part of the
                // features identity. Thus, remove feature with value 0 and add new one with value
                // 1. Just adding the same feature with a new value will NOT override the existing
                // entry.
                Feature feature = new Feature(getFeaturePrefix() + "_" + ng, 0, true, FeatureType.BOOLEAN);
                features.remove(feature);
                
                //Set value to 1, i.e. feature found and mark the feature value as non-default value
                feature.setValue(1);
                feature.setDefault(false);
                
                //add to set
                features.add(feature);
            }
        }
        return features;
    }

    protected void prepare() throws TextClassificationException
    {
        prepFeatSet = new HashSet<>(1024);
        //Iterate once all topK and init features  
        for(String topNgram : topKSet.getKeys()) {
            Feature feature = new Feature(getFeaturePrefix() + "_"  + topNgram, 0, true, FeatureType.BOOLEAN);
            prepFeatSet.add(feature);
        }
    }
}
