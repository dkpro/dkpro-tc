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
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.NP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.PP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.SBAR;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.VP;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.features.readability.util.ParsePatternUtils;

public class ParsePatternExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    /**
     * Extracts parse features to estimate syntactic complexity. The features are described in
     * Vajjala and Detmar Meurers. 2012. On improving the accuracy of readability classification
     * using insights from second language acquisition. In Proceedings of the Seventh Workshop on
     * Building Educational Applications Using NLP. Association for Computational Linguistics,
     * Stroudsburg, PA, USA, 163-173.
     * 
     * The definitions of syntactic elements are based on Xiaofei Lu, 2010. Automatic analysis of
     * syntactic complexity in second language writing. In International Journal of Corpus
     * Linguistics}, vol.15, issue 4, pp.474--496
     * 
     * @author beinborn
     **/
    public static final String NPS_PER_SENTENCE = "NPsPerSentence";
    public static final String VPS_PER_SENTENCE = "VPsPerSentence";
    public static final String PPS_PER_SENTENCE = "PPsPerSentence";
    public static final String SBARS_PER_SENTENCE = "SBarsPerSentence";

    public static final String CLAUSES_PER_SENTENCE = "ClausesPerSentence";
    public static final String DEP_CLAUSES_PER_SENTENCE = "DependentClausesPerSentence";

    public static final String TUNITS_PER_SENTENCE = "TUnitsPerSentence";
    public static final String COMPLEX_TUNITS_PER_SENTENCE = "ComplexTUnitsPerSentence";
    public static final String COORDS_PER_SENTENCE = "CoOrdPhrasesPerSentence";

    public static final String AVG_NP_LENGTH = "AvgNPLength";
    public static final String AVG_VP_LENGTH = "AvgVPLength";
    public static final String AVG_PP_LENGTH = "AvgPPLength";
    public static final String AVG_CLAUSE_LENGTH = "AvgClauseLength";
    public static final String AVG_TUNIT_LENGTH = "AvgTunitLength";

    public static final String AVG_TREE_DEPTH = "AvgParseTreeDepth";

    public static final String CLAUSES_PER_TUNIT = "ClausesPerTunit";
    public static final String COMPLEX_TUNITS_PER_TUNIT = "ComplexTunitsPerTunit";
    public static final String COORDS_PER_TUNIT = "CoordinationsPerTunit";
    public static final String COMPLEXNOMINALS_PER_TUNIT = "ComplexNominalsPerTunit";
    public static final String VERBPHRASES_PER_TUNIT = "VerbPhrasesPerTunit";
    public static final String DEPCLAUSE_TUNIT_RATIO = "DependentClauseToTUnitRatio";

    public static final String DEPCLAUSE_CLAUSE_RATIO = "DependentClauseToClauseRatio";
    public static final String COORDS_PER_CLAUSE = "CoordinationsPerClause";
    public static final String COMPLEXNOMINALS_PER_CLAUSE = "ComplexNominalsPerClause";

    // Co-ordinate Phrases per Clause (CP/C)
    // Complex Nominals per Clause (CN/C)

    public List<Feature> extract(JCas jcas)

    {
        double nrOfNPs = 0.0;
        double nrOfVPs = 0.0;
        double nrOfPPs = 0.0;
        int nrOfSbars = 0;
        int nrOfVerbphrases = 0;
        int nrOfComplexNominals = 0;
        double nrOfClauses = 0.0;
        int nrOfDependentClauses = 0;
        double nrOfTunits = 0.0;
        int nrOfComplexTunits = 0;
        int nrOfCoords = 0;

        int lengthSumNPs = 0;
        int lengthSumVPs = 0;
        int lengthSumPPs = 0;
        int lengthSumClauses = 0;
        int lengthSumTunits = 0;
        int parseTreeDepthSum = 0;
        List<Feature> featList = new ArrayList<Feature>();
        double nrOfSentences = JCasUtil.select(jcas, Sentence.class).size() * 1.0;
        for (Sentence s : JCasUtil.select(jcas, Sentence.class)) {
            parseTreeDepthSum += ParsePatternUtils.getParseDepth(s);
            for (Constituent c : JCasUtil.selectCovered(Constituent.class, s)) {
                if (c instanceof NP) {
                    nrOfNPs++;
                    lengthSumNPs += c.getCoveredText().length();
                }
                else if (c instanceof VP) {
                    nrOfVPs++;
                    lengthSumVPs += c.getCoveredText().length();
                }
                else if (c instanceof PP) {
                    nrOfPPs++;
                    lengthSumPPs += c.getCoveredText().length();
                }

                else if (c instanceof SBAR) {
                    nrOfSbars++;
                    if (ParsePatternUtils.isDependentClause(c)) {
                        nrOfDependentClauses++;
                    }

                }

                else if (ParsePatternUtils.isClause(c)) {
                    nrOfClauses++;
                    lengthSumClauses += c.getCoveredText().length();

                }

                if (ParsePatternUtils.isTunit(c)) {
                    nrOfTunits++;
                    lengthSumTunits += c.getCoveredText().length();
                    if (ParsePatternUtils.isComplexTunit(c)) {
                        nrOfComplexTunits++;
                    }
                }
                if (ParsePatternUtils.isCoordinate(c)) {
                    nrOfCoords++;
                }

                if (ParsePatternUtils.isComplexNominal(c)) {
                    nrOfComplexNominals++;
                }
                if (ParsePatternUtils.isVerbPhrase(c)) {
                    nrOfVerbphrases++;
                }
            }
        }

        // avoid division by zero, there should be at least one sentence in the cas
        nrOfSentences = Math.max(1, nrOfSentences);

        featList.addAll(Arrays.asList(new Feature(NPS_PER_SENTENCE, nrOfNPs / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(VPS_PER_SENTENCE, nrOfVPs / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(PPS_PER_SENTENCE, nrOfPPs / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(SBARS_PER_SENTENCE, nrOfSbars / nrOfSentences)));

        featList.addAll(Arrays
                .asList(new Feature(CLAUSES_PER_SENTENCE, nrOfClauses / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(DEP_CLAUSES_PER_SENTENCE, nrOfDependentClauses
                / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(TUNITS_PER_SENTENCE, nrOfTunits / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(COMPLEX_TUNITS_PER_SENTENCE, nrOfComplexTunits
                / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(COORDS_PER_SENTENCE, nrOfCoords / nrOfSentences)));

        // avoid division by 0,
        // if we don't have any NPs, the lengthSum is 0, division by 1 will yield 0 as average
        // length
        nrOfNPs = Math.max(1, nrOfNPs);
        nrOfVPs = Math.max(1, nrOfVPs);
        nrOfPPs = Math.max(1, nrOfPPs);
        nrOfClauses = Math.max(1, nrOfClauses);
        nrOfTunits = Math.max(1, nrOfTunits);

        featList.addAll(Arrays.asList(new Feature(AVG_NP_LENGTH, lengthSumNPs / nrOfNPs)));
        featList.addAll(Arrays.asList(new Feature(AVG_VP_LENGTH, lengthSumVPs / nrOfVPs)));
        featList.addAll(Arrays.asList(new Feature(AVG_PP_LENGTH, lengthSumPPs / nrOfPPs)));
        featList.addAll(Arrays.asList(new Feature(AVG_TUNIT_LENGTH, lengthSumTunits / nrOfTunits)));
        featList.addAll(Arrays
                .asList(new Feature(AVG_CLAUSE_LENGTH, lengthSumClauses / nrOfClauses)));
        featList.addAll(Arrays
                .asList(new Feature(AVG_TREE_DEPTH, parseTreeDepthSum / nrOfSentences)));

        featList.addAll(Arrays.asList(new Feature(CLAUSES_PER_TUNIT, nrOfClauses / nrOfTunits)));
        featList.addAll(Arrays.asList(new Feature(COMPLEX_TUNITS_PER_TUNIT, nrOfComplexTunits
                / nrOfTunits)));
        featList.addAll(Arrays.asList(new Feature(COORDS_PER_TUNIT, nrOfCoords / nrOfTunits)));
        featList.addAll(Arrays.asList(new Feature(COMPLEXNOMINALS_PER_TUNIT, nrOfComplexNominals
                / nrOfTunits)));
        featList.addAll(Arrays.asList(new Feature(VERBPHRASES_PER_TUNIT, nrOfVerbphrases
                / nrOfTunits)));
        featList.addAll(Arrays.asList(new Feature(DEPCLAUSE_TUNIT_RATIO, nrOfDependentClauses
                / nrOfTunits)));
        ;

        featList.addAll(Arrays.asList(new Feature(DEPCLAUSE_CLAUSE_RATIO, nrOfDependentClauses
                / nrOfClauses)));
        featList.addAll(Arrays.asList(new Feature(COORDS_PER_CLAUSE, nrOfCoords / nrOfClauses)));
        ;
        featList.addAll(Arrays.asList(new Feature(COMPLEXNOMINALS_PER_CLAUSE, nrOfComplexNominals
                / nrOfClauses)));
        ;
        return featList;
    }
}