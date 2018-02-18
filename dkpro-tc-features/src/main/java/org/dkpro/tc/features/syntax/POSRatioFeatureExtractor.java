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
package org.dkpro.tc.features.syntax;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

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
 * Extracts the ratio of each universal POS tags to the total number of tags
 */
@TypeCapability(inputs = {
"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"
})
public class POSRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
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
    public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        Set<Feature> features = new HashSet<Feature>();
        double total = selectCovered(jcas, POS.class, aTarget).size();
        double adj = selectCovered(jcas, POS_ADJ.class, aTarget).size() / total;
        double adv = selectCovered(jcas, POS_ADV.class, aTarget).size() / total;
        double art = selectCovered(jcas, POS_DET.class, aTarget).size() / total;
        double card = selectCovered(jcas, POS_NUM.class, aTarget).size() / total;
        double conj = selectCovered(jcas, POS_CONJ.class, aTarget).size() / total;
        double noun = selectCovered(jcas, POS_NOUN.class, aTarget).size() / total;
        double propNoun = selectCovered(jcas, POS_PROPN.class, aTarget).size() / total;
        double other = selectCovered(jcas, POS_X.class, aTarget).size() / total;
        double prep = selectCovered(jcas, POS_ADP.class, aTarget).size() / total;
        double pron = selectCovered(jcas, POS_PRON.class, aTarget).size() / total;
        double punc = selectCovered(jcas, POS_PUNCT.class, aTarget).size() / total;
        double verb = selectCovered(jcas, POS_VERB.class, aTarget).size() / total;

        features.add(new Feature(FN_ADJ_RATIO, adj, FeatureType.NUMERIC));
        features.add(new Feature(FN_ADV_RATIO, adv, FeatureType.NUMERIC));
        features.add(new Feature(FN_ART_RATIO, art, FeatureType.NUMERIC));
        features.add(new Feature(FN_CARD_RATIO, card, FeatureType.NUMERIC));
        features.add(new Feature(FN_CONJ_RATIO, conj, FeatureType.NUMERIC));
        features.add(new Feature(FN_N_RATIO, noun+propNoun, FeatureType.NUMERIC));
        features.add(new Feature(FN_O_RATIO, other, FeatureType.NUMERIC));
        features.add(new Feature(FN_PR_RATIO, pron, FeatureType.NUMERIC));
        features.add(new Feature(FN_PP_RATIO, prep, FeatureType.NUMERIC));
        features.add(new Feature(FN_PUNC_RATIO, punc, FeatureType.NUMERIC));
        features.add(new Feature(FN_V_RATIO, verb, FeatureType.NUMERIC));

        return features;
    }
}