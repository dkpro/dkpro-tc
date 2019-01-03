/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.features.pair.similarity;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.similarity.algorithms.api.SimilarityException;
import org.dkpro.similarity.algorithms.lexical.string.GreedyStringTiling;

/**
 * Extracts the document pair similarity using the GreedyStringTiling measure
 */
public class GreedyStringTilingFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    protected GreedyStringTiling measure = new GreedyStringTiling(3);

    @Override
    public Set<Feature> extract(JCas view1, JCas view2) throws TextClassificationException
    {

        try {
            double similarity = measure.getSimilarity(view1.getDocumentText(),
                    view2.getDocumentText());

            return new Feature("Similarity" + measure.getName(), similarity, FeatureType.NUMERIC)
                    .asSet();
        }
        catch (SimilarityException e) {
            throw new TextClassificationException(e);
        }
    }
}