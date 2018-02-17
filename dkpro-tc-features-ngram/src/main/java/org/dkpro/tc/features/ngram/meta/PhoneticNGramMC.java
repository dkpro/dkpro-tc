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
package org.dkpro.tc.features.ngram.meta;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.dkpro.tc.core.Constants.NGRAM_GLUE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.language.ColognePhonetic;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.PhoneticNGram;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;

public class PhoneticNGramMC
    extends LuceneMC
{
    public static final String LUCENE_PHONETIC_NGRAM_FIELD = "phoneticngram";


    @ConfigurationParameter(name = PhoneticNGram.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = PhoneticNGram.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
        throws TextClassificationException
    {
        TextClassificationTarget fullDoc = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());
        return getDocumentPhoneticNgrams(jcas, fullDoc, ngramMinN,
                ngramMaxN);
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_PHONETIC_NGRAM_FIELD + featureExtractorName;
    }
    
    public static FrequencyDistribution<String> getDocumentPhoneticNgrams(JCas jcas,
            Annotation target, int minN, int maxN)
                throws TextClassificationException
    {
        StringEncoder encoder;
        String languageCode = jcas.getDocumentLanguage();

        if (languageCode.equals("en")) {
            encoder = new Soundex();
        }
        else if (languageCode.equals("de")) {
            encoder = new ColognePhonetic();
        }
        else {
            throw new TextClassificationException(
                    "Language code '" + languageCode + "' not supported by phonetic ngrams FE.");
        }

        FrequencyDistribution<String> phoneticNgrams = new FrequencyDistribution<String>();
        for (Sentence s : selectCovered(jcas, Sentence.class, target)) {
            List<String> phoneticStrings = new ArrayList<String>();
            for (Token t : JCasUtil.selectCovered(jcas, Token.class, s)) {
                try {
                    phoneticStrings.add(encoder.encode(t.getCoveredText()));
                }
                catch (EncoderException e) {
                    throw new TextClassificationException(e);
                }
            }
            String[] array = phoneticStrings.toArray(new String[phoneticStrings.size()]);

            for (List<String> ngram : new NGramStringListIterable(array, minN, maxN)) {
                phoneticNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));

            }
        }
        return phoneticNgrams;
    }
}