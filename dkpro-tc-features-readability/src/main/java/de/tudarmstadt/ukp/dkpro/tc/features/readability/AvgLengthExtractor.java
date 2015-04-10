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

package de.tudarmstadt.ukp.dkpro.tc.features.readability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.WordSyllableCounter;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.features.readability.util.ReadabilityUtils;

/**
 * Computes the average word and average sentence length, ignores punctuation
 * 
 * @author beinborn
 * 
 */
public class AvgLengthExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{

    public static final String AVG_SENTENCE_LENGTH = "AvgSentenceLength";
    public static final String AVG_WORD_LENGTH_IN_CHARACTERS = "AvgWordLengthInCharacters";
    public static final String AVG_WORD_LENGTH_IN_SYLLABLES = "AvgWordLengthInSyllables";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        List<Feature> featList = new ArrayList<Feature>();

        int nrOfWords = 0;
        int nrOfCharacters = 0;
        int nrOfSyllables = 0;
        int nrOfSentences = JCasUtil.select(jcas, Sentence.class).size();

        WordSyllableCounter counter = new WordSyllableCounter(jcas.getDocumentLanguage());

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
        featList.addAll(Arrays.asList(new Feature(AVG_SENTENCE_LENGTH, nrOfWords
                / (double) nrOfSentences)));

        nrOfWords = Math.max(nrOfWords, 1);
        featList.addAll(Arrays.asList(new Feature(AVG_WORD_LENGTH_IN_CHARACTERS, nrOfCharacters
                / (double) nrOfWords)));
        featList.addAll(Arrays.asList(new Feature(AVG_WORD_LENGTH_IN_SYLLABLES, nrOfSyllables
                / (double) nrOfWords)));

        return featList;
    }

}