/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.core.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;

/**
 * Convenience class that builds a parameter space object that can be passed to a DKPro Lab
 * experiment.
 */
public class ExperimentBuilder
    implements Constants
{
    List<TcShallowLearningAdapter> adapter = new ArrayList<>();
    List<String[]> arguments = new ArrayList<>();
    String learningMode = null;
    String featureMode = null;
    Map<String, Object> trainTestReaders = null;
    List<TcFeatureSet> featureSets = new ArrayList<>();

    public void addAdapterConfiguration(TcShallowLearningAdapter adapter, String... arguments)
    {
        this.adapter.add(adapter);
        this.arguments.add(arguments);
    }

    public ParameterSpace build()
    {
        List<Dimension<?>> dimensions = new ArrayList<>();

        List<Map<String, Object>> adapterMaps = getAdapterInfo();

        @SuppressWarnings("unchecked")
        Map<String, Object>[] array = adapterMaps.toArray(new Map[0]);
        Dimension<Map<String, Object>> mlaDim = Dimension
                .createBundle("machineLearningAdapterConfigurations", array);
        dimensions.add(mlaDim);

        dimensions.add(addFeatureMode());
        dimensions.add(addLearningMode());
        dimensions.add(addFeatureSets());
        dimensions.add(addReaders());

        ParameterSpace ps = new ParameterSpace();
        ps.setDimensions(dimensions.toArray(new Dimension<?>[0]));

        return ps;
    }

    private Dimension<?> addReaders()
    {
        return Dimension.createBundle(DIM_READERS, trainTestReaders);
    }

    private Dimension<?> addFeatureSets()
    {
        if (featureSets.isEmpty()) {
            throw new IllegalStateException(
                    "No feature sets provided, please provide at least one feature set i.e. ["
                            + TcFeatureSet.class.getName() + "]");
        }

        return Dimension.create(DIM_FEATURE_SET, featureSets.toArray(new TcFeatureSet[0]));
    }

    private Dimension<?> addLearningMode()
    {
        if (learningMode == null) {
            throw new NullPointerException(
                    "No learning mode set, please provide this information via the respective setter method");
        }

        return Dimension.create(DIM_LEARNING_MODE, learningMode);
    }

    private Dimension<?> addFeatureMode()
    {
        if (featureMode == null) {
            throw new NullPointerException(
                    "No feature mode set, please provide this information via the respective setter method");
        }

        return Dimension.create(DIM_FEATURE_MODE, featureMode);
    }

    private List<Map<String, Object>> getAdapterInfo()
    {
        List<Map<String, Object>> maps = new ArrayList<>();

        for (int i = 0; i < adapter.size(); i++) {
            TcShallowLearningAdapter a = adapter.get(i);
            String[] strings = arguments.get(i);

            List<Object> o = new ArrayList<>();
            o.add(a);
            for (String s : strings) {
                o.add(s);
            }

            Map<String, Object> m = new HashedMap<>();
            m.put(DIM_CLASSIFICATION_ARGS, o);
            m.put(DIM_DATA_WRITER, a.getDataWriterClass().getName());
            m.put(DIM_FEATURE_USE_SPARSE, a.useSparseFeatures() + "");

            maps.add(m);
        }
        return maps;
    }

    public void setReaders(Map<String, Object> dimReaders)
    {
        this.trainTestReaders = dimReaders;
        sanityCheckReaders();
    }

    public void setFeatureMode(String featureMode)
    {
        this.featureMode = featureMode;
    }

    public void setLearningMode(String learningMode)
    {
        this.learningMode = learningMode;
    }

    public void addFeatureSet(TcFeatureSet featureSet)
    {
        this.featureSets.add(featureSet);
    }

    public void addReader(CollectionReaderDescription reader, boolean isTrain)
    {
        if (reader == null) {
            throw new NullPointerException(
                    "Provided CollectionReaderDescription is null, please provide an initialized CollectionReaderDescription");
        }

        if (trainTestReaders == null) {
            trainTestReaders = new HashMap<>();
        }
        if (isTrain) {
            trainTestReaders.put(DIM_READER_TRAIN, reader);
        }
        else {
            trainTestReaders.put(DIM_READER_TEST, reader);
        }

        sanityCheckReaders();
    }

    private void sanityCheckReaders()
    {
        if (trainTestReaders.size() > 2) {
            throw new IllegalStateException(
                    "More than two readers have been added. Train-test experiments require two data readers, one for train, one for test. Cross-validation experiments require only one.");
        }
        
    }

}
