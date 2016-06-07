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

package org.dkpro.tc.features.readability;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.readability.util.ReadabilityUtils;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.WordSyllableCounter;

/**
 * Computes the average word and average sentence length, ignores punctuation
 */
public class AvgLengthExtractor
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{

    public static final String AVG_SENTENCE_LENGTH = "AvgSentenceLength";
    public static final String AVG_WORD_LENGTH_IN_CHARACTERS = "AvgWordLengthInCharacters";
    public static final String AVG_WORD_LENGTH_IN_SYLLABLES = "AvgWordLengthInSyllables";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
        Set<Feature> featSet = new HashSet<Feature>();

        int nrOfWords = 0;
        int nrOfCharacters = 0;
        int nrOfSyllables = 0;
        int nrOfSentences = JCasUtil.selectCovered(jcas, Sentence.class, target).size();

        WordSyllableCounter counter = new WordSyllableCounter(
                jcas.getDocumentLanguage());

        for (Token t : JCasUtil.select(jcas, Token.class)) {

            if (ReadabilityUtils.isWord(t)) {
                String word = t.getCoveredText();
                nrOfWords++;
                nrOfCharacters += word.length();
                nrOfSyllables += counter.countSyllables(word);
            }
        }
        // avoid division by 0;

        nrOfSentences = Math.max(nrOfSentences, 1);
        featSet.addAll(Arrays
                .asList(new Feature(AVG_SENTENCE_LENGTH, nrOfWords / (double) nrOfSentences)));

        nrOfWords = Math.max(nrOfWords, 1);
        featSet.addAll(Arrays.asList(
                new Feature(AVG_WORD_LENGTH_IN_CHARACTERS, nrOfCharacters / (double) nrOfWords)));
        featSet.addAll(Arrays.asList(
                new Feature(AVG_WORD_LENGTH_IN_SYLLABLES, nrOfSyllables / (double) nrOfWords)));

        return featSet;
    }

}