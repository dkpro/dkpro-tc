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
package org.dkpro.tc.features.spelling;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_CONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_DET;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NOUN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NUM;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PRON;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PROPN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PUNCT;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_VERB;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_X;

/**
 * Computes for each coarse grained POS tag the ratio of being affected by a spelling error. For
 * example, if there are 4 spelling errors in the document, and 3 of them affect nouns, while one
 * affects a verb, the ratio will be 0.75 for nouns, 0.25 for verbs, and 0.0 for all other POS.
 */
public class SpellingErrorPOSRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    public static final String FN_ADJ_ERROR_RATIO = "AdjErrorRatio";
    public static final String FN_ADV_ERROR_RATIO = "AdvErrorRatio";
    public static final String FN_ART_ERROR_RATIO = "ArtErrorRatio";
    public static final String FN_CARD_ERROR_RATIO = "CardErrorRatio";
    public static final String FN_CONJ_ERROR_RATIO = "ConjErrorRatio";
    public static final String FN_N_ERROR_RATIO = "NounErrorRatio";
    public static final String FN_O_ERROR_RATIO = "OtherErrorRatio";
    public static final String FN_PP_ERROR_RATIO = "PrepErrorRatio";
    public static final String FN_PR_ERROR_RATIO = "PronErrorRatio";
    public static final String FN_PUNC_ERROR_RATIO = "PuncErrorRatio";
    public static final String FN_V_ERROR_RATIO = "VerbErrorRatio";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
        Set<Feature> featSet = new HashSet<Feature>();

        int nrOfSpellingAnomalies = 0;
        int adjErrors = 0;
        int advErrors = 0;
        int artErrors = 0;
        int cardErrors = 0;
        int conjErrors = 0;
        int nounErrors = 0;
        int otherErrors = 0;
        int prepErrors = 0;
        int pronErrors = 0;
        int puncErrors = 0;
        int verbErrors = 0;

        for (SpellingAnomaly anomaly : JCasUtil.selectCovered(jcas, SpellingAnomaly.class, target)) {
            for (POS pos : JCasUtil.selectCovered(jcas, POS.class, anomaly)) {
                if (pos instanceof POS_ADJ) {
                    adjErrors++;
                }
                else if (pos instanceof POS_ADV) {
                    advErrors++;
                }
                else if (pos instanceof POS_DET) {
                    artErrors++;
                }
                else if (pos instanceof POS_NUM) {
                    cardErrors++;
                }
                else if (pos instanceof POS_CONJ) {
                    conjErrors++;
                }
                else if (pos instanceof POS_NOUN || pos instanceof POS_PROPN) {
                    nounErrors++;
                }
                else if (pos instanceof POS_X) {
                    otherErrors++;
                }
                else if (pos instanceof POS_ADP) {
                    prepErrors++;
                }
                else if (pos instanceof POS_PRON) {
                    pronErrors++;
                }
                else if (pos instanceof POS_PUNCT) {
                    puncErrors++;
                }
                else if (pos instanceof POS_VERB) {
                    verbErrors++;
                }
            }

            nrOfSpellingAnomalies++;
        }

        featSet.add(new Feature(FN_ADJ_ERROR_RATIO, (double) adjErrors
                / nrOfSpellingAnomalies));
        featSet.add(new Feature(FN_ADV_ERROR_RATIO, (double) advErrors
                / nrOfSpellingAnomalies));
        featSet.add(new Feature(FN_ART_ERROR_RATIO, (double) artErrors
                / nrOfSpellingAnomalies));
        featSet.add(new Feature(FN_CARD_ERROR_RATIO, (double) cardErrors
                / nrOfSpellingAnomalies));
        featSet.add(new Feature(FN_CONJ_ERROR_RATIO, (double) conjErrors
                / nrOfSpellingAnomalies));
        featSet.add(new Feature(FN_N_ERROR_RATIO, (double) nounErrors
                / nrOfSpellingAnomalies));
        featSet.add(new Feature(FN_O_ERROR_RATIO, (double) otherErrors
                / nrOfSpellingAnomalies));
        featSet.add(new Feature(FN_PR_ERROR_RATIO, (double) pronErrors
                / nrOfSpellingAnomalies));
        featSet.add(new Feature(FN_PP_ERROR_RATIO, (double) prepErrors
                / nrOfSpellingAnomalies));
        featSet.add(new Feature(FN_PUNC_ERROR_RATIO, (double) puncErrors
                / nrOfSpellingAnomalies));
        featSet.add(new Feature(FN_V_ERROR_RATIO, (double) verbErrors
                / nrOfSpellingAnomalies));

        return featSet;
    }
}