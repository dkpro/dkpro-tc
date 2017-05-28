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
