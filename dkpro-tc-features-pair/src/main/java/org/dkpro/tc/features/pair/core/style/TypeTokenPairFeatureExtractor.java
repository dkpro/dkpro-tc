/*******************************************************************************
 * Copyright 2015
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
package org.dkpro.tc.features.pair.core.style;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.PairFeatureExtractor;

/**
 * Pair-wise feature extractor Computes the type-token-ratio in a view and returns the difference of
 * type-token-rations in both views.
 */
public class TypeTokenPairFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    @Override
    public Set<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        return new Feature("DiffTypeTokenRatio",
                        getTypeTokenRatio(view1) / getTypeTokenRatio(view2)
                ).asSet();
    }

    /**
     * 
     * @param view
     *            the view for which the type-token-ratio is computed
     * @return type-token-ratio
     */
    private double getTypeTokenRatio(JCas view)
    {
        Set<String> types = new HashSet<String>();
        for (Lemma lemma : JCasUtil.select(view, Lemma.class)) {
            types.add(lemma.getValue());
        }
        return types.size() / (double) JCasUtil.select(view, Lemma.class).size();
    }
}
