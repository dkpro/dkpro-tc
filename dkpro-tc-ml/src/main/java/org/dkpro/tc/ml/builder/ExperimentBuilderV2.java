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
    List<TcShallowLearningAdapter> adapter;
    List<List<String>> arguments;
    List<ReportBase> reports;
    String learningMode;
    String featureMode;
    Map<String, Object> readerMap;
    List<TcFeatureSet> featureSets;
    List<Dimension<?>> additionalDimensions;
    ShallowLearningExperiment_ImplBase experiment;
    ParameterSpace parameterSpace;
    String experimentName;
    ExperimentType type;
    int numFolds = -1;
    private AnalysisEngineDescription preprocessing;
    private List<String> featureFilter;

    /**
     * Creates an experiment builder object.
     * 
     */
    public ExperimentBuilderV2()
    {

    }

    public ExperimentBuilderV2 machineLearningBackend(MLBackend... backends)
    {
        this.adapter = new ArrayList<>();
        this.arguments = new ArrayList<>();

        for (MLBackend b : backends) {
            this.adapter.add(b.getAdapter());
            this.arguments.add(b.getParametrization());
        }

        return this;
    }

    public ParameterSpace getParameterSpace()
    {
        List<Dimension<?>> dimensions = new ArrayList<>();

        dimensions.add(getAsDimensionMachineLearningAdapter());
        dimensions.add(getAsDimensionFeatureMode());
        dimensions.add(getAsDimensionLearningMode());
        dimensions.add(getAsDimensionFeatureSets());
        dimensions.add(getAsDimensionReaders());
        
        
        if (featureFilter != null && featureFilter.size() > 0) {
            dimensions.add(getFeatureFilters());
        }
        if (additionalDimensions != null && additionalDimensions.size() > 0) {
            dimensions.addAll(additionalDimensions);
        }

        parameterSpace = new ParameterSpace(dimensions.toArray(new Dimension<?>[0]));

        return parameterSpace;
    }

    @SuppressWarnings("unchecked")
    private Dimension<?> getFeatureFilters()
    {
        return Dimension.create(DIM_FEATURE_FILTERS, featureFilter);
    }

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
        if (!readerMap.keySet().contains(DIM_READER_TRAIN)) {
            throw new IllegalStateException("You must provide at least a training data reader");
        }

        return Dimension.createBundle(DIM_READERS, readerMap);
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

    public ExperimentBuilderV2 featureSets(TcFeatureSet... featureSet)
    {
        for (TcFeatureSet fs : featureSet) {
            sanityCheckFeatureSet(fs);
        }
        this.featureSets = new ArrayList<>(Arrays.asList(featureSet));
        return this;
    }
    
    public ExperimentBuilderV2 featureFilter(String...filter) {

        if(filter == null) {
            throw new NullPointerException("The feature filters are null");
        }
        
        featureFilter = new ArrayList<>();
        
        for(String f : filter) {
            featureFilter.add(f);
        }
        
        return this;
    }

    public ExperimentBuilderV2 features(TcFeature... features)
    {
        if (features == null) {
            throw new NullPointerException("The features are null");
        }

        this.featureSets = new ArrayList<>();

        TcFeatureSet set = new TcFeatureSet();
        for (TcFeature f : features) {
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

    public ExperimentBuilderV2 dataReaderTrain(CollectionReaderDescription reader)
        throws IllegalStateException
    {
        if (reader == null) {
            throw new NullPointerException(
                    "Provided CollectionReaderDescription is null, please provide an initialized CollectionReaderDescription");
        }

        if (readerMap == null) {
            readerMap = new HashMap<>();
        }
        readerMap.put(DIM_READER_TRAIN, reader);

        return this;
    }

    public ExperimentBuilderV2 dataReaderTest(CollectionReaderDescription reader)
        throws IllegalStateException
    {
        if (reader == null) {
            throw new NullPointerException(
                    "Provided CollectionReaderDescription is null, please provide an initialized CollectionReaderDescription");
        }

        if (readerMap == null) {
            readerMap = new HashMap<>();
        }
        readerMap.put(DIM_READER_TEST, reader);

        return this;
    }

    public ExperimentBuilderV2 additionalDimensions(Dimension<?>... dim)
    {

        if (dim == null) {
            throw new NullPointerException("The added dimension is null");
        }
        for (Dimension<?> d : dim) {
            if (d == null) {
                throw new NullPointerException("The added dimension is null");
            }
        }

        additionalDimensions = new ArrayList<>(Arrays.asList(dim));
        return this;
    }

    public ExperimentBuilderV2 learningMode(LearningMode learningMode)
    {
        if (learningMode == null) {
            throw new NullPointerException("Learning mode is null");
        }

        this.learningMode = learningMode.toString();
        return this;
    }

    public ExperimentBuilderV2 featureMode(FeatureMode featureMode)
    {
        if (featureMode == null) {
            throw new NullPointerException("Feature mode is null");
        }

        this.featureMode = featureMode.toString();
        return this;
    }

    public ExperimentBuilderV2 experiment(ShallowLearningExperiment_ImplBase experiment)
    {

        if (experiment == null) {
            throw new NullPointerException("The experiment is null");
        }

        this.experiment = experiment;
        return this;
    }

    public ExperimentBuilderV2 experiment(ExperimentType type, String experimentName)
    {
        this.type = type;
        this.experimentName = experimentName;
        return this;
    }

    private ExperimentBuilderV2 experiment(ExperimentType type, String experimentName,
            int... numFolds)
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
            }
            else {
                experiment = new ExperimentCrossValidation(experimentName, numFolds[0]);
            }
            experiment.addReport(new BatchCrossValidationReport());
            break;
        }
        return this;
    }

    public ExperimentBuilderV2 experimentReports(ReportBase... reports)
    {
        this.reports = new ArrayList<>();
        for (ReportBase r : reports) {
            this.reports.add(r);
        }

        return this;
    }

    public ExperimentBuilderV2 experimentPreprocessing(AnalysisEngineDescription preprocessing)
    {
        this.preprocessing = preprocessing;
        return this;
    }

    public ExperimentBuilderV2 numFolds(int numFolds)
    {
        this.numFolds = numFolds;
        return this;
    }

    public ExperimentBuilderV2 experimentName(String experimentName)
    {
        if (experiment == null) {
            throw new NullPointerException("The experiment is not initialized");
        }
        experiment.setExperimentName(experimentName);
        return this;
    }

    public ShallowLearningExperiment_ImplBase build() throws Exception
    {
        if (experiment == null && type != null) {
            int numFolds = this.numFolds;
            if (numFolds == -1) {
                numFolds = 10;
            }

            experiment(type, experimentName, numFolds);
        }
        
        if (experiment instanceof ExperimentTrainTest && readerMap.size() != 2) {
            throw new IllegalStateException("Train test requires two readers");
        }
        
        if (parameterSpace == null) {
            parameterSpace = getParameterSpace();
        }
        experiment.setParameterSpace(parameterSpace);

        if (preprocessing != null) {
            experiment.setPreprocessing(preprocessing);
        }

        for (ReportBase r : reports) {
            experiment.addReport(r);
        }

        return experiment;
    }
    
    public void run() throws Exception {
        ShallowLearningExperiment_ImplBase build = build();
        Lab.getInstance().run(build);
    }

}
