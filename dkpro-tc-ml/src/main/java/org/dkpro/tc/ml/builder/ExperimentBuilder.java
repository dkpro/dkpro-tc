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
import org.apache.uima.fit.factory.CollectionReaderFactory;
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
 * Builder class that offers a simplified wiring of DKPro TC experiments.
 */
public class ExperimentBuilder
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
    AnalysisEngineDescription preprocessing;
    List<String> featureFilter;

    /**
     * Creates an experiment builder object.
     * 
     */
    public ExperimentBuilder()
    {

    }

    /**
     * Sets one or multiple machine learning adapter configurations. Several configurations of the
     * same adapter have to be passed as separated {@link MLBackend} configurations. Calling this
     * method will remove all previously set {@link MLBackend} configurations.
     * 
     * @param backends
     *            one or more machine learning backends
     * @return the builder object
     */
    public ExperimentBuilder machineLearningBackend(MLBackend... backends)
    {
        this.adapter = new ArrayList<>();
        this.arguments = new ArrayList<>();

        for (MLBackend b : backends) {
            this.adapter.add(b.getAdapter());
            this.arguments.add(b.getParametrization());
        }

        return this;
    }

    /**
     * Wires the parameter space. The created parameter space can be passed to experimental setup.
     * 
     * @return a parameter space
     */
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
        if (featureSets == null) {
            throw new NullPointerException("Set either a feature set ["
                    + TcFeatureSet.class.getName() + "]or provide at least a single feature");
        }

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

    /**
     * Sets one or more pre-configured {@link TcFeatureSet}. Either this method or
     * {@link #features(TcFeature...)} has to be called to create a valid experimental setup.
     * Calling this method will remove all previously set feature sets.
     * 
     * @param featureSet
     *            one or more feature sets
     * @return the builder object
     */
    public ExperimentBuilder featureSets(TcFeatureSet... featureSet)
    {
        for (TcFeatureSet fs : featureSet) {
            sanityCheckFeatureSet(fs);
        }
        this.featureSets = new ArrayList<>(Arrays.asList(featureSet));
        return this;
    }

    /**
     * Sets one or more feature filters by their full name i.e. .class.getName()
     * 
     * @param filter
     *            one or more filter names
     * @return the builder object
     */
    public ExperimentBuilder featureFilter(String... filter)
    {

        if (filter == null) {
            throw new NullPointerException("The feature filters are null");
        }

        featureFilter = new ArrayList<>();

        for (String f : filter) {
            featureFilter.add(f);
        }

        return this;
    }

    /**
     * Sets several features to be used in an experiment. If this method is used a single
     * {@link TcFeatureSet} is created in the background. If multiple feature sets shall be used use
     * {@link #featureSets(TcFeatureSet...)} Calling this method will remove all previously set
     * feature configurations
     * 
     * @param features
     *            one or more features
     * @return the builder object
     */
    public ExperimentBuilder features(TcFeature... features)
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

    /**
     * Sets the data reader which reads the training data
     * 
     * @param reader
     *            the {@link CollectionReaderDescription} that can be created via
     *            {@link CollectionReaderFactory} from a reader class.
     * @return the builder object
     * 
     */
    public ExperimentBuilder dataReaderTrain(CollectionReaderDescription reader)
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

    /**
     * Sets the data reader which reads the test data
     * 
     * @param reader
     *            the {@link CollectionReaderDescription} that can be created via
     *            {@link CollectionReaderFactory} from a reader class.
     * @return the builder object
     */
    public ExperimentBuilder dataReaderTest(CollectionReaderDescription reader)
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

    /**
     * Allows the user to set additional dimensions. This is for advanced users that use dimensions
     * that are not part of the minimal configuration for an experiment. This method will remove all
     * previously set additional dimensions and replaces them with the newly provided one.
     * 
     * @param dim
     *            a list of dimensions
     * @return the builder object
     */
    public ExperimentBuilder additionalDimensions(Dimension<?>... dim)
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

    /**
     * Sets the learning mode of the experiment
     * 
     * @param learningMode
     *          The learning mode
     * @return
     *          The builder object
     */
    public ExperimentBuilder learningMode(LearningMode learningMode)
    {
        if (learningMode == null) {
            throw new NullPointerException("Learning mode is null");
        }

        this.learningMode = learningMode.toString();
        return this;
    }

    /**
     * Sets the feature mode of the experiment
     * 
     * @param featureMode
     *          The feature mode
     * @return
     *      The builder object
     */
    public ExperimentBuilder featureMode(FeatureMode featureMode)
    {
        if (featureMode == null) {
            throw new NullPointerException("Feature mode is null");
        }

        this.featureMode = featureMode.toString();
        return this;
    }

    /**
     * Sets an externally pre-defined experimental setup
     * 
     * @param experiment
     *          An experimental setup
     * @return
     *      The builder object
     */
    public ExperimentBuilder experiment(ShallowLearningExperiment_ImplBase experiment)
    {

        if (experiment == null) {
            throw new NullPointerException("The experiment is null");
        }

        this.experiment = experiment;
        return this;
    }

    /**
     * Creates an experimental setup with a pre-defined type
     * @param type
     *          The type of experiment
     * @param experimentName
     *          The name of the experiment
     * @return
     *          The builder object
     */
    public ExperimentBuilder experiment(ExperimentType type, String experimentName)
    {
        this.type = type;
        this.experimentName = experimentName;
        return this;
    }

    private ExperimentBuilder experiment(ExperimentType type, String experimentName,
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

    /**
     * Sets reports for the experiments
     * 
     * @param reports
     *          One or more reports
     * @return
     *          The builder object
     */
    public ExperimentBuilder reports(ReportBase... reports)
    {
        this.reports = new ArrayList<>();
        for (ReportBase r : reports) {
            this.reports.add(r);
        }

        return this;
    }

    /**
     * Sets a {@link AnalysisEngineDescription} which contains all necessary pre-processing steps.
     * Multiple {@link AnalysisEngineDescription} can be combined into a single one by nesting
     * multiple descriptions, e.g. 
     * <pre>
     *     AnalysisEngineFactory.createEngineDescription(
     *              AnalysisEngineFactory.createEngineDescription(abc1.class),
     *              AnalysisEngineFactory.createEngineDescription(abc2.class),
     *              AnalysisEngineFactory.createEngineDescription(...),
     *     );
     * </pre> 
     * 
     * @param preprocessing
     *          the preprocessing component
     * @return
     *          The builder object
     */
    public ExperimentBuilder preprocessing(AnalysisEngineDescription preprocessing)
    {
        this.preprocessing = preprocessing;
        return this;
    }

    /**
     * Sets the number of folds for {@link ExperimentType#CROSS_VALIDATION}. Defaults to ten if not
     * set by the user. Is ignored for other experiment types.
     * 
     * @param numFolds
     *          The number of folds
     * @return
     *      The builder object
     */
    public ExperimentBuilder numFolds(int numFolds)
    {
        this.numFolds = numFolds;
        return this;
    }

    /**
     * Sets the experiment Name
     * @param experimentName
     *          The name
     * @return
     *      The builder object
     */
    public ExperimentBuilder name(String experimentName)
    {
        this.experimentName = experimentName;
        return this;
    }

    /**
     * Wires the provided parameter to an experiment. The experiment object can be executed by
     * calling:
     * 
     * <pre>
     *      Lab.getInstance().run(...)
     * </pre>
     * 
     * The method {@link #run()} performs automatically above step as convenience service.
     * 
     * @return The experiment
     * @throws Exception
     *             In case of invalid configurations
     */
    public ShallowLearningExperiment_ImplBase build() throws Exception
    {

        setExperiment();
        setParameterSpace();
        setPreprocessing();
        setReports();

        return experiment;
    }

    private void setReports()
    {
        if (reports != null) {
            for (ReportBase r : reports) {
                experiment.addReport(r);
            }
        }
    }

    private void setPreprocessing()
    {
        if (preprocessing != null) {
            experiment.setPreprocessing(preprocessing);
        }
    }

    private void setParameterSpace()
    {
        if (parameterSpace == null) {
            parameterSpace = getParameterSpace();
        }
        experiment.setParameterSpace(parameterSpace);
    }

    private void setExperiment() throws Exception
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

        if (experimentName != null) {
            experiment.setExperimentName(experimentName);
        }
    }

    /**
     * Executes the experiment
     * 
     * @throws Exception
     *             In case of an invalid configuration or missing mandatory values
     */
    public void run() throws Exception
    {
        ShallowLearningExperiment_ImplBase build = build();
        Lab.getInstance().run(build);
    }

}
