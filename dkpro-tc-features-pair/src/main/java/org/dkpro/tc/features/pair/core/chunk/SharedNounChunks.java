/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.features.pair.core.chunk;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.PairFeatureExtractor;

/**
 * Pair-wise feature extractor. Computes how many noun chunks two views share.
 */
public class SharedNounChunks
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    public static final String PARAM_NORMALIZE_WITH_FIRST = "NormalizeWithFirst";
    @ConfigurationParameter(name = PARAM_NORMALIZE_WITH_FIRST, mandatory = false, defaultValue = "true")
    protected boolean normalizeWithFirst;

    @Override
    public Set<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {

        if (normalizeWithFirst) {
            return new Feature("SharedNounChunkView1", getSharedNounChunksCount(view1, view2)).asSet();
        }
        else {
            return new Feature("SharedNounChunkView2", getSharedNounChunksCount(view2, view1)).asSet();
        }

    }

    /**
     * Computes the ratio of shared nouns
     * 
     * @param view1
     *            First view to be processed
     * @param view2
     *            Second view to be processed
     * @return The quotient of shared noun chunks in both views and noun chunks in the first view
     */
    private double getSharedNounChunksCount(JCas view1, JCas view2)
    {

        Set<String> chunks1 = new HashSet<String>();
        for (Chunk chunk : JCasUtil.select(view1, Chunk.class)) {
            chunks1.add(chunk.getCoveredText());
        }
        Set<String> chunks2 = new HashSet<String>();
        for (Chunk chunk : JCasUtil.select(view2, Chunk.class)) {
            chunks2.add(chunk.getCoveredText());
        }
        chunks1.retainAll(chunks2);
        Double result = chunks1.size() / (double) JCasUtil.select(view1, Chunk.class).size();

        return result.equals(Double.NaN) ? 0. : result;
    }

}
