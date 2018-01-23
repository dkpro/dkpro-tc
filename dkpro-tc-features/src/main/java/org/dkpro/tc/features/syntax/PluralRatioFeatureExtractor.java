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

import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NOUN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

/**
 * Extracts the ratio of plural to total nouns.
 * 
 * Works for Penn Treebank POS tags only.
 */
public class PluralRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    public static final String FN_PLURAL_RATIO = "PluralRatio";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
        int plural = 0;
        int singular = 0;

        for (POS tag : JCasUtil.selectCovered(jcas, POS_NOUN.class, target)) {
            // FIXME Issue 123: depends on tagset
            if ((tag.getPosValue().equals("NNS")) || (tag.getPosValue().equals("NNPS"))
                    || (tag.getPosValue().equals("NNS"))) {
                plural++;
            }
            else if ((tag.getPosValue().equals("NNP")) || (tag.getPosValue().equals("NN"))) {
                singular++;
            }
        }
        if ((singular + plural) > 0) {
            return new Feature(FN_PLURAL_RATIO, (double) plural
                    / (singular + plural)).asSet();
        }
        else {
        	return new Feature(FN_PLURAL_RATIO, 0.0).asSet();
        }
    }
}