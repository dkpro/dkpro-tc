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
package org.dkpro.tc.features.syntax;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CARD;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.DocumentFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Extracts the ratio of each universal POS tags to the total number of tags 
 */
public class POSRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_ADJ_RATIO = "AdjRatioFeature";
    public static final String FN_ADV_RATIO = "AdvRatioFeature";
    public static final String FN_ART_RATIO = "ArtRatioFeature";
    public static final String FN_CARD_RATIO = "CardRatioFeature";
    public static final String FN_CONJ_RATIO = "ConjRatioFeature";
    public static final String FN_N_RATIO = "NRatioFeature";
    public static final String FN_O_RATIO = "ORatioFeature";
    public static final String FN_PP_RATIO = "PpRatioFeature";
    public static final String FN_PR_RATIO = "PrRatioFeature";
    public static final String FN_PUNC_RATIO = "PuncRatioFeature";
    public static final String FN_V_RATIO = "VRatioFeature";

    @Override
    public Set<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        Set<Feature> features = new HashSet<Feature>();

        double total = JCasUtil.select(jcas, POS.class).size();
        double adj = select(jcas, ADJ.class).size() / total;
        double adv = select(jcas, ADV.class).size() / total;
        double art = select(jcas, ART.class).size() / total;
        double card = select(jcas, CARD.class).size() / total;
        double conj = select(jcas, CONJ.class).size() / total;
        double noun = select(jcas, N.class).size() / total;
        double other = select(jcas, O.class).size() / total;
        double prep = select(jcas, PP.class).size() / total;
        double pron = select(jcas, PR.class).size() / total;
        double punc = select(jcas, PUNC.class).size() / total;
        double verb = select(jcas, V.class).size() / total;

        features.add(new Feature(FN_ADJ_RATIO, adj));
        features.add(new Feature(FN_ADV_RATIO, adv));
        features.add(new Feature(FN_ART_RATIO, art));
        features.add(new Feature(FN_CARD_RATIO, card));
        features.add(new Feature(FN_CONJ_RATIO, conj));
        features.add(new Feature(FN_N_RATIO, noun));
        features.add(new Feature(FN_O_RATIO, other));
        features.add(new Feature(FN_PR_RATIO, pron));
        features.add(new Feature(FN_PP_RATIO, prep));
        features.add(new Feature(FN_PUNC_RATIO, punc));
        features.add(new Feature(FN_V_RATIO, verb));

        return features;
    }
}