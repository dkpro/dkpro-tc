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

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.features.readability.util.ReadabilityUtils;

public class TypeTokenRatioExtractor

    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    /**
     * @author beinborn
     * 
     *         Calculates the Type-Token Ratio following the explanations in: Sowmya Vajjala and
     *         Detmar Meurers. 2012. On improving the accuracy of readability classification using
     *         insights from second language acquisition. In Proceedings of the Seventh Workshop on
     *         Building Educational Applications Using NLP. Association for Computational
     *         Linguistics, Stroudsburg, PA, USA,163-173.
     * 
     *         In this paper, the formula for the Uber Index is wrong, we corrected it here
     *         according to the original usage.
     * 
     *         Type-Token Ratio (TTR) is the ratio of number of word types (T) to total number word
     *         tokens in a text (N). It has been widely used as a measure of lexical diversity or
     *         lexical variation in language acquisition studies. However, since it is dependent on
     *         the text size, various alternative transformations of TTR came into existence. We
     *         considered Root TTR (T/√N), Corrected TTR (T/√2N), Bilogarithmic TTR (Log T/Log N)
     *         and Uber Index. Another recent TTR variant we considered, which is not a part of Lu
     *         (2011a), is the Measure of Textual Lexical Diversity (MTLD; McCarthy and Jarvis,
     *         2010). It is a TTR-based approach that is not affected by text length. It is
     *         evaluated sequentially, as mean length of string sequences that maintain a default
     *         Type-Token Ratio value. That is, the TTR is calculated at each word. When the default
     *         TTR value is reached, the MTLD count increases by one and TTR evaluations are again
     *         reset. McCarthy and Jarvis (2010) considered the default TTR as 0.72 and we continued
     *         with the same default
     **/
    public static final String PARAM_ADD_VARIANTS = "AddTTRVariants";
    @ConfigurationParameter(name = PARAM_ADD_VARIANTS, defaultValue = "false")
    protected boolean addVariants;

    public static final String FULL_FACTOR_SCORE = "ThresholdForMTLD";
    @ConfigurationParameter(name = FULL_FACTOR_SCORE, defaultValue = "0.72")
    private static double mtldThreshold;

    public static final String TYPE_TOKEN_RATIO = "Type_Token_Ratio";

    public List<Feature> extract(JCas jcas)
    {
        int numberOfTokens = 0;
        // int numberOfVerbs = 0;

        List<String> types = new ArrayList<String>();
        // List<String> verbtypes = new ArrayList<String>();

        for (Sentence sent : JCasUtil.select(jcas, Sentence.class)) {
            for (Token t : JCasUtil.selectCovered(jcas, Token.class, sent)) {

                if (ReadabilityUtils.isWord(t)) {
                    numberOfTokens++;
                    String lemma = t.getLemma().getValue().toLowerCase();
                    if (!types.contains(lemma)) {
                        types.add(lemma);
                    }
                }
            }
        }

        double typeTokenRatio = types.size() / (double) numberOfTokens;

        List<Feature> featList = new ArrayList<Feature>();
        featList.addAll(Arrays.asList(new Feature(TYPE_TOKEN_RATIO, typeTokenRatio)));
        if (addVariants) {
            double rootTtr = types.size() / Math.sqrt(numberOfTokens);
            double correctedTtr = types.size() / Math.sqrt((2 * numberOfTokens));
            double bilog = Math.log(types.size()) / Math.log(numberOfTokens);
            double uberIndex = (Math.log(numberOfTokens) * Math.log(numberOfTokens))
                    / (Math.log(numberOfTokens) - Math.log(types.size()));
            double mtld = 0.5 * (ReadabilityUtils.getMTLD(jcas, false, mtldThreshold) + ReadabilityUtils
                    .getMTLD(jcas, true, mtldThreshold));

            featList.addAll(Arrays.asList(new Feature("RootTTR", rootTtr)));
            featList.addAll(Arrays.asList(new Feature("CorrectedTTR", correctedTtr)));
            featList.addAll(Arrays.asList(new Feature("Bilogarithmic TTR", bilog)));
            featList.addAll(Arrays.asList(new Feature("UberIndex", uberIndex)));
            featList.addAll(Arrays.asList(new Feature("MTLD", mtld)));

        }
        return featList;
    }
}
