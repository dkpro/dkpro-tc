/*******************************************************************************
 * Copyright 2018
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.LuceneFeatureExtractorBase;
import org.dkpro.tc.features.ngram.meta.CharacterNGramMC;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Extracts character n-grams.
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class CharacterNGram
    extends LuceneFeatureExtractorBase
    implements FeatureExtractor
{
    
    private Set<Feature> map;
    
    @Override
    public Set<Feature> extract(JCas aJCas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        if (map == null) {
            prepare();
        }
        
        
        FrequencyDistribution<String> documentCharNgrams = CharacterNGramMC
                .getAnnotationCharacterNgrams(aTarget, 
                                              ngramLowerCase, 
                                              ngramMinN, 
                                              ngramMaxN, 
                                              CharacterNGramMC.CHAR_WORD_BEGIN,
                                              CharacterNGramMC.CHAR_WORD_END);
        
        /*
         * Instead of iterating all top-k ngrams comparing them to all document ngrams for each
         * iteration (expensive for large top-Ks),we build all features that might be created only once.
         * We copy this feature map then for each call, which is cheaper and update only the values of those ngrams that are found.
         * (TH 2018-09-23) 
         */
        Set<Feature> features = new HashSet<>(map);
        
        for (String docNgram : documentCharNgrams.getKeys()) {
            if (topKSet.contains(docNgram)) {
                // remove default value from set, i.e. feature name and value are part of the
                // features identity. Thus, remove feature with value 0 and add new one with value
                // 1. Just adding the same feature with a new value will NOT override the existing
                // entry.
                Feature feature = new Feature(getFeaturePrefix() + "_" + docNgram, 0, true, FeatureType.BOOLEAN);
                features.remove(feature);
                
                //Set value to 1, i.e. feature found and mark the feature value as non-default value
                feature.setValue(1);
                feature.setDefault(false);
                
                //add to set
                features.add(feature);
            }
        }
        
//        for (String topNgram : topKSet.getKeys()) {
//            if (documentCharNgrams.getKeys().contains(topNgram)) {
//                features.add(
//                        new Feature(getFeaturePrefix() + "_" + topNgram, 1, FeatureType.BOOLEAN));
//            }
//            else {
//                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0, true,
//                        FeatureType.BOOLEAN));
//            }
//        }
        return features;
    }

    private void prepare() throws TextClassificationException
    {
        map = new HashSet<>(1024);
        //Iterate once all topK and init features  
        for(String topNgram : topKSet.getKeys()) {
            Feature feature = new Feature(getFeaturePrefix() + "_"  + topNgram, 0, true, FeatureType.BOOLEAN);
            map.add(feature);
        }
    }

    @Override
    protected String getFieldName()
    {
        return CharacterNGramMC.LUCENE_CHAR_NGRAM_FIELD + featureExtractorName;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return getClass().getSimpleName();
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays
                .asList(new MetaCollectorConfiguration(CharacterNGramMC.class, parameterSettings)
                        .addStorageMapping(CharacterNGramMC.PARAM_TARGET_LOCATION,
                                CharacterNGram.PARAM_SOURCE_LOCATION, CharacterNGramMC.LUCENE_DIR));
    }

    @Override
    protected void logSelectionProcess(long N)
    {
        getLogger().log(Level.INFO, "+++ SELECTING THE " + N + " MOST FREQUENT CHARACTER ["
                + range() + "]-GRAMS (" + caseSensitivity() + ")");
    }

    private String range()
    {
        return ngramMinN == ngramMaxN ? ngramMinN + "" : ngramMinN + "-" + ngramMaxN;
    }

    private String caseSensitivity()
    {
        return ngramLowerCase ? "case-insensitive" : "case-sensitive";
    }
}