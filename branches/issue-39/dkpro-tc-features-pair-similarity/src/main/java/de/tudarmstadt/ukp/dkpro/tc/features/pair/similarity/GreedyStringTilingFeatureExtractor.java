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
package de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import dkpro.similarity.algorithms.api.SimilarityException;
import dkpro.similarity.algorithms.lexical.string.GreedyStringTiling;

/**
 * Extracts the document pair similarity using the GreedyStringTiling measure
 */
public class GreedyStringTilingFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    protected GreedyStringTiling measure = new GreedyStringTiling(3);

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {

        try {
            double similarity = measure.getSimilarity(view1.getDocumentText(),
                    view2.getDocumentText());

            return Arrays.asList(new Feature("Similarity" + measure.getName(), similarity));
        }
        catch (SimilarityException e) {
            throw new TextClassificationException(e);
        }
    }
}