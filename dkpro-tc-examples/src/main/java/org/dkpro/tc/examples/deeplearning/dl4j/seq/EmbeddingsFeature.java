/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.tc.examples.deeplearning.dl4j.seq;

import java.io.IOException;

import org.dkpro.tc.examples.deeplearning.dl4j.seq.BinaryWordVectorSerializer.BinaryVectorizer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class EmbeddingsFeature
    implements Feature
{
    private BinaryVectorizer wordVectors;

    public EmbeddingsFeature(BinaryVectorizer aWordVectors)
        throws IOException
    {
        wordVectors = aWordVectors;
    }

    @Override
    public INDArray apply(String aWord)
        throws IOException
    {
        return Nd4j.create(wordVectors.vectorize(aWord));
    }

    @Override
    public int size()
    {
        return wordVectors.getVectorSize();
    }
}
