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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.meta.KeywordNGramMC;
import org.dkpro.tc.features.ngram.util.KeywordNGramUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class KeywordNGram
    extends AbstractNgram
{
    public static final String PARAM_NGRAM_KEYWORDS_FILE = "keywordsFile";
    @ConfigurationParameter(name = PARAM_NGRAM_KEYWORDS_FILE, mandatory = true)
    protected String keywordsFile;

    public static final String PARAM_KEYWORD_NGRAM_MARK_SENTENCE_BOUNDARY = "markSentenceBoundary";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MARK_SENTENCE_BOUNDARY, mandatory = false, defaultValue = "true")
    protected boolean markSentenceBoundary;

    public static final String PARAM_KEYWORD_NGRAM_MARK_SENTENCE_LOCATION = "markSentenceLocation";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_MARK_SENTENCE_LOCATION, mandatory = false, defaultValue = "false")
    protected boolean markSentenceLocation;

    public static final String PARAM_KEYWORD_NGRAM_INCLUDE_COMMAS = "includeCommas";
    @ConfigurationParameter(name = PARAM_KEYWORD_NGRAM_INCLUDE_COMMAS, mandatory = false, defaultValue = "false")
    protected boolean includeCommas;

    protected Set<String> keywords;

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        
        if (prepFeatSet == null) {
            prepare();
        }
        
        FrequencyDistribution<String> documentNgrams = KeywordNGramUtils.getDocumentKeywordNgrams(
                jcas, aTarget, ngramMaxN, ngramMaxN, markSentenceBoundary, markSentenceLocation,
                includeCommas, keywords);

         
        return getFeatureSet(documentNgrams);
    }

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            keywords = FeatureUtil.getStopwords(keywordsFile, true);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays.asList(new MetaCollectorConfiguration(KeywordNGramMC.class, parameterSettings)
                .addStorageMapping(KeywordNGramMC.PARAM_TARGET_LOCATION,
                        KeywordNGram.PARAM_SOURCE_LOCATION, KeywordNGramMC.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return KeywordNGramMC.KEYWORD_NGRAM_FIELD + featureExtractorName;
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
}