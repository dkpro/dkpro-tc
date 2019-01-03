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
package org.dkpro.tc.features.ngram.meta;

import java.io.IOException;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.features.ngram.meta.base.NGramFeatureExtractorBase;
import org.dkpro.tc.features.ngram.util.NGramUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class WordNGramMC
    extends LuceneMC
{

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    private String ngramStopwordsFile;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_FILTER_PARTIAL_STOPWORD_MATCHES, mandatory = true, defaultValue = "false")
    private boolean filterPartialStopwordMatches;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "true")
    private String stringNgramLowerCase;

    boolean ngramLowerCase = true;

    private Set<String> stopwords;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        ngramLowerCase = Boolean.valueOf(stringNgramLowerCase);

        try {
            stopwords = FeatureUtil.getStopwords(ngramStopwordsFile, ngramLowerCase);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
        throws TextClassificationException
    {

        TextClassificationTarget fullDoc = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());

        FrequencyDistribution<String> fd = null;
        fd = NGramUtils.getDocumentNgrams(jcas, fullDoc, ngramLowerCase,
                filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords, Token.class);

        return fd;
    }

    @Override
    protected String getFieldName()
    {
        return WordNGram.LUCENE_NGRAM_FIELD + featureExtractorName;
    }
}