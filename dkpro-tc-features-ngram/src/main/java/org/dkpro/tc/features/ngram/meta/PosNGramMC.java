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
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.PosNGram;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;

public class PosNGramMC
    extends LuceneMC
{
    public static final String LUCENE_POS_NGRAM_FIELD = "posngram";

    @ConfigurationParameter(name = PosNGram.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = PosNGram.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;

    @ConfigurationParameter(name = PosNGram.PARAM_USE_CANONICAL_POS, mandatory = true, defaultValue = "true")
    private boolean useCanonical;

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
    {
        TextClassificationTarget fullDoc = new TextClassificationTarget(jcas, 0,
                jcas.getDocumentText().length());
        return getDocumentPosNgrams(jcas, fullDoc, ngramMinN, ngramMaxN, useCanonical);
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_POS_NGRAM_FIELD + featureExtractorName;
    }

    public static FrequencyDistribution<String> getDocumentPosNgrams(JCas jcas, Annotation focus,
            int minN, int maxN, boolean useCanonical)
    {
        if (selectCovered(jcas, Sentence.class, focus).size() > 0) {
            return sentenceBasedDistribution(jcas, focus, useCanonical, minN, maxN);
        }
        return documentBasedDistribution(jcas, focus, useCanonical, minN, maxN);
    }

    private static FrequencyDistribution<String> documentBasedDistribution(JCas jcas,
            Annotation focus, boolean useCanonical, int minN, int maxN)
    {

        FrequencyDistribution<String> posNgrams = new FrequencyDistribution<String>();

        List<String> postagstrings = new ArrayList<String>();
        for (POS p : selectCovered(jcas, POS.class, focus)) {
            if (useCanonical) {
                postagstrings.add(p.getClass().getSimpleName());
            }
            else {
                postagstrings.add(p.getPosValue());
            }
        }
        String[] posarray = postagstrings.toArray(new String[postagstrings.size()]);
        for (List<String> ngram : new NGramStringListIterable(posarray, minN, maxN)) {
            posNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));
        }
        return posNgrams;
    }

    private static FrequencyDistribution<String> sentenceBasedDistribution(JCas jcas,
            Annotation focus, boolean useCanonical, int minN, int maxN)
    {

        FrequencyDistribution<String> posNgrams = new FrequencyDistribution<String>();

        for (Sentence s : selectCovered(jcas, Sentence.class, focus)) {
            List<String> postagstrings = new ArrayList<String>();
            for (POS p : selectCovered(jcas, POS.class, s)) {
                if (useCanonical) {
                    postagstrings.add(p.getClass().getSimpleName());
                }
                else {
                    postagstrings.add(p.getPosValue());
                }
            }
            String[] posarray = postagstrings.toArray(new String[postagstrings.size()]);
            for (List<String> ngram : new NGramStringListIterable(posarray, minN, maxN)) {
                posNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));
            }
        }
        return posNgrams;
    }

}