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

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LuceneCharacterNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;

public class LuceneCharacterNGramUnitMetaCollector
    extends LuceneBasedMetaCollector
{

    @ConfigurationParameter(name = LuceneCharacterNGramFeatureExtractorBase.PARAM_CHAR_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int charNgramMinN;

    @ConfigurationParameter(name = LuceneCharacterNGramFeatureExtractorBase.PARAM_CHAR_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int charNgramMaxN;

    @ConfigurationParameter(name = LuceneCharacterNGramFeatureExtractorBase.PARAM_CHAR_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "false")
    private boolean lowerCase;

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
    {
        
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        for (Annotation a : JCasUtil.select(jcas, TextClassificationUnit.class)) {
            FrequencyDistribution<String> ngramDist = NGramUtils.getAnnotationCharacterNgrams(a,
                    lowerCase, charNgramMinN, charNgramMaxN, '^','$');
            for (String condition : ngramDist.getKeys()) {
                fd.addSample(condition, ngramDist.getCount(condition));
            }
        }
        return fd;
    }

    @Override
    protected String getFieldName()
    {
        return LuceneCharacterNGramFeatureExtractorBase.LUCENE_CHAR_NGRAM_FIELD;
    }
}