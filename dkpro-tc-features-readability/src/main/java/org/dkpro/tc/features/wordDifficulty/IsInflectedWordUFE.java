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

package org.dkpro.tc.features.wordDifficulty;

import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.MissingValue;
import org.dkpro.tc.api.features.MissingValue.MissingValueNonNominalType;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.readability.util.ReadabilityUtils;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos" })
public class IsInflectedWordUFE
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    /**
     *         In this feature, we assume that the word is inflected if it does not equal the lemma.
     *         This is slightly simplified. From a strict linguistic view, there also exists
     *         so-called zero derivation. The distinction between derivation and inflection is also
     *         a little weak in this feature extractor.
     */

    public static final String IS_LEMMA = "IsLemma";
    public static final String INFLECTED_ADJ = "IsInflectedAdjective";
    public static final String INFLECTED_NOUN = "IsInflectedNoun";
    public static final String DERIVED_ADJ = "IsDerivedAdjective";
    public static final String INFLECTED_VERB = "IsInflectedVerb";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget classificationUnit)
        throws TextClassificationException
    {

        POS solutionPos = JCasUtil.selectCovered(jcas, POS.class, classificationUnit).get(0);
        String solutionLemma = JCasUtil.selectCovered(jcas, Lemma.class, classificationUnit).get(0)
                .getValue();
        String token = classificationUnit.getCoveredText();

        String lang = jcas.getDocumentLanguage();

        Set<Feature> featSet = new HashSet<Feature>();
        boolean inflectedAdjective = false;
        boolean inflectedNoun = false;
        boolean inflectedVerb = false;

        if (solutionPos != null) {
            if (!token.equals(solutionLemma)) {
                if (solutionPos instanceof ADJ) {
                    inflectedAdjective = true;
                    String[] adjectiveEndings;
                    try {
                        adjectiveEndings = ReadabilityUtils.getAdjectiveEndings(lang);
                        for (String ending : adjectiveEndings) {
                            if (token.endsWith(ending)) {
                                featSet.add(new Feature(DERIVED_ADJ, true));
                                break;
                            }
                        }
                    }
                    catch (MissingResourceException e) {
                        // adjective endings are currently only available for English, German, and
                        // French
                        featSet.add(new Feature(DERIVED_ADJ, new MissingValue(
                                MissingValueNonNominalType.BOOLEAN)));
                    }

                }

                if (solutionPos instanceof N) {
                    inflectedNoun = true;
                }

                if (solutionPos instanceof V) {
                    inflectedVerb = true;
                }
            }
        }
        featSet.add(new Feature(IS_LEMMA, solutionLemma.equals(token)));
        featSet.add(new Feature(INFLECTED_NOUN, inflectedNoun));
        featSet.add(new Feature(INFLECTED_ADJ, inflectedAdjective));
        featSet.add(new Feature(INFLECTED_VERB, inflectedVerb));

        return featSet;
    }
}
