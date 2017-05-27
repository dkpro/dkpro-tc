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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class ShapeFeature
    implements Feature
{
    public static final int CASE = 0;

    @Override
    public INDArray apply(String aWord)
        throws IOException
    {
        INDArray vector = Nd4j.zeros(1);
        if (aWord.length() > 0) {
            vector.putScalar(CASE, Character.isUpperCase(aWord.charAt(0)) ? 1 : 0);
        }

        return vector;
    }

    @Override
    public int size()
    {
        return 1;
    }
}
