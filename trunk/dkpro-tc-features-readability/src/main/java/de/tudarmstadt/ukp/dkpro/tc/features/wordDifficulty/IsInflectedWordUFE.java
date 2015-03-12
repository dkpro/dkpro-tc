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

package de.tudarmstadt.ukp.dkpro.tc.features.wordDifficulty;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MissingValue;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MissingValue.MissingValueNonNominalType;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.tudarmstadt.ukp.dkpro.tc.features.readability.util.ReadabilityUtils;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos" })
public class IsInflectedWordUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    /**
     * @author beinborn
     * 
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
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {

        POS solutionPos = JCasUtil.selectCovered(jcas, POS.class, classificationUnit).get(0);
        String solutionLemma = JCasUtil.selectCovered(jcas, Lemma.class, classificationUnit).get(0)
                .getValue();
        String token = classificationUnit.getCoveredText();

        String lang = jcas.getDocumentLanguage();

        List<Feature> featList = new ArrayList<Feature>();
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
                                featList.add(new Feature(DERIVED_ADJ, true));
                                break;
                            }
                        }
                    }
                    catch (MissingResourceException e) {
                        // adjective endings are currently only available for English, German, and
                        // French
                        featList.add(new Feature(DERIVED_ADJ, new MissingValue(
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
        featList.add(new Feature(IS_LEMMA, solutionLemma.equals(token)));
        featList.add(new Feature(INFLECTED_NOUN, inflectedNoun));
        featList.add(new Feature(INFLECTED_ADJ, inflectedAdjective));
        featList.add(new Feature(INFLECTED_VERB, inflectedVerb));

        return featList;
    }
}
