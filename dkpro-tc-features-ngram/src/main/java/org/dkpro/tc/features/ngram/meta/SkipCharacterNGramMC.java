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

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.SkipCharacterNGram;
import org.dkpro.tc.features.ngram.util.SkipNgramStringListIterable;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class SkipCharacterNGramMC
    extends LuceneMC
{

    public static final String LUCENE_FIELD = "charskipngram";

    @ConfigurationParameter(name = SkipCharacterNGram.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "2")
    private int minN;

    @ConfigurationParameter(name = SkipCharacterNGram.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int maxN;

    @ConfigurationParameter(name = SkipCharacterNGram.PARAM_CHAR_SKIP_SIZE, mandatory = true, defaultValue = "2")
    private int skipSize;

    @ConfigurationParameter(name = SkipCharacterNGram.PARAM_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "true")
    private String stringLowerCase;

    boolean lowerCase = true;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        lowerCase = Boolean.valueOf(stringLowerCase);

    }

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
    {
        TextClassificationTarget fullDoc = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());

        return getCharacterSkipNgrams(jcas, fullDoc, lowerCase, minN, maxN, skipSize);
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_FIELD + featureExtractorName;
    }

    public static FrequencyDistribution<String> getCharacterSkipNgrams(JCas jcas, Annotation target,
            boolean lowerCaseNGrams, int minN, int maxN, int skipN)
    {
        FrequencyDistribution<String> charNgrams = new FrequencyDistribution<String>();
        for (Token t : selectCovered(jcas, Token.class, target)) {
            String tokenText = t.getCoveredText();
            String[] charsTemp = tokenText.split("");
            String[] chars = new String[charsTemp.length + 1];
            for (int i = 0; i < charsTemp.length; i++) {
                chars[i] = charsTemp[i];
            }

            chars[0] = "^";
            chars[charsTemp.length] = "$";

            for (List<String> ngram : new SkipNgramStringListIterable(chars, minN, maxN, skipN)) {
                if (lowerCaseNGrams) {
                    ngram = lower(ngram);
                }

                String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                charNgrams.inc(ngramString);
            }
        }
        return charNgrams;
    }

    public static List<String> lower(List<String> ngram)
    {
        List<String> newNgram = new ArrayList<String>();
        for (String token : ngram) {
            newNgram.add(token.toLowerCase());
        }
        return newNgram;
    }
}