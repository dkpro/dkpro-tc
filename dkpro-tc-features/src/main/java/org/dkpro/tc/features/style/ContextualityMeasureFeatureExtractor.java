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
package org.dkpro.tc.features.style;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

/**
 * Heylighen &amp; Dewaele (2002): Variation in the contextuality of language The contextuality
 * measure can reach values 0-100 The higher value, the more formal (male) style the text is, i.e.
 * contains many nouns, verbs, determiners. The lower value, the more contextual (female) style the
 * text is, i.e. contains many adverbs, pronouns and such.
 * <p>
 * Extracts also values for each pos class, as they are calculated anyway
 */
public class ContextualityMeasureFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    public static final String CONTEXTUALITY_MEASURE_FN = "ContextualityMeasure";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationUnit target)
        throws TextClassificationException
    {
        Set<Feature> featSet = new HashSet<Feature>();

        double total = JCasUtil.selectCovered(jcas, POS.class, target).size();
        double noun = selectCovered(jcas, N.class, target).size() / total;
        double adj = selectCovered(jcas, ADJ.class, target).size() / total;
        double prep = selectCovered(jcas, PP.class, target).size() / total;
        double art = selectCovered(jcas, ART.class, target).size() / total;// !includes determiners
        double pro = selectCovered(jcas, PR.class, target).size() / total;
        double verb = selectCovered(jcas, V.class, target).size() / total;
        double adv = selectCovered(jcas, ADV.class, target).size() / total;

        int interjCount = 0;
        for (POS tag : JCasUtil.select(jcas, O.class)) {
            // FIXME Issue 123: this is tagset specific
            if (tag.getPosValue().contains("UH")) {
                interjCount++;
            }
        }
        double interj = interjCount / total;

        // noun freq + adj.freq. + prepositions freq. + article freq. - pronoun freq. - verb f. -
        // adverb - interjection + 100
        double contextualityMeasure = 0.5
                * (noun + adj + prep + art - pro - verb - adv - interj + 100);

        featSet.add(new Feature("NounRate", noun));
        featSet.add(new Feature("AdjectiveRate", adj));
        featSet.add(new Feature("PrepositionRate", prep));
        featSet.add(new Feature("ArticleRate", art));
        featSet.add(new Feature("PronounRate", pro));
        featSet.add(new Feature("VerbRate", verb));
        featSet.add(new Feature("AdverbRate", adv));
        featSet.add(new Feature("InterjectionRate", interj));
        featSet.add(new Feature(CONTEXTUALITY_MEASURE_FN, contextualityMeasure));

        return featSet;
    }

}
