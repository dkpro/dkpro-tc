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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Extracts the ratio of plural to total nouns.
 * 
 * Works for Penn Treebank POS tags only.
 */
public class PluralRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_PLURAL_RATIO = "PluralRatio";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        int plural = 0;
        int singular = 0;

        for (POS tag : JCasUtil.select(jcas, N.class)) {
            // FIXME Issue 123: depends on tagset
            if ((tag.getPosValue().equals("NNS")) || (tag.getPosValue().equals("NNPS"))
                    || (tag.getPosValue().equals("NNS"))) {
                plural++;
            }
            else if ((tag.getPosValue().equals("NNP")) || (tag.getPosValue().equals("NN"))) {
                singular++;
            }
        }
        List<Feature> featList = new ArrayList<Feature>();
        if ((singular + plural) > 0) {
            featList.add(new Feature(FN_PLURAL_RATIO, (double) plural
                    / (singular + plural)));
        }
        return featList;
    }
}