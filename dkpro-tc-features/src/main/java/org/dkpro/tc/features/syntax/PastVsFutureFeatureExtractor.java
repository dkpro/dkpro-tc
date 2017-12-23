/*******************************************************************************
 * Copyright 2017
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

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_VERB;

/**
 * Quick, very simplified approximation of usage of past tense in comparison to present/future tense
 * in the text.
 * 
 * Works for Penn Treebank POS tags only.
 * 
 * Captures the ratio of all verbs to "VBD" (verb praeterite) and "VBN" (verb past participle) as
 * past and "VB" (verb base form), "VBP" (verb present) and "VBZ" (verb present 3rd pers sg) as
 * present/future. The output is multiplied by 100 as the values are usually very small.
 */
public class PastVsFutureFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    public static final String FN_PAST_RATIO = "PastVerbRatio";
    public static final String FN_FUTURE_RATIO = "FutureVerbRatio";
    public static final String FN_FUTURE_VS_PAST_RATIO = "FutureVsPastVerbRatio";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
    {
        double pastRatio = 0.0;
        double futureRatio = 0.0;
        double futureToPastRatio = 0.0;
        int pastVerbs = 0;
        int futureVerbs = 0;
        int verbs = 0;

        for (POS_VERB tag : JCasUtil.selectCovered(jcas, POS_VERB.class, target)) {
            verbs++;
            // FIXME Issue 123: depends on tagset
            if (tag.getPosValue().contains("VBD") || tag.getPosValue().contains("VBN")) {
                pastVerbs++;
            }
            if (tag.getPosValue().contains("VB") || tag.getPosValue().contains("VBP")
                    || tag.getPosValue().contains("VBZ")) {
                futureVerbs++;
            }
        }
        if (verbs > 0) {
            pastRatio = (double) pastVerbs * 100 / verbs;
            futureRatio = (double) futureVerbs * 100 / verbs;
        }
        if ((pastRatio > 0) && (futureRatio > 0)) {
            futureToPastRatio = futureRatio / pastRatio;
        }

        Set<Feature> features = new HashSet<Feature>();
        features.add(new Feature(FN_PAST_RATIO, pastRatio));
        features.add(new Feature(FN_FUTURE_RATIO, futureRatio));
        features.add(new Feature(FN_FUTURE_VS_PAST_RATIO, futureToPastRatio));

        return features;
    }
}
