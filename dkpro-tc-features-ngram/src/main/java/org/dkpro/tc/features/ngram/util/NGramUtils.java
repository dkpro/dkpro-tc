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
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.dkpro.tc.core.Constants.NGRAM_GLUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.tc.api.exception.TextClassificationException;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;

public class NGramUtils
{

    public static FrequencyDistribution<String> getAnnotationNgrams(JCas jcas,
            Annotation focusAnnotation, boolean lowerCaseNGrams, boolean filterPartialMatches,
            int minN, int maxN, Set<String> stopwords)
    {
        FrequencyDistribution<String> annoNgrams = new FrequencyDistribution<String>();

        // If the focusAnnotation contains sentence annotations, extract the ngrams sentence-wise
        // if not, extract them from all tokens in the focusAnnotation
        if (selectCovered(jcas, Sentence.class, focusAnnotation).size() > 0) {
            for (Sentence s : selectCovered(jcas, Sentence.class, focusAnnotation)) {
                for (List<String> ngram : new NGramStringListIterable(
                        toText(selectCovered(Token.class, s)), minN, maxN)) {

                    if (lowerCaseNGrams) {
                        ngram = lower(ngram);
                    }

                    if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                        String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                        annoNgrams.inc(ngramString);
                    }
                }
            }
        }
        else {
            for (List<String> ngram : new NGramStringListIterable(
                    toText(selectCovered(Token.class, focusAnnotation)), minN, maxN)) {

                if (lowerCaseNGrams) {
                    ngram = lower(ngram);
                }

                if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                    String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                    annoNgrams.inc(ngramString);
                }
            }
        }
        return annoNgrams;
    }

    /**
     * Returns document ngrams over any annotation type that extends Annotation. Intended use is
     * Lemma, Stem, etc.
     * 
     * @param jcas
     *            a jcas
     * @param aTarget
     *            target annotation span
     * @param lowerCaseNGrams
     *            lower caseing
     * @param filterPartialMatches
     *            filter partial matches
     * @param minN
     *            minimal n
     * @param maxN
     *            maximal n
     * @param stopwords
     *            set of stopwords
     * @param annotationClass
     *            annotation type of the ngram
     * @return a frequency distribution
     * 
     * @throws TextClassificationException
     *             when an exception occurs
     */
    public static FrequencyDistribution<String> getDocumentNgrams(JCas jcas, Annotation aTarget,
            boolean lowerCaseNGrams, boolean filterPartialMatches, int minN, int maxN,
            Set<String> stopwords, Class<? extends Annotation> annotationClass)
        throws TextClassificationException
    {
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        for (Sentence s : selectCovered(jcas, Sentence.class, aTarget)) {
            List<String> strings = valuesToText(jcas, s, annotationClass.getName());
            for (List<String> ngram : new NGramStringListIterable(strings, minN, maxN)) {
                if (lowerCaseNGrams) {
                    ngram = lower(ngram);
                }

                if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                    String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                    documentNgrams.inc(ngramString);
                }
            }
        }
        return documentNgrams;
    }

    /**
     * An ngram (represented by the list of tokens) does not pass the stopword filter: a)
     * filterPartialMatches=true - if it contains any stopwords b) filterPartialMatches=false - if
     * it entirely consists of stopwords
     * 
     * @param tokenList
     *            The list of tokens in a single ngram
     * @param stopwords
     *            The set of stopwords used for filtering
     * @param filterPartialMatches
     *            Whether ngrams where only parts are stopwords should also be filtered. For
     *            example, "United States of America" would be filtered, as it contains the stopword
     *            "of".
     * @return Whether the ngram (represented by the list of tokens) passes the stopword filter or
     *         not.
     */
    public static boolean passesNgramFilter(List<String> tokenList, Set<String> stopwords,
            boolean filterPartialMatches)
    {
        List<String> filteredList = new ArrayList<String>();
        for (String ngram : tokenList) {
            if (!stopwords.contains(ngram)) {
                filteredList.add(ngram);
            }
        }

        if (filterPartialMatches) {
            return filteredList.size() == tokenList.size();
        }
        else {
            return filteredList.size() != 0;
        }
    }

    public static List<String> lower(List<String> ngram)
    {
        List<String> newNgram = new ArrayList<String>();
        for (String token : ngram) {
            newNgram.add(token.toLowerCase());
        }
        return newNgram;
    }

    public static <T extends Annotation> List<String> valuesToText(JCas jcas, Sentence s,
            String annotationClassName)
        throws TextClassificationException
    {
        List<String> texts = new ArrayList<String>();

        try {
            for (Entry<AnnotationFS, String> entry : FeaturePathFactory.select(jcas.getCas(),
                    annotationClassName)) {
                if (entry.getKey().getBegin() >= s.getBegin()
                        && entry.getKey().getEnd() <= s.getEnd()) {
                    texts.add(entry.getValue());
                }
            }
        }
        catch (FeaturePathException e) {
            throw new TextClassificationException(e);
        }
        return texts;
    }
}