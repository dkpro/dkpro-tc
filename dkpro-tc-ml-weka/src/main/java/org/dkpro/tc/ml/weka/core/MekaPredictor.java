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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.dkpro.tc.ml.base.TcPredictor;

import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class MekaPredictor
    extends _eka
    implements TcPredictor
{
    
    private double threshold;

    public MekaPredictor(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public List<String> predict(File data, File model) throws Exception
    {
        Instances meka = toWekaInstances(data, true);
        return predict(meka, model);
    }

    public List<String> predict(Instances data, File model) throws Exception
    {
        MultiLabelClassifier cls = (MultiLabelClassifier) SerializationHelper
                .read(model.getAbsolutePath());
        return performPrediction(cls, data);
    }

    public List<String> performPrediction(Classifier cl, Instances data) throws Exception
    {
        List<String> results = new ArrayList<>();
        for (int j = 0; j < data.size(); j++) {

            double[] vals = null;
            try {
                vals = cl.distributionForInstance(data.instance(j));
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
            List<String> outcomes = new ArrayList<String>();
            for (int i = 0; i < vals.length; i++) {
                if (vals[i] >= threshold) {
                    String label = data.instance(j).attribute(i).name();
                    outcomes.add(label);
                }
            }

            results.add(StringUtils.join(outcomes, ","));
        }
        return results;
    }

}
