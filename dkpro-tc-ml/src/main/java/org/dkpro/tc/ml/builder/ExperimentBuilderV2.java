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
package org.dkpro.tc.ml.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.lab.Lab;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.base.ShallowLearningExperiment_ImplBase;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;

/**
 * Convenience class that builds a parameter space object that can be passed to a DKPro Lab
 * experiment.
 */
public class ExperimentBuilderV2
    implements Constants
{
    List<TcShallowLearningAdapter> adapter = new ArrayList<>();
    List<List<String>> arguments = new ArrayList<>();
    String learningMode;
    String featureMode;
    Map<String, Object> readers = null;
    List<TcFeatureSet> featureSets = new ArrayList<>();
    List<Dimension<?>> additionalDimensions = new ArrayList<>();
    ShallowLearningExperiment_ImplBase experiment;
    ParameterSpace parameterSpace;

    /**
     * Creates an experiment builder object.
     * 
     */
    public ExperimentBuilderV2()
    {

    }

    public ExperimentBuilderV2 setMachineLearningBackend(MLBackend ...backends)
    {
        this.adapter = new ArrayList<>();
        this.arguments = new ArrayList<>();
        
        for(MLBackend b : backends) {
            this.adapter.add(b.getAdapter());
            this.arguments.add(b.getParametrization());    
        }
        
        return this;
    }

    public ExperimentBuilderV2 buildParameterSpace()
    {
        List<Dimension<?>> dimensions = new ArrayList<>();

        dimensions.add(getAsDimensionMachineLearningAdapter());
        dimensions.add(getAsDimensionFeatureMode());
        dimensions.add(getAsDimensionLearningMode());
        dimensions.add(getAsDimensionFeatureSets());
        dimensions.add(getAsDimensionReaders());
        dimensions.addAll(additionalDimensions);

        parameterSpace = new ParameterSpace();
        parameterSpace.setDimensions(dimensions.toArray(new Dimension<?>[0]));

        return this;
    }
    
//    public void runExperiment() throws Exception
//    {
//
//        if (experiment == null) {
//            throw new NullPointerException("The experiment has not been set");
//        }
//
//        ParameterSpace pSpace = buildParameterSpace();
//        experiment.setParameterSpace(pSpace);
//        Lab.getInstance().run(experiment);
//    }

    private Dimension<?> getAsDimensionMachineLearningAdapter()
    {
        List<Map<String, Object>> adapterMaps = getAdapterInfo();

        @SuppressWarnings("unchecked")
        Map<String, Object>[] array = adapterMaps.toArray(new Map[0]);
        Dimension<Map<String, Object>> mlaDim = Dimension.createBundle(DIM_MLA_CONFIGURATIONS,
                array);
        return mlaDim;
    }

    private Dimension<?> getAsDimensionReaders()
    {
        if (!readers.keySet().contains(DIM_READER_TRAIN)) {
            throw new IllegalStateException("You must provide at least a training data reader");
        }

        return Dimension.createBundle(DIM_READERS, readers);
    }

    private Dimension<?> getAsDimensionFeatureSets()
    {
        if (featureSets.isEmpty()) {
            throw new IllegalStateException(
                    "No feature sets provided, please provide at least one feature set i.e. ["
                            + TcFeatureSet.class.getName() + "]");
        }

        return Dimension.create(DIM_FEATURE_SET, featureSets.toArray(new TcFeatureSet[0]));
    }

    private Dimension<?> getAsDimensionLearningMode()
    {
        if (learningMode == null) {
            throw new NullPointerException(
                    "No learning mode set, please provide this information via the respective setter method");
        }

        return Dimension.create(DIM_LEARNING_MODE, learningMode);
    }

    private Dimension<?> getAsDimensionFeatureMode()
    {
        if (featureMode == null) {
            throw new NullPointerException(
                    "No feature mode set, please provide this information via the respective setter method");
        }

        return Dimension.create(DIM_FEATURE_MODE, featureMode);
    }

    private List<Map<String, Object>> getAdapterInfo()
    {
        if (adapter.size() == 0) {
            throw new IllegalStateException(
                    "No machine learning adapter set - Provide at least one machine learning configuration");
        }

        List<Map<String, Object>> maps = new ArrayList<>();

        for (int i = 0; i < adapter.size(); i++) {
            TcShallowLearningAdapter a = adapter.get(i);
            List<String> list = arguments.get(i);

            List<Object> o = new ArrayList<>();
            o.add(a);
            o.addAll(list);

            Map<String, Object> m = new HashedMap<>();
            m.put(DIM_CLASSIFICATION_ARGS, o);
            m.put(DIM_DATA_WRITER, a.getDataWriterClass());
            m.put(DIM_FEATURE_USE_SPARSE, a.useSparseFeatures() + "");

            maps.add(m);
        }
        return maps;
    }

    public void setReaders(Map<String, Object> dimReaders) throws NullPointerException
    {
        nullCheckReaderMap(dimReaders);
        this.readers = dimReaders;
        sanityCheckReaders();
    }

    private void nullCheckReaderMap(Map<String, Object> readers)
    {
        if (readers == null) {
            throw new NullPointerException("The provided readers are null");
        }
    }

    public ExperimentBuilderV2 setFeatureSets(TcFeatureSet ...featureSet)
    {
        for(TcFeatureSet fs : featureSet) {
            sanityCheckFeatureSet(fs);
        }
        this.featureSets = new ArrayList<>(Arrays.asList(featureSet));
        return this;
    }
    
    public ExperimentBuilderV2 setFeatures(TcFeature...features)
    {
        if(features == null) {
            throw new NullPointerException("The features are null");
        }
        
        this.featureSets = new ArrayList<>();
        
        TcFeatureSet set = new TcFeatureSet();
        for(TcFeature f : features) {
            set.add(f);
        }
        
        this.featureSets.add(set);
        return this;
    }

    private void sanityCheckFeatureSet(TcFeatureSet featureSet)
    {
        if (featureSet == null) {
            throw new NullPointerException("The provided feature set is null");
        }
        if (featureSet.isEmpty()) {
            throw new IllegalStateException("The provided feature set contains no features");
        }
    }

    public ExperimentBuilderV2 setReader(CollectionReaderDescription reader, boolean isTrain)
        throws IllegalStateException
    {
        if (reader == null) {
            throw new NullPointerException(
                    "Provided CollectionReaderDescription is null, please provide an initialized CollectionReaderDescription");
        }

        if (readers == null) {
            readers = new HashMap<>();
        }
        if (isTrain) {
            readers.put(DIM_READER_TRAIN, reader);
        }
        else {
            readers.put(DIM_READER_TEST, reader);
        }

        sanityCheckReaders();
        
        return this;
    }

    public ExperimentBuilderV2 setAdditionalDimensions(Dimension<?>...dim)
    {

        if (dim == null) {
            throw new NullPointerException("The added dimension is null");
        }
        for(Dimension<?> d : dim) {
            if(d == null) {
                throw new NullPointerException("The added dimension is null");
            }
        }

        additionalDimensions = new ArrayList<>(Arrays.asList(dim));
        return this;
    }

    private void sanityCheckReaders() throws IllegalStateException
    {
        if (readers.size() > 2) {
            throw new IllegalStateException(
                    "More than two readers have been added. Train-test experiments require two data readers, one for train, one for test. Cross-validation experiments require only one.");
        }

    }

    public ExperimentBuilderV2 setLearningMode(LearningMode learningMode)
    {
        if (learningMode == null) {
            throw new NullPointerException("Learning mode is null");
        }

        this.learningMode = learningMode.toString();
        return this;
    }
    
    public ExperimentBuilderV2 setFeatureMode(FeatureMode featureMode)
    {
        if (featureMode == null) {
            throw new NullPointerException("Feature mode is null");
        }

        this.featureMode = featureMode.toString();
        return this;
    }

    public ExperimentBuilderV2 setExperiment(ShallowLearningExperiment_ImplBase experiment)
    {

        if (experiment == null) {
            throw new NullPointerException("The experiment is null");
        }

        this.experiment = experiment;
        return this;
    }
    
    public ExperimentBuilderV2 setExperiment(ExperimentType type, String experimentName, int... numFolds)
        throws Exception
    {
        switch (type) {
        case TRAIN_TEST:
            experiment = new ExperimentTrainTest(experimentName);
            experiment.addReport(new BatchTrainTestReport());
            break;
        case CROSS_VALIDATION:
            if (numFolds.length > 1) {
                throw new IllegalArgumentException(
                        "Please provide only one value - the number of folds. Specifying multiple values is not possible");
            }
            else if (numFolds.length == 0) {
                experiment = new ExperimentCrossValidation(experimentName, 10);
                experiment.addReport(new BatchCrossValidationReport());
            }
            else {
                experiment = new ExperimentCrossValidation(experimentName, numFolds[0]);
                experiment.addReport(new BatchCrossValidationReport());
            }
            break;
        }
        return this;
    }
    
    public ExperimentBuilderV2 setExperimentReports(ReportBase... reports)
    {
        if (experiment == null) {
            throw new NullPointerException("The experiment is not set");
        }
        for (ReportBase r : reports) {
            experiment.addReport(r);
        }
        
        return this;
    }
    
    public ExperimentBuilderV2 setExperimentPreprocessing(AnalysisEngineDescription preprocessing)
    {
        if (experiment == null) {
            throw new NullPointerException("The experiment is not initialized");
        }
        experiment.setPreprocessing(preprocessing);
        return this;
    }
    
    public ExperimentBuilderV2 setExperimentName(String experimentName)
    {
        if (experiment == null) {
            throw new NullPointerException("The experiment is not initialized");
        }
        experiment.setExperimentName(experimentName);
        return this;
    }

    public void run() throws Exception
    {
        if(parameterSpace == null) {
            buildParameterSpace();
        }
        
        experiment.setParameterSpace(parameterSpace);
        Lab.getInstance().run(experiment);
    }

}
