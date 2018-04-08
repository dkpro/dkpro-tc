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

import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MekaTrainer
    extends _eka
    implements TcTrainer
{

    private boolean serializeModel;

    /**
     * Trains a Meka classifier
     * @param serializeModel
     *            if the model shall be serialized. Some weka models are not serializable.
     */
    public MekaTrainer(boolean serializeModel)
    {
        this.serializeModel = serializeModel;
    }

    @Override
    public void train(File data, File model, List<String> parameters) throws Exception
    {
        train(toWekaInstances(data, true), model, parameters);
    }

    public Classifier train(Instances data, File model, List<String> parameters) throws Exception
    {
        List<String> mlArgs = parameters.subList(1, parameters.size());
        MultiLabelClassifier cl = (MultiLabelClassifier) AbstractClassifier
                .forName((String) parameters.get(0), new String[] {});
        if (!mlArgs.isEmpty()) {
            cl.setOptions(mlArgs.toArray(new String[0]));
        }
        cl.buildClassifier(data);

        if (serializeModel) {
            weka.core.SerializationHelper.write(model.getAbsolutePath(), cl);
        }

        return cl;
    }

}
