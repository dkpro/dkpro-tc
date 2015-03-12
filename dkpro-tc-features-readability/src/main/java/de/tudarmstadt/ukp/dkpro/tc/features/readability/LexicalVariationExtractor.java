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

/**
 @author beinborn
 *
 * Calculates lexical varions as described in:
 * Vajjala and Detmar Meurers. 2012. On improving the accuracy of readability classification
 * using insights from second language acquisition. In Proceedings of the Seventh Workshop on
 * Building Educational Applications Using NLP. Association for Computational Linguistics,
 * Stroudsburg, PA, USA, 163-173.
 * 

 In addition, we use the per sentence features. 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.features.readability.util.ReadabilityUtils;

public class LexicalVariationExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String PARAM_EXCLUDE_LEXICAL_VARIATION = "ExcludeLexicalVariation";
    @ConfigurationParameter(name = PARAM_EXCLUDE_LEXICAL_VARIATION, defaultValue = "false")
    protected boolean excludeLexicalVariation;

    public static final String PARAM_EXCLUDE_PER_SENTENCE_COUNTS = "ExcludePerSentenceCounts";
    @ConfigurationParameter(name = PARAM_EXCLUDE_PER_SENTENCE_COUNTS, defaultValue = "false")
    protected boolean excludeSentenceCounts;

    public static final String N_PER_SENTENCE = "NounsPerSentence";
    public static final String ADJ_PER_SENTENCE = "AdjectivesPerSentence";
    public static final String ADV_PER_SENTENCE = "AdverbsPerSentence";
    public static final String V_PER_SENTENCE = "VerbsPerSentence";

    public static final String NOUN_VARIATION = "NounVariation";
    public static final String VERB_VARIATION = "VerbVariation1";
    public static final String VERB_VARIATION1 = "VerbVariation2";
    public static final String SQUARED_VERB_VARIATION = "SquaredVerbVariation";
    public static final String CORRECTED_VERB_VARIATION = "CorrectedVerbVariation";
    public static final String ADJ_VARIATION = "AdjectiveVariation";
    public static final String ADV_VARIATION = "AdverbVariation";
    public static final String MODIFIER_VARIATION = "ModifierVariation";
    public static final String LEXICAL_DENSITY = "LexicalDensity";
    public static final String LEXICAL_VARIATION = "LexicalVariation";

    public List<Feature> extract(JCas jcas)

    {

        double nrOfLexicalWords = 0.0;
        double nrOfNonLexicalWords = 0.0;
        Set<String> lexicalTokens = new HashSet<String>();
        int nrOfVerbs = 0;
        int nrOfAdverbs = 0;
        int nrOfAdjectives = 0;
        int nrOfModifiers = 0;
        int nrOfNouns = 0;
        Set<String> verbTypes = new HashSet<String>();
        double nrOfSentences = JCasUtil.select(jcas, Sentence.class).size() * 1.0;
        for (Token t : JCasUtil.select(jcas, Token.class)) {

            POS p = t.getPos();

            if (ReadabilityUtils.isLexicalWord(t, jcas.getDocumentLanguage())) {

                nrOfLexicalWords++;
                lexicalTokens.add(t.getLemma().getValue());
                if (p instanceof N) {
                    nrOfNouns++;
                }
                if (p instanceof ADJ) {
                    nrOfAdjectives++;
                    nrOfModifiers++;
                }
                if (p instanceof ADV) {
                    nrOfAdverbs++;
                    nrOfModifiers++;
                }
                if (p instanceof V) {
                    nrOfVerbs++;
                    verbTypes.add(t.getLemma().getValue());
                }
            }
            else if (ReadabilityUtils.isWord(t)) {

                nrOfNonLexicalWords++;
            }

        }

        List<Feature> featList = new ArrayList<Feature>();

        // per sentence
        if (!excludeSentenceCounts) {
            featList.addAll(Arrays.asList(new Feature(N_PER_SENTENCE, nrOfNouns / nrOfSentences)));
            featList.addAll(Arrays.asList(new Feature(ADJ_PER_SENTENCE, nrOfAdjectives
                    / nrOfSentences)));
            featList.addAll(Arrays
                    .asList(new Feature(ADV_PER_SENTENCE, nrOfAdverbs / nrOfSentences)));
            featList.addAll(Arrays.asList(new Feature(V_PER_SENTENCE, nrOfVerbs / nrOfSentences)));
        }
        // per lexical word

        if (!excludeLexicalVariation) {
            featList.addAll(Arrays
                    .asList(new Feature(NOUN_VARIATION, nrOfNouns / nrOfLexicalWords)));
            featList.addAll(Arrays
                    .asList(new Feature(VERB_VARIATION, nrOfVerbs / nrOfLexicalWords)));
            featList.addAll(Arrays.asList(new Feature(VERB_VARIATION1, nrOfVerbs
                    / (double) verbTypes.size())));

            featList.addAll(Arrays.asList(new Feature(CORRECTED_VERB_VARIATION, nrOfVerbs
                    / Math.sqrt((2 * ((double) verbTypes.size()))))));
            featList.addAll(Arrays.asList(new Feature(ADJ_VARIATION, nrOfAdjectives
                    / nrOfLexicalWords)));
            featList.addAll(Arrays
                    .asList(new Feature(ADV_VARIATION, nrOfAdverbs / nrOfLexicalWords)));
            featList.addAll(Arrays.asList(new Feature(MODIFIER_VARIATION, nrOfModifiers
                    / nrOfLexicalWords)));
            featList.addAll(Arrays.asList(new Feature(LEXICAL_DENSITY, nrOfLexicalWords
                    / (nrOfLexicalWords + nrOfNonLexicalWords))));
            featList.addAll(Arrays.asList(new Feature(LEXICAL_VARIATION, nrOfLexicalWords
                    / lexicalTokens.size())));
            // the value is so high, it does not seem to make sense to compare it with the others
            featList.addAll(Arrays.asList(new Feature(SQUARED_VERB_VARIATION,
                    (nrOfVerbs * nrOfVerbs) / (double) verbTypes.size())));
        }
        return featList;
    }
}