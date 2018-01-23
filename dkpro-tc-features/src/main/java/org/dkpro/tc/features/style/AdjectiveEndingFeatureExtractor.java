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

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADV;

/**
 * Gender-Preferential Text Mining of E-mail Discourse
 * Malcolm Corney, Olivier de Vel, Alison Anderson, George Mohay
 * 
 * Counts ratio of English adjective and adverb endings 
 * (in proportion to all adjectives/adverbs)
 * that may signalize neuroticism,
 * respectively express the emotional level of a person. Can be used
 * for autorship attribution or such style-related tasks.
 * 
 * Output is multiplied by 100 to avoid too small numbers.
 */
public class AdjectiveEndingFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    public static final String ADJ_ENDING1 = "EndingAble";
    public static final String ADJ_ENDING2 = "EndingAl";
    public static final String ADJ_ENDING3 = "EndingFul";
    public static final String ADJ_ENDING4 = "EndingIble";
    public static final String ADJ_ENDING5 = "EndingLess";
    public static final String ADJ_ENDING6 = "EndingOus";
    public static final String ADJ_ENDING7 = "EndingIve";
    public static final String ADJ_ENDING8 = "EndingIc";
    
    public static final String ADV_ENDING9 = "EndingLy"; // adverb, but anyway

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
    {

        double able = 0;
        double al = 0;
        double ful = 0;
        double ible = 0;
        double ic = 0;
        double ive = 0;
        double less = 0;
        double ous = 0;
        double ly = 0;

        int n = 0;
        for (POS_ADJ adj : JCasUtil.selectCovered(jcas, POS_ADJ.class, target)) {
            n++;

            String text = adj.getCoveredText().toLowerCase();
            if (text.endsWith("able")) {
                able++;
            }
            else if (text.endsWith("al")) {
                al++;
            }
            else if (text.endsWith("ful")) {
                ful++;
            }
            else if (text.endsWith("ible")) {
                ible++;
            }
            else if (text.endsWith("ic")) {
                ic++;
            }
            else if (text.endsWith("ive")) {
                ive++;
            }
            else if (text.endsWith("less")) {
                less++;
            }
            else if (text.endsWith("ous")) {
                ous++;
            }
        }

        int m = 0;
        for (POS_ADV adv : JCasUtil.select(jcas, POS_ADV.class)) {
            m++;

            String text = adv.getCoveredText().toLowerCase();
            if (text.endsWith("ly")) {
                ly++;
            }
        }

		Set<Feature> featSet = new HashSet<Feature>();
		featSet.add(new Feature(ADJ_ENDING1, n > 0 ? able * 100 / n : 0, n == 0));
		featSet.add(new Feature(ADJ_ENDING2, n > 0 ? al * 100 / n : 0, n == 0));
		featSet.add(new Feature(ADJ_ENDING3, n > 0 ? ful * 100 / n : 0, n == 0));
		featSet.add(new Feature(ADJ_ENDING4, n > 0 ? ible * 100 / n : 0, n == 0));
		featSet.add(new Feature(ADJ_ENDING5, n > 0 ? less * 100 / n : 0, n == 0));
		featSet.add(new Feature(ADJ_ENDING6, n > 0 ? ous * 100 / n : 0, n == 0));
		featSet.add(new Feature(ADJ_ENDING7, n > 0 ? ive * 100 / n : 0, n == 0));
		featSet.add(new Feature(ADJ_ENDING8, n > 0 ? ic * 100 / n : 0, n == 0));

		featSet.add(new Feature(ADV_ENDING9, m > 0 ? ly * 100 / m : 0, n == 0));

        return featSet;
    }
}