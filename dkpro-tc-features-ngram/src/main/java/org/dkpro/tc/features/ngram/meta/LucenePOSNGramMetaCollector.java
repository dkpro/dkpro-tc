/*******************************************************************************
 * Copyright 2016
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

import static org.dkpro.tc.features.ngram.base.LucenePOSNGramFeatureExtractorBase.LUCENE_POS_NGRAM_FIELD;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.LucenePOSNGramFeatureExtractorBase;
import org.dkpro.tc.features.ngram.util.NGramUtils;

public class LucenePOSNGramMetaCollector
    extends LuceneBasedMetaCollector
{
    @ConfigurationParameter(name = LucenePOSNGramFeatureExtractorBase.PARAM_POS_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int posNgramMinN;

    @ConfigurationParameter(name = LucenePOSNGramFeatureExtractorBase.PARAM_POS_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int posNgramMaxN;

    @ConfigurationParameter(name = LucenePOSNGramFeatureExtractorBase.PARAM_USE_CANONICAL_POS, mandatory = true, defaultValue = "true")
    private boolean useCanonical;

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
    {
        TextClassificationTarget fullDoc = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());

        return NGramUtils.getDocumentPosNgrams(jcas, fullDoc, posNgramMinN, posNgramMaxN,
                useCanonical);
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_POS_NGRAM_FIELD + featureExtractorName;
    }

}