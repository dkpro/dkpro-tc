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
package org.dkpro.tc.ml.experiment.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.lab.Lab;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.ml.base.DeepLearningExperiment_ImplBase;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentCrossValidation;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentLearningCurve;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentLearningCurveTrainTest;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentTrainTest;
import org.dkpro.tc.ml.report.CrossValidationReport;
import org.dkpro.tc.ml.report.LearningCurveReport;
import org.dkpro.tc.ml.report.TrainTestReport;

/**
 * Builder class that offers a simplified wiring of DKPro TC experiments.
 */
public class DeepExperimentBuilder
    implements Constants, DeepLearningConstants
{
    protected List<TcDeepLearningAdapter> backends;
    protected List<Object> userCodePath;
    protected List<ReportBase> reports;
    protected String learningMode;
    protected String featureMode;
    protected Map<String, Object> readerMap;
    protected List<Dimension<?>> additionalDimensions;
    protected DeepLearningExperiment_ImplBase experiment;
    protected ParameterSpace parameterSpace;
    protected String experimentName;
    protected ExperimentType type;
    protected AnalysisEngineDescription preprocessing;
    protected List<Map<String, Object>> additionalMapDimensions;

    int numFolds = -1;
    double bipartitionThreshold = -1;
    File outputFolder;
    protected int learningCurveLimit = -1;
    protected String pythonPath;
    protected String embeddingPath;
    protected int maxLen;
    protected boolean vectorize;

    /**
     * Creates an experiment builder object.
     */
    public DeepExperimentBuilder()
    {
        // groovy
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
    public DeepExperimentBuilder machineLearningBackend(MLBackend... backends)
    {
        this.backends = new ArrayList<>();
        this.userCodePath = new ArrayList<>();

        for (MLBackend b : backends) {
            this.backends.add(b.getAdapterDeep());
            this.userCodePath.add(b.getParametrization().get(0));
        }

        return this;
    }

    /**
     * Wires the parameter space. The created parameter space can be passed to experimental setup.
     * 
     * @return a parameter space
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ParameterSpace getParameterSpace()
    {
        List<Dimension<?>> dimensions = new ArrayList<>();

        dimensions.add(getAsDimensionMachineLearningAdapter());
        dimensions.add(getAsDimensionFeatureMode());
        dimensions.add(getAsDimensionLearningMode());
        dimensions.add(getAsDimensionReaders());
        dimensions.add(getAsDimensionPythonPath());
        dimensions.add(getAsDimensionVectorizeToInteger());
        
        if(maxLen != -1) {
            dimensions.add(getAsDimensionMaximumLength());
        }
        
        if(embeddingPath!=null) {
            dimensions.add(getAsDimensionEmbedding());
        }

        if (additionalDimensions != null && additionalDimensions.size() > 0) {
            dimensions.addAll(additionalDimensions);
        }

        if (additionalMapDimensions != null && additionalMapDimensions.size() > 0) {
            int dim = 1;
            for (Map m : additionalMapDimensions) {
                Dimension bundle = Dimension.createBundle("additionalDim_" + dim++, m);
                dimensions.add(bundle);
            }
        }

        if (this.bipartitionThreshold != -1) {
            dimensions.add(getAsDimensionsBipartionThreshold());
        }

        parameterSpace = new ParameterSpace(dimensions.toArray(new Dimension<?>[0]));

        return parameterSpace;
    }

    private Dimension<?> getAsDimensionVectorizeToInteger()
    {
        return Dimension.create(DIM_VECTORIZE_TO_INTEGER, vectorize);
    }

    private Dimension<?> getAsDimensionMaximumLength()
    {
        return Dimension.create(DIM_MAXIMUM_LENGTH, maxLen);
    }

    private Dimension<?> getAsDimensionEmbedding()
    {
        return Dimension.create(DIM_PRETRAINED_EMBEDDINGS, embeddingPath);
    }

    protected Dimension<?> getAsDimensionPythonPath()
    {
        return Dimension.create(DIM_PYTHON_INSTALLATION, pythonPath);
    }

    protected Dimension<?> getAsDimensionsBipartionThreshold()
    {
        return Dimension.create(DIM_BIPARTITION_THRESHOLD, bipartitionThreshold);
    }

    protected Dimension<?> getAsDimensionMachineLearningAdapter()
    {
        List<Map<String, Object>> adapterMaps = getAdapterInfo();

        @SuppressWarnings("unchecked")
        Map<String, Object>[] array = adapterMaps.toArray(new Map[0]);
        Dimension<Map<String, Object>> mlaDim = Dimension.createBundle(DIM_MLA_CONFIGURATIONS,
                array);
        return mlaDim;
    }

    protected Dimension<?> getAsDimensionReaders()
    {
        if (!readerMap.keySet().contains(DIM_READER_TRAIN)) {
            throw new IllegalStateException("You must provide at least a training data reader");
        }

        return Dimension.createBundle(DIM_READERS, readerMap);
    }

    protected Dimension<?> getAsDimensionLearningMode()
    {
        if (learningMode == null) {
            throw new NullPointerException(
                    "No learning mode set, please provide this information via the respective setter method");
        }

        return Dimension.create(DIM_LEARNING_MODE, learningMode);
    }

    protected Dimension<?> getAsDimensionFeatureMode()
    {
        if (featureMode == null) {
            throw new NullPointerException(
                    "No feature mode set, please provide this information via the respective setter method");
        }

        return Dimension.create(DIM_FEATURE_MODE, featureMode);
    }

    protected List<Map<String, Object>> getAdapterInfo()
    {
        if (backends.size() == 0) {
            throw new IllegalStateException(
                    "No machine learning adapter set - Provide at least one machine learning configuration");
        }

        List<Map<String, Object>> maps = new ArrayList<>();

        for (int i = 0; i < backends.size(); i++) {
            TcDeepLearningAdapter a = backends.get(i);
            Object userCode = userCodePath.get(i);

            List<Object> o = new ArrayList<>();
            o.add(a);
            o.add(userCode);

            Map<String, Object> m = new HashedMap<>();
            m.put(DIM_CLASSIFICATION_ARGS, o);

            maps.add(m);
        }
        return maps;
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
    public DeepExperimentBuilder dataReaderTrain(CollectionReaderDescription reader)
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
    public DeepExperimentBuilder dataReaderTest(CollectionReaderDescription reader)
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
    public DeepExperimentBuilder additionalDimensions(Dimension<?>... dim)
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

    @SafeVarargs
    public final DeepExperimentBuilder additionalDimensions(Map<String, Object>... dimensions)
    {

        if (dimensions == null) {
            throw new NullPointerException("The added dimension is null");
        }
        for (Map<String, Object> d : dimensions) {
            if (d == null) {
                throw new NullPointerException("The added map is null");
            }
        }

        additionalMapDimensions = new ArrayList<>(Arrays.asList(dimensions));
        return this;
    }

    /**
     * Sets the learning mode of the experiment
     * 
     * @param learningMode
     *            The learning mode
     * @return The builder object
     */
    public DeepExperimentBuilder learningMode(LearningMode learningMode)
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
     *            The feature mode
     * @return The builder object
     */
    public DeepExperimentBuilder featureMode(FeatureMode featureMode)
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
     *            An experimental setup
     * @return The builder object
     */
    public DeepExperimentBuilder experiment(DeepLearningExperiment_ImplBase experiment)
    {

        if (experiment == null) {
            throw new NullPointerException("The experiment is null");
        }

        this.experiment = experiment;
        return this;
    }

    /**
     * This switch is relevant for {@link ExperimentType#LEARNING_CURVE} and
     * {@link ExperimentType#LEARNING_CURVE_FIXED_TEST_SET}. Sets a maximum number of train set
     * permutations on each learning curve stage. For instance, on the first stage of a ten fold run
     * you will get the following folds on the first two stages:
     * 
     * <pre>
     * Stage 1
     * [0]
     * [1]
     * [2]
     * [3]
     * [4]
     * ...
     * [9]
     * 
     * Stage 2
     * [0, 1]
     * [1, 2]
     * [2, 3]
     * ...
     * [9, 0]
     * 
     * Stage 3
     * [0, 1, 2]
     * [1, 2, 3]
     * [2, 3, 4]
     * ...
     * Stage 4
     * ...
     * Stage 10
     * ...
     * </pre>
     * 
     * Even for a small number of folds, the number of created train set permutations is rather high
     * leading to a long runtime. It is usually not necessary to use all permutations of the train
     * set to receive a smooth learning curve. This parameter limits the number of runs in each
     * stage to the number specified as parameter. This will considerably speed up the learning
     * curve.
     * 
     * @param learningCurveLimit
     *            The limit which must be a non-zero positive integer
     * @return The builder object
     */
    public DeepExperimentBuilder learningCurveLimit(int learningCurveLimit)
    {
        if (learningCurveLimit <= 0) {
            throw new IllegalArgumentException(
                    "Learning curve limit must be a positive integer greater zero but was ["
                            + learningCurveLimit + "]");
        }
        this.learningCurveLimit = learningCurveLimit;
        return this;
    }

    /**
     * Creates an experimental setup with a pre-defined type
     * 
     * @param type
     *            The type of experiment
     * @param experimentName
     *            The name of the experiment
     * @return The builder object
     */
    public DeepExperimentBuilder experiment(ExperimentType type, String experimentName)
    {
        this.type = type;
        this.experimentName = experimentName;
        return this;
    }

    protected DeepExperimentBuilder configureExperiment(ExperimentType type, String experimentName)
        throws Exception
    {
        switch (type) {
        case TRAIN_TEST:
            experiment = new DeepLearningExperimentTrainTest(experimentName);
            experiment.addReport(new TrainTestReport());
            readersCheckExperimentTrainTestCheck();
            break;
        case CROSS_VALIDATION:
            int folds = getCvFolds();
            experiment = new DeepLearningExperimentCrossValidation(experimentName, folds);
            experiment.addReport(new CrossValidationReport());
            readersCheckExperimentCrossValidation();
            break;
        case LEARNING_CURVE:
            folds = getCvFolds();
            experiment = new DeepLearningExperimentLearningCurve(experimentName, folds,
                    learningCurveLimit);
            experiment.addReport(new LearningCurveReport());
            readersCheckExperimentCrossValidation();
            break;
        case LEARNING_CURVE_FIXED_TEST_SET:
            folds = getCvFolds();
            experiment = new DeepLearningExperimentLearningCurveTrainTest(experimentName, folds,
                    learningCurveLimit);
            experiment.addReport(new LearningCurveReport());
            readersCheckExperimentTrainTestCheck();
            break;
         case SAVE_MODEL:
             throw new UnsupportedOperationException("This is currently not implemted");
//         sanityCheckSaveModelExperiment();
//         experiment = new ExperimentSaveModel(experimentName, outputFolder);
//         break;

        }
        return this;
    }

    private void readersCheckExperimentCrossValidation()
    {
        if (!(experiment instanceof DeepLearningExperimentCrossValidation)
                && !(experiment instanceof DeepLearningExperimentLearningCurve)) {
            return;
        }

        if (readerMap.size() < 1) {
            throw new IllegalStateException("No reader set for reading training data");
        }
        else if (readerMap.size() > 1) {
            LogFactory.getLog(getClass())
                    .warn("Experiment type [" + experiment.getClass().getSimpleName()
                            + "] requires only one reader [" + readerMap.size()
                            + "] were found - additional readers will be ignored");
        }
    }

    private void readersCheckExperimentTrainTestCheck()
    {
        if (!(experiment instanceof DeepLearningExperimentTrainTest)
//                && !(experiment instanceof DeepLearningExperimentLearningCurveTrainTest)
                ) {
            return;
        }

        if (readerMap.size() < 2) {
            throw new IllegalStateException(
                    "Experiment type [" + experiment.getClass().getSimpleName()
                            + "] requires two readers (train/test) but [" + readerMap.size()
                            + "] readers were provided");
        }
    }

    protected int getCvFolds()
    {
        // -1 defines leave one out and is, thus, valid as parameter. Any lower number
        // is not
        if (numFolds < -1) {
            throw new IllegalArgumentException("Specified number of folds [" + numFolds
                    + "] is invlaid, set either [-1] for LEAVE-ONE-OUT or a positive value");
        }

        LogFactory.getLog(getClass()).debug("Number of folds set to [" + numFolds + "]");

        return numFolds;
    }

    protected void sanityCheckSaveModelExperiment()
    {
        if (outputFolder == null) {
            throw new IllegalStateException(
                    "The output folder to which the model will be stored is not set.");
        }

        if (backends == null) {
            throw new IllegalStateException(
                    "No machine learning backend select - set exactly one machine learning backend for save model experiments");
        }

        if (backends.size() > 1) {
            throw new IllegalStateException(
                    "Only one machine learning backend can be specified for model saving");
        }
    }

    /**
     * Sets user-specific reports for the experiments. Calling this method multiple times overwrites
     * all changes from the previous calls.
     * 
     * @param reports
     *            One or more reports
     * @return The builder object
     */
    public DeepExperimentBuilder reports(ReportBase... reports)
    {
        this.reports = new ArrayList<>();
        for (ReportBase r : reports) {
            LogFactory.getLog(getClass()).debug(
                    "Add report [" + r.getClass().getSimpleName() + "] to experimental setup");
            this.reports.add(r);
        }

        return this;
    }

    /**
     * Sets a {@link AnalysisEngineDescription} which contains all necessary pre-processing steps.
     * Multiple {@link AnalysisEngineDescription} can be combined into a single one by nesting
     * multiple descriptions, e.g.
     * 
     * <pre>
     *     AnalysisEngineFactory.createEngineDescription(
     *              AnalysisEngineFactory.createEngineDescription(abc1.class),
     *              AnalysisEngineFactory.createEngineDescription(abc2.class),
     *              AnalysisEngineFactory.createEngineDescription(...),
     *     );
     * </pre>
     * 
     * @param preprocessing
     *            the preprocessing component
     * @return The builder object
     */
    public DeepExperimentBuilder preprocessing(AnalysisEngineDescription preprocessing)
    {
        this.preprocessing = preprocessing;
        return this;
    }

    /**
     * Sets the number of folds for {@link ExperimentType#CROSS_VALIDATION}. Defaults to ten if not
     * set by the user. Is ignored for other experiment types.
     * 
     * @param numFolds
     *            The number of folds
     * @return The builder object
     */
    public DeepExperimentBuilder numFolds(int numFolds)
    {
        this.numFolds = numFolds;
        return this;
    }

    /**
     * Sets the experiment Name
     * 
     * @param experimentName
     *            The name
     * @return The builder object
     */
    public DeepExperimentBuilder name(String experimentName)
    {
        this.experimentName = experimentName;
        return this;
    }

    public DeepExperimentBuilder bipartitionThreshold(double threshold)
    {
        this.bipartitionThreshold = threshold;
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
    public DeepLearningExperiment_ImplBase build() throws Exception
    {

        setExperiment();
        setParameterSpace();
        setPreprocessing();
        setReports();

        return experiment;
    }

    protected void setReports()
    {
        if (reports != null) {
            for (ReportBase r : reports) {
                experiment.addReport(r);
            }
        }
    }

    protected void setPreprocessing()
    {
        if (preprocessing != null) {
            experiment.setPreprocessing(preprocessing);
        }
    }

    protected void setParameterSpace()
    {
        if (parameterSpace == null) {
            parameterSpace = getParameterSpace();
        }
        experiment.setParameterSpace(parameterSpace);
    }

    protected void setExperiment() throws Exception
    {
        if (experiment == null && type != null) {
            configureExperiment(type, experimentName);
            return;
        }

        if (experimentName != null) {
            experiment.setExperimentName(experimentName);
        }

        throw new IllegalStateException("Please set an experiment");
    }

    /**
     * Executes the experiment
     * 
     * @throws Exception
     *             In case of an invalid configuration or missing mandatory values
     */
    public void run() throws Exception
    {
        DeepLearningExperiment_ImplBase build = build();
        Lab.getInstance().run(build);
    }

    /**
     * Sets the output folder to which the model is saved when the experiment type is set to save
     * model
     * 
     * @param filePath
     *            path to the file
     * @return the builder itself
     */
    public DeepExperimentBuilder outputFolder(String filePath)
    {
        if (filePath == null) {
            throw new NullPointerException("The provided output folder path is null");
        }

        this.outputFolder = new File(filePath);
        if (!this.outputFolder.exists()) {
            boolean mkdirs = outputFolder.mkdirs();
            if (!mkdirs) {
                throw new IllegalStateException(
                        "Could not create output folder [" + filePath + "]");
            }
        }
        return this;
    }

    public DeepExperimentBuilder pythonPath(String pythonPath)
    {
        this.pythonPath = pythonPath;
        return this;
    }

    public DeepExperimentBuilder embeddingPath(String embeddingPath)
    {
        this.embeddingPath = embeddingPath;
        return this;
    }

    public DeepExperimentBuilder maximumLength(int maxLen)
    {
        this.maxLen = maxLen;
        return this;
    }

    public DeepExperimentBuilder vectorizeToInteger(boolean vectorize)
    {
        this.vectorize = vectorize;
        return this;
    }

}
