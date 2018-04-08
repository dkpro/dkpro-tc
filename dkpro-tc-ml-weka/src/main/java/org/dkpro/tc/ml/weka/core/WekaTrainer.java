/**
 * Copyright 2018
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
package org.dkpro.tc.ml.weka.core;

import java.io.File;
import java.util.List;

import org.dkpro.tc.ml.base.TcTrainer;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class WekaTrainer implements TcTrainer
{

    public void train(File data, File model, List<String> parameters) throws Exception
    {

        sanityCheckParameters(parameters);

        Instances wekaData = toWeka(data);

        String algoName = parameters.get(0);
        List<String> algoParameters = parameters.subList(1, parameters.size());

        // build classifier
        Classifier cl = AbstractClassifier.forName(algoName, algoParameters.toArray(new String[0]));
        cl.buildClassifier(wekaData);
        
        weka.core.SerializationHelper.write(model.getAbsolutePath(), cl);
    }

    private Instances toWeka(File data) throws Exception
    {
        Instances train = WekaUtils.getInstances(data, false);
        Instances wekaData = WekaUtils.removeInstanceId(train, false);
        return wekaData;
    }

    private void sanityCheckParameters(List<String> parameters)
    {
        if (parameters == null) {
            throw new NullPointerException("The provided parameters are null");
        }

        if (parameters.size() == 0) {
            throw new IllegalArgumentException(
                    "At least the name (.getClass().getName()) of the Weka classifier has to be provided");
        }
    }

}
