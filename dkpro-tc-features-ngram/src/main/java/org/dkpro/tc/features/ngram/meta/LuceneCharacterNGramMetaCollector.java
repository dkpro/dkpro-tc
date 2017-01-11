/*******************************************************************************
 * Copyright 2017
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

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;
import org.dkpro.tc.features.ngram.util.NGramUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Creates a frequency distribution over all characters occurring in the entire document text i.e. index zero to document-length.
 * For considering only a subset of the document text and working with several target annotations {@link org.dkpro.tc.features.ngram.meta.LuceneCharacterNGramUnitMetaCollector}
 */
public class LuceneCharacterNGramMetaCollector
    extends LuceneBasedMetaCollector
{
    public static final String LUCENE_CHAR_NGRAM_FIELD = "charngram";

    @ConfigurationParameter(name = LuceneCharacterNGram.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = LuceneCharacterNGram.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;
    
    @ConfigurationParameter(name = LuceneCharacterNGram.PARAM_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "true")
    private String stringLowerCase;
    
    boolean lowerCase = true;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        lowerCase = Boolean.valueOf(stringLowerCase);
        
    }
    
    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas){
        TextClassificationTarget fullDoc = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
        return NGramUtils.getDocumentCharacterNgrams(jcas, fullDoc, lowerCase,
                ngramMinN, ngramMaxN);
    }
    
    @Override
    protected String getFieldName(){
        return LUCENE_CHAR_NGRAM_FIELD + featureExtractorName;
    }
}