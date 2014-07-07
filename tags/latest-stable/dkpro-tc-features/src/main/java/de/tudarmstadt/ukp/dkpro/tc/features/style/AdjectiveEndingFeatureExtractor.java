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
package de.tudarmstadt.ukp.dkpro.tc.features.style;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

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
    implements DocumentFeatureExtractor
{
    public static final String FN_ENDING1 = "EndingAble";
    public static final String FN_ENDING2 = "EndingAl";
    public static final String FN_ENDING3 = "EndingFul";
    public static final String FN_ENDING4 = "EndingIble";
    public static final String FN_ENDING5 = "EndingLess";
    public static final String FN_ENDING6 = "EndingOus";
    public static final String FN_ENDING7 = "EndingIve";
    public static final String FN_ENDING8 = "EndingIc";
    public static final String FN_ENDING9 = "EndingLy"; // adverb, but anyway

    @Override
    public List<Feature> extract(JCas jcas)
    {

        int able = 0;
        int al = 0;
        int ful = 0;
        int ible = 0;
        int ic = 0;
        int ive = 0;
        int less = 0;
        int ous = 0;
        int ly = 0;

        int n = 0;
        for (ADJ adj : JCasUtil.select(jcas, ADJ.class)) {
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
        for (ADV adv : JCasUtil.select(jcas, ADV.class)) {
            m++;

            String text = adv.getCoveredText().toLowerCase();
            if (text.endsWith("ly")) {
                ly++;
            }
        }

        List<Feature> featList = new ArrayList<Feature>();
        if (n > 0) {
            featList.add(new Feature(FN_ENDING1, (double) able * 100 / n));
            featList.add(new Feature(FN_ENDING2, (double) al * 100 / n));
            featList.add(new Feature(FN_ENDING3, (double) ful * 100 / n));
            featList.add(new Feature(FN_ENDING4, (double) ible * 100 / n));
            featList.add(new Feature(FN_ENDING5, (double) less * 100 / n));
            featList.add(new Feature(FN_ENDING6, (double) ous * 100 / n));
            featList.add(new Feature(FN_ENDING7, (double) ive * 100 / n));
            featList.add(new Feature(FN_ENDING8, (double) ic * 100 / n));
        }
        if (m > 0) {
            featList.add(new Feature(FN_ENDING9, (double) ly * 100 / m));
        }

        return featList;
    }
}