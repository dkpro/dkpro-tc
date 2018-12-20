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
package org.dkpro.tc.features.pair.core.ngram.meta;

import static org.dkpro.tc.core.Constants.NGRAM_GLUE;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.util.NGramUtils;

public class ComboUtils
{
    /**
     * This is the character for joining strings for combo ngrams.
     */
    public static final String JOINT = "_AND";

    /**
     * Get combinations of ngrams from a pair of documents.
     * 
     * @param document1NGrams
     *            ngrams from document 1
     * @param document2NGrams
     *            ngrams from document 2
     * @param minN
     *            minimum size for a new combined ngram
     * @param maxN
     *            max size for a new combined ngram
     * @param ngramUseSymmetricalCombos
     *            whether or not to return view-neutral ngrams
     * @return combinations of ngrams
     */
    public static FrequencyDistribution<String> getCombinedNgrams(
            FrequencyDistribution<String> document1NGrams,
            FrequencyDistribution<String> document2NGrams, int minN, int maxN,
            boolean ngramUseSymmetricalCombos)
    {
        FrequencyDistribution<String> documentComboNGrams = new FrequencyDistribution<String>();
        for (String ngram1 : document1NGrams.getKeys()) {
            int ngram1size = StringUtils.countMatches(ngram1, NGRAM_GLUE) + 1;
            for (String ngram2 : document2NGrams.getKeys()) {
                int ngram2size = StringUtils.countMatches(ngram2, NGRAM_GLUE) + 1;
                if (ngram1size + ngram2size >= minN && ngram1size + ngram2size <= maxN) {
                    // final feature value, binary or count, is controlled in the FE
                    long value = document1NGrams.getCount(ngram1)
                            * document2NGrams.getCount(ngram2);
                    String comboNgram = ngram1 + JOINT + ngram2;
                    documentComboNGrams.addSample(comboNgram, value);
                    if (ngramUseSymmetricalCombos) {
                        comboNgram = ngram2 + JOINT + ngram1;
                        documentComboNGrams.addSample(comboNgram, value);
                    }
                }
            }
        }
        return documentComboNGrams;
    }

    public static FrequencyDistribution<String> getMultipleViewNgrams(List<JCas> JCases,
            Annotation preSetTarget, boolean ngramLowerCase, boolean filterPartialStopwords,
            int ngramMinN, int ngramMaxN, Set<String> stopwords)
        throws TextClassificationException
    {

        FrequencyDistribution<String> viewNgramsTotal = new FrequencyDistribution<String>();

        for (JCas jCas : JCases) {
            
            TextClassificationTarget aTarget = JCasUtil.selectSingle(jCas,
                    TextClassificationTarget.class);
            
            FrequencyDistribution<String> oneViewsNgrams = NGramUtils.getDocumentNgrams(jCas, aTarget, ngramLowerCase,
                    filterPartialStopwords, ngramMinN, ngramMaxN, stopwords, Token.class);
            
            if (preSetTarget != null) {
                oneViewsNgrams = NGramUtils.getAnnotationNgrams(jCas, preSetTarget,
                        ngramLowerCase, filterPartialStopwords, ngramMinN, ngramMaxN, stopwords);
            }
            // This is a hack because there's no method to combine 2 FD's
            for (String key : oneViewsNgrams.getKeys()) {
                viewNgramsTotal.addSample(key, oneViewsNgrams.getCount(key));
            }
        }

        return viewNgramsTotal;

    }

}
