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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureSet;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.LuceneFeatureExtractorBase;
import org.dkpro.tc.features.ngram.meta.WordNGramMC;
import org.dkpro.tc.features.ngram.util.NGramUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Extracts token n-grams within the given text classification unit
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class WordNGram
    extends LuceneFeatureExtractorBase
    implements FeatureExtractor
{

    @Override
    public FeatureSet extract(JCas jcas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
    		FeatureSet features = new FeatureSet();
        FrequencyDistribution<String> documentNgrams = null;

        documentNgrams = NGramUtils.getAnnotationNgrams(jcas, aTarget, ngramLowerCase,
                filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords);

        for (String topNgram : topKSet.getKeys()) {
            if (documentNgrams.getKeys().contains(topNgram)) {
                features.add(
                        new Feature(getFeaturePrefix() + "_" + topNgram, 1, FeatureType.BOOLEAN));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0, true,
                        FeatureType.BOOLEAN));
            }
        }
        return features;
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays.asList(new MetaCollectorConfiguration(WordNGramMC.class, parameterSettings)
                .addStorageMapping(WordNGramMC.PARAM_TARGET_LOCATION,
                        WordNGram.PARAM_SOURCE_LOCATION, WordNGramMC.LUCENE_DIR));
    }

    @Override
    protected void logSelectionProcess(long N)
    {
        LogFactory.getLog(getClass()).info("+++ SELECTING THE " + N + " MOST FREQUENT WORD ["
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

    @Override
    protected String getFieldName()
    {
        return LUCENE_NGRAM_FIELD + featureExtractorName;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "ngram";
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }
}