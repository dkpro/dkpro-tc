/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import static de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePhoneticNGramFeatureExtractorBase.LUCENE_PHONETIC_NGRAM_FIELD;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePhoneticNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;

public class LucenePhoneticNGramMetaCollector
    extends LuceneBasedMetaCollector
{

    @ConfigurationParameter(name = LucenePhoneticNGramFeatureExtractorBase.PARAM_PHONETIC_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int phoneticNgramMinN;

    @ConfigurationParameter(name = LucenePhoneticNGramFeatureExtractorBase.PARAM_PHONETIC_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int phoneticNgramMaxN;

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
        throws TextClassificationException
    {
        return NGramUtils.getDocumentPhoneticNgrams(jcas,
                phoneticNgramMinN, phoneticNgramMaxN);
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_PHONETIC_NGRAM_FIELD;
    }
}