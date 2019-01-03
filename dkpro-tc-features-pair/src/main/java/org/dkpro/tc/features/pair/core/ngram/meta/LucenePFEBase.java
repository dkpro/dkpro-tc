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
package org.dkpro.tc.features.pair.core.ngram.meta;

import static org.dkpro.tc.core.Constants.NGRAM_GLUE;

import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.features.ngram.meta.base.LuceneFeatureExtractorBase;
import org.dkpro.tc.features.ngram.meta.base.NGramFeatureExtractorBase;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public abstract class LucenePFEBase
    extends LuceneFeatureExtractorBase
{
    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int kngramUseTopK;
    /**
     * Use this number of most frequent ngrams from View 1's.
     */
    public static final String PARAM_NGRAM_USE_TOP_K_VIEW1 = "NgramUseTopK1";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_VIEW1, mandatory = true, defaultValue = "500")
    protected int ngramUseTopK1;
    /**
     * Use this number of most frequent ngrams from View 2's.
     */
    public static final String PARAM_NGRAM_USE_TOP_K_VIEW2 = "NgramUseTopK2";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_VIEW2, mandatory = true, defaultValue = "500")
    protected int ngramUseTopK2;
    /**
     * Whether features should be marked with binary (occurs, doesn't occur in this document pair)
     * values, versus the document count of the feature. In combo ngrams this is (doc1freq *
     * doc2freq). Note this only applies to feature values; frequency selection of features is based
     * on frequency across documents, not within documents.
     */
    public static final String PARAM_NGRAM_BINARY_FEATURE_VALUES_COMBO = "ngramBinaryFeatureValuesCombos";
    @ConfigurationParameter(name = PARAM_NGRAM_BINARY_FEATURE_VALUES_COMBO, mandatory = false, defaultValue = "true")
    protected boolean ngramBinaryFeatureValuesCombos;

    protected FrequencyDistribution<String> topKSetView1;
    protected FrequencyDistribution<String> topKSetView2;

    // FIXME This is a hack to deal with getTopNgrams() in LuceneFeatureExtractorBase, which can
    // take no args
    protected String fieldOfTheMoment;
    protected int topNOfTheMoment;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        forceRereadFromIndex = true;

        return true;
    }

    protected Set<Feature> addToFeatureArray(FrequencyDistribution<String> viewNgrams,
            FrequencyDistribution<String> topKSet, Set<Feature> features) throws TextClassificationException
    {
        for (String ngram : topKSet.getKeys()) {
            long value = 1;
            if (!ngramBinaryFeatureValuesCombos) {
                value = viewNgrams.getCount(ngram);
            }
            if (viewNgrams.contains(ngram)) {
                features.add(new Feature(prefix + NGRAM_GLUE + ngram, value, FeatureType.NUMERIC));
            }
            else {
                features.add(new Feature(prefix + NGRAM_GLUE + ngram, 0, FeatureType.NUMERIC));
            }
        }
        return features;
    }
}
