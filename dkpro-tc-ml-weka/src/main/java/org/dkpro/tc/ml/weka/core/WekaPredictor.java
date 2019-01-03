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
package org.dkpro.tc.ml.weka.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.tc.ml.base.TcPredictor;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class WekaPredictor
    extends _eka
    implements TcPredictor
{

    @Override
    public List<String> predict(File data, File model) throws Exception
    {
        Instances weka = toWekaInstances(data, false);
        return predict(weka, model);
    }

    public List<String> predict(Instances data, File model) throws Exception
    {
        Classifier cls = (Classifier) SerializationHelper.read(model.getAbsolutePath());
        return performPrediction(cls, data);
    }

    public List<String> performPrediction(Classifier cl, Instances data) throws Exception
    {

        StringBuffer classVals = new StringBuffer();
        for (int i = 0; i < data.classAttribute().numValues(); i++) {
            if (classVals.length() > 0) {
                classVals.append(",");
            }
            classVals.append(data.classAttribute().value(i));
        }

        // get predictions
        List<String> predictions = new ArrayList<String>();
        for (int i = 0; i < data.size(); i++) {
            Double pred = cl.classifyInstance(data.instance(i));
            predictions.add(pred.toString());
        }

        return predictions;
    }

}
