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
package org.dkpro.tc.features.pair.core.chunk;

import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.PairFeatureExtractor;

/**
 * Pair-wise feature extractor Computes the average token length of all noun chunks in a view and
 * returns the difference of both views.
 */
public class DiffNounChunkTokenLength
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    @Override
    public Set<Feature> extract(JCas view1, JCas view2) throws TextClassificationException
    {
        return new Feature("DiffNounPhraseTokenLength",
                getAverageNounPhraseTokenLength(view1) - getAverageNounPhraseTokenLength(view2),
                FeatureType.NUMERIC).asSet();

    }

    /**
     * Returns average token length of chunks in a view
     * 
     * @param view
     *            the view of the JCas
     * @return average token length of all chunks
     */
    private double getAverageNounPhraseTokenLength(JCas view)
    {
        int totalNumber = 0;
        for (Chunk chunk : JCasUtil.select(view, Chunk.class)) {
            totalNumber += JCasUtil.selectCovered(view, Token.class, chunk).size();
        }
        return totalNumber / (double) JCasUtil.select(view, Chunk.class).size();
    }
}
