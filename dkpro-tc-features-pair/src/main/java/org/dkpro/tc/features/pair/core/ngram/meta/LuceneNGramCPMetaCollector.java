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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.features.ngram.util.NGramUtils;
import org.dkpro.tc.features.pair.core.ngram.LuceneNGramCPFE;
import org.dkpro.tc.features.pair.core.ngram.LuceneNGramPFE;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class LuceneNGramCPMetaCollector
    extends LuceneCPMetaCollectorBase
{

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_NGRAM_MIN_N_COMBO, mandatory = true, defaultValue = "2")
    protected int ngramMinNCombo;

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_NGRAM_MAX_N_COMBO, mandatory = true, defaultValue = "4")
    protected int ngramMaxNCombo;

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_NGRAM_MIN_N_VIEW1, mandatory = true, defaultValue = "1")
    protected int ngramView1MinN;

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_NGRAM_MIN_N_VIEW2, mandatory = true, defaultValue = "1")
    protected int ngramView2MinN;

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    protected int ngramMinN;

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_NGRAM_MAX_N_VIEW1, mandatory = true, defaultValue = "3")
    protected int ngramView1MaxN;

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_NGRAM_MAX_N_VIEW2, mandatory = true, defaultValue = "3")
    protected int ngramView2MaxN;

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    protected int ngramMaxN;

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    protected String ngramStopwordsFile;

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_FILTER_PARTIAL_STOPWORD_MATCHES, mandatory = true, defaultValue = "false")
    protected boolean filterPartialStopwordMatches;

    @ConfigurationParameter(name = LuceneNGramCPFE.PARAM_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "true")
    protected boolean ngramLowerCase;

    protected Set<String> stopwords;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            stopwords = FeatureUtil.getStopwords(ngramStopwordsFile, ngramLowerCase);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    protected int getNgramMinNCombo()
    {
        return ngramMinNCombo;
    }

    @Override
    protected int getNgramMaxNCombo()
    {
        return ngramMaxNCombo;
    }

    @Override
    protected FrequencyDistribution<String> getNgramsFD(List<JCas> jcases)
        throws TextClassificationException
    {
        FrequencyDistribution<String> fd = ComboUtils.getMultipleViewNgrams(jcases, null,
                ngramLowerCase, filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords);
        return fd;
    }

    /**
     * This is an artifact to be merged with {@code getNgramsFD(List<JCas> jcases)} when pair FEs
     * are ready.
     */
    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
        throws TextClassificationException
    {
        return null;
    }

    @Override
    protected FrequencyDistribution<String> getNgramsFDView1(JCas view1,
            TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        FrequencyDistribution<String> fd = NGramUtils.getDocumentNgrams(view1, aTarget,
                ngramLowerCase, filterPartialStopwordMatches, ngramView1MinN, ngramView1MaxN,
                stopwords, Token.class);
        return fd;
    }

    @Override
    protected FrequencyDistribution<String> getNgramsFDView2(JCas view2,
            TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        FrequencyDistribution<String> fd = NGramUtils.getDocumentNgrams(view2, aTarget,
                ngramLowerCase, filterPartialStopwordMatches, ngramView2MinN, ngramView2MaxN,
                stopwords, Token.class);
        return fd;
    }

    @Override
    protected String getFieldName()
    {
        return WordNGram.LUCENE_NGRAM_FIELD;
    }

    @Override
    protected String getFieldNameView1()
    {
        return LuceneNGramPFE.LUCENE_NGRAM_FIELD1;
    }

    @Override
    protected String getFieldNameView2()
    {
        return LuceneNGramPFE.LUCENE_NGRAM_FIELD2;
    }

    @Override
    protected String getFieldNameCombo()
    {
        return LuceneNGramCPFE.LUCENE_NGRAM_FIELDCOMBO;
    }
}
