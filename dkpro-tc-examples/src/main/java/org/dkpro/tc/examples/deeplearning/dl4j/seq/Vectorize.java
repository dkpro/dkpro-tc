/**
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.examples.deeplearning.dl4j.seq;

import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.tc.examples.deeplearning.dl4j.seq.BinaryWordVectorSerializer.BinaryVectorizer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class Vectorize
{
    // Tagset
    private Object2IntMap<String> tagset = new Object2IntLinkedOpenHashMap<>();

    /**
     * Use for training - tagset will be built internally.
     */
    public Vectorize()
    {
        // Nothing to do
    }
    
    /**
     * Use for tagging - pre-load tagset! Mind that order of tags must be exactly as produced during
     * training.
     */
    public Vectorize(String[] aTagset)
    {
        this();
        
        for (int i = 0; i < aTagset.length; i++) {
            tagset.put(aTagset[i], i);
        }
    }
    
    public DataSet vectorize(List<List<String>> sentences, List<List<String>> sentLabels, BinaryVectorizer wordVectors,
            int truncateLength, int maxTagsetSize, boolean includeLabels)
                throws IOException
    {
        // Feature extractors
        List<Feature> featureGenerators = new ArrayList<>();
        featureGenerators.add(new EmbeddingsFeature(wordVectors));
        // featureGenerators.add(new ShapeFeature());
     
        // Get size of feature vector
        int featureVectorSize = featureGenerators.stream().mapToInt(f -> f.size()).sum();
        
        // If longest sentence exceeds 'truncateLength': only take the first 'truncateLength' words
        int maxSentLength = sentences.stream().mapToInt(tokens -> tokens.size()).max().getAsInt();
        if (maxSentLength > truncateLength) {
            maxSentLength = truncateLength;
        }

        // Create data for training
        // Here: we have sentences.size() examples of varying lengths
        INDArray features = Nd4j.create(sentences.size(), featureVectorSize, maxSentLength);
        // Tags are using a 1-hot encoding
        INDArray labels = Nd4j.create(sentences.size(), maxTagsetSize, maxSentLength);

        // Sentences have variable length, so we we need to mask positions not used in short
        // sentences.
        INDArray featuresMask = Nd4j.zeros(sentences.size(), maxSentLength);
        INDArray labelsMask = Nd4j.zeros(sentences.size(), maxSentLength);

        // Iterate over all sentences
        for (int s = 0; s < sentences.size(); s++) {
            // Get word vectors for each word in review, and put them in the training data
            List<String> tokens = sentences.get(s);
            List<String> tokLabels = sentLabels.get(s);
            for (int t = 0; t < Math.min(tokens.size(), maxSentLength); t++) {
                // Look up embedding
                String token = tokens.get(t);
                INDArray embedding = Nd4j.create(wordVectors.vectorize(token));
                features.put(new INDArrayIndex[]{ point(s), all(), point(t) }, embedding);
                
                // Word is present (not padding) -> 1.0 in features mask
                featuresMask.putScalar(new int[] { s, t }, 1.0);

                String tag = tokLabels.get(t);
                // Grow tagset if necessary
                if (!tagset.containsKey(tag)) {
                    tagset.put(tag, tagset.size());
                }
                
                // Add POS label 
                labels.putScalar(s, tagset.getInt(tag), t, 1.0);
                labelsMask.putScalar(new int[] { s, t }, 1.0);
            }
        }

        return new DataSet(features, labels, featuresMask, labelsMask);
    }
    
    public String[] getTagset()
    {
        return tagset.keySet().toArray(new String[tagset.size()]);
    }
}
