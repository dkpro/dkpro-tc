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
package org.dkpro.tc.features.ngram.util;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;

public class KeywordNGramUtils
{
    public static final String SENTENCE_BOUNDARY = "SB";
    public static final String COMMA = "CA";
    public static final String GLUE = "_";
    public static final String MIDNGRAMGLUE = "_A";

    // all tokens should be already lowercased
    /**
     * Finds all minN- to maxN-length ngrams of tokens occurring in the keyword list. All tokens
     * should already be lowercased, if applicable. The keyword list can contain multi-token words
     * like "Brussel sprouts". If keyword list contains both "Brussel" and "Brussel sprouts", then
     * only "Brussel sprouts" will be added. Otherwise, the smallest multiword matching keyword will
     * be added.
     * 
     * @param jcas
     *            a jcas
     * @param anno
     *            the annotation
     * @param minN
     *            minimum ngram length
     * @param maxN
     *            maximum ngram length
     * @param markSentenceBoundary
     *            mark the boundary of a sentence
     * @param markSentenceLocation
     *            mark the location of a sentence
     * @param includeCommas
     *            include commas
     * @param keywords
     *            list of keywords
     * @return all ngrams of keywords in jcas
     */
    public static FrequencyDistribution<String> getDocumentKeywordNgrams(JCas jcas, Annotation anno,
            int minN, int maxN, boolean markSentenceBoundary, boolean markSentenceLocation,
            boolean includeCommas, Set<String> keywords)
    {
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        List<String> keywordList = new ArrayList<String>();
        int sentenceNumber = 0;
        int totalSentences = selectCovered(jcas, Sentence.class, anno).size();
        for (Sentence s : selectCovered(jcas, Sentence.class, anno)) {
            List<Token> sentence = selectCovered(Token.class, s);
            for (int tokenpointer = 0; tokenpointer < sentence.size(); tokenpointer++) {
                String token = sentence.get(tokenpointer).getCoveredText();
                token = token.toLowerCase();
                String compositeNgram = "";
                boolean foundComposite = false;
                for (int i = tokenpointer; i >= 0; i--) {
                    compositeNgram = sentence.get(i).getCoveredText().toLowerCase() + " "
                            + compositeNgram;
                    if (compositeNgram.endsWith(" ")) {
                        compositeNgram = compositeNgram.replace(" ", "");
                    }
                    if (keywords.contains(compositeNgram)) {
                        keywordList.add(compositeNgram.replace(" ", MIDNGRAMGLUE));
                        foundComposite = true;
                    }
                }
                if (!foundComposite && keywords.contains(token)) {
                    keywordList.add(token);
                }
                else if (includeCommas && token.equals(",")) {
                    keywordList.add(COMMA);
                }
            }
            String sentenceBoundary = SENTENCE_BOUNDARY;
            if (markSentenceLocation) {
                if (((double) sentenceNumber / totalSentences) < 0.25) {
                    sentenceBoundary = sentenceBoundary + "BEG";
                }
                else if (((double) sentenceNumber / totalSentences) > 0.75) {
                    sentenceBoundary = sentenceBoundary + "END";
                }
                else {
                    sentenceBoundary = sentenceBoundary + "MID";
                }
            }
            if (markSentenceBoundary) {
                keywordList.add(sentenceBoundary);
            }
            sentenceNumber++;
        }
        for (List<String> ngram : new NGramStringListIterable(
                keywordList.toArray(new String[keywordList.size()]), minN, maxN)) {
            String ngramString = StringUtils.join(ngram, GLUE);
            documentNgrams.inc(ngramString);
        }
        return documentNgrams;
    }

    public static FrequencyDistribution<String> getMultipleViewKeywordNgrams(List<JCas> jcases,
            int minN, int maxN, boolean markSentenceBoundary, boolean markSentenceLocation,
            boolean includeCommas, Set<String> keywords)
    {

        FrequencyDistribution<String> viewNgramsTotal = new FrequencyDistribution<String>();

        for (JCas view : jcases) {
            TextClassificationTarget aTarget = JCasUtil.selectSingle(view,
                    TextClassificationTarget.class);
            FrequencyDistribution<String> oneViewsNgrams = getDocumentKeywordNgrams(view, aTarget,
                    minN, maxN, markSentenceBoundary, markSentenceLocation, includeCommas,
                    keywords);
            // This is a hack because there's no method to combine 2 FD's
            for (String key : oneViewsNgrams.getKeys()) {
                viewNgramsTotal.addSample(key, oneViewsNgrams.getCount(key));
            }
        }

        return viewNgramsTotal;

    }
}
