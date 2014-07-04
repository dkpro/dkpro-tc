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
package de.tudarmstadt.ukp.dkpro.tc.features.syntax;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Extracts the ratio of the 6 major English pronouns to the total pronouns
 * 
 * English only.
 */
public class PronounRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_I_RATIO = "PronounRatioI";
    public static final String FN_HE_RATIO = "PronounRatioHe";
    public static final String FN_SHE_RATIO = "PronounRatioShe";
    public static final String FN_WE_RATIO = "PronounRatioWe";
    public static final String FN_THEY_RATIO = "PronounRatioThey";
    public static final String FN_US_RATIO = "PronounRatioUs";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {

        int heCount = 0;
        int sheCount = 0;
        int iCount = 0;
        int weCount = 0;
        int theyCount = 0;
        int usCount = 0;

        int n = 0;
        for (PR pronoun : JCasUtil.select(jcas, PR.class)) {
            n++;

            String text = pronoun.getCoveredText().toLowerCase();
            if (text.equals("he")) {
                heCount++;
            }
            else if (text.equals("she")) {
                sheCount++;
            }
            else if (text.equals("i")) {
                iCount++;
            }
            else if (text.equals("we")) {
                weCount++;
            }
            else if (text.equals("they")) {
                theyCount++;
            }
            else if (text.equals("us")) {
                usCount++;
            }
        }

        List<Feature> featList = new ArrayList<Feature>();
        if (n > 0) {
            featList.add(new Feature(FN_HE_RATIO, (double) heCount / n));
            featList.add(new Feature(FN_SHE_RATIO, (double) sheCount / n));
            featList.add(new Feature(FN_I_RATIO, (double) iCount / n));
            featList.add(new Feature(FN_WE_RATIO, (double) weCount / n));
            featList.add(new Feature(FN_THEY_RATIO, (double) theyCount / n));
            featList.add(new Feature(FN_US_RATIO, (double) usCount / n));
        }

        return featList;
    }
}