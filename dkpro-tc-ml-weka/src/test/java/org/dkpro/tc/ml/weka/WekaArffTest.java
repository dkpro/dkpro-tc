/**
 * Copyright 2019
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
package org.dkpro.tc.ml.weka;

import java.io.File;

import org.dkpro.tc.ml.weka.core._eka;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

/**
 * A test setup for native Weka access
 */
public class WekaArffTest
{

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        File train = new File("src/main/resources/arff/manyInstances/train.arff.gz");
        File test = new File("src/main/resources/arff/manyInstances/test.arff.gz");

        Instances trainData = _eka.getInstances(train, false);
        Instances testData = _eka.getInstances(test, false);

        Classifier cl = new NaiveBayes();

        // no problems until here
        Evaluation eval = new Evaluation(trainData);
        eval.evaluateModel(cl, testData);
    }
}
