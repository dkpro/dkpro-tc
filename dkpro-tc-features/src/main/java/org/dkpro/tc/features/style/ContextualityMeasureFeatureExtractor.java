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
package org.dkpro.tc.features.style;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureSet;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_DET;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NOUN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PRON;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_VERB;

/**
 * Heylighen &amp; Dewaele (2002): Variation in the contextuality of language The contextuality
 * measure can reach values 0-100 The higher value, the more formal (male) style the text is, i.e.
 * contains many nouns, verbs, determiners. The lower value, the more contextual (female) style the
 * text is, i.e. contains many adverbs, pronouns and such.
 * <p>
 * Extracts also values for each pos class, as they are calculated anyway
 */

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class ContextualityMeasureFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    public static final String CONTEXTUALITY_MEASURE_FN = "ContextualityMeasure";

    @Override
    public FeatureSet extract(JCas jcas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
    		FeatureSet featSet = new FeatureSet();

        double total = selectCovered(jcas, POS.class, aTarget).size();
        double noun = selectCovered(jcas, POS_NOUN.class, aTarget).size() / total;
        double adj = selectCovered(jcas, POS_ADJ.class, aTarget).size() / total;
        double prep = selectCovered(jcas, POS_ADP.class, aTarget).size() / total;
        double art = selectCovered(jcas, POS_DET.class, aTarget).size() / total;// !includes
                                                                                // determiners
        double pro = selectCovered(jcas, POS_PRON.class, aTarget).size() / total;
        double verb = selectCovered(jcas, POS_VERB.class, aTarget).size() / total;
        double adv = selectCovered(jcas, POS_ADV.class, aTarget).size() / total;

        // noun freq + adj.freq. + prepositions freq. + article freq. - pronoun freq. - verb f. -
        // adverb - interjection + 100
        double contextualityMeasure = 0.5 * (noun + adj + prep + art - pro - verb - adv + 100);

        featSet.add(new Feature("NounRate", noun, FeatureType.NUMERIC));
        featSet.add(new Feature("AdjectiveRate", adj, FeatureType.NUMERIC));
        featSet.add(new Feature("PrepositionRate", prep, FeatureType.NUMERIC));
        featSet.add(new Feature("ArticleRate", art, FeatureType.NUMERIC));
        featSet.add(new Feature("PronounRate", pro, FeatureType.NUMERIC));
        featSet.add(new Feature("VerbRate", verb, FeatureType.NUMERIC));
        featSet.add(new Feature("AdverbRate", adv, FeatureType.NUMERIC));
        featSet.add(
                new Feature(CONTEXTUALITY_MEASURE_FN, contextualityMeasure, FeatureType.NUMERIC));

        return featSet;
    }

}
