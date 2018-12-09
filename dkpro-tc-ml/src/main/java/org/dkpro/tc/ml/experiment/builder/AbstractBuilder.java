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
import org.dkpro.tc.ml.base.Experiment_ImplBase;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentCrossValidation;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentLearningCurve;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentLearningCurveTrainTest;
import org.dkpro.tc.ml.experiment.deep.DeepLearningExperimentTrainTest;

public abstract class AbstractBuilder implements Constants, DeepLearningConstants
{
    protected List<Map<String, Object>> additionalMapDimensions;
    protected List<Dimension<?>> additionalDimensions;
    protected String learningMode;
    protected String featureMode;
    protected Map<String, Object> readerMap;
    protected Experiment_ImplBase experiment;
    protected String experimentName;
    protected ExperimentType type;
    protected AnalysisEngineDescription preprocessing;
    protected List<ReportBase> reports;
    protected ParameterSpace parameterSpace;
    protected File outputFolder;
    double bipartitionThreshold = -1;
    protected int learningCurveLimit = -1;
    int numFolds = -1;
    
    public abstract AbstractBuilder machineLearningBackend(MLBackend... backends);
    protected abstract AbstractBuilder configureExperiment(ExperimentType type, String experimentName) throws Exception;
    
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
    public abstract Experiment_ImplBase build() throws Exception;
    
    public abstract ParameterSpace getParameterSpace();
    
    protected void setParameterSpace()
    {
        if (parameterSpace == null) {
            parameterSpace = getParameterSpace();
        }
        experiment.setParameterSpace(parameterSpace);
    }

    /**
     * Creates an experimental setup with a pre-defined type
     * 
     * @param type           The type of experiment
     * @param experimentName The name of the experiment
     * @return The builder object
     */
    public AbstractBuilder experiment(ExperimentType type, String experimentName) {
        this.type = type;
        this.experimentName = experimentName;
        return this;
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
    public AbstractBuilder dataReaderTrain(CollectionReaderDescription reader)
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
    public AbstractBuilder dataReaderTest(CollectionReaderDescription reader)
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
     * Sets the experiment Name
     * 
     * @param experimentName
     *            The name
     * @return The builder object
     */
    public AbstractBuilder name(String experimentName)
    {
        this.experimentName = experimentName;
        return this;
    }

    public AbstractBuilder bipartitionThreshold(double threshold)
    {
        if (threshold <= 0 || threshold >= 1.0) {
            throw new IllegalArgumentException("Bipartition threshold should be > 0 and < 1");
        }
        
        this.bipartitionThreshold = threshold;
        return this;
    }
    
    /**
     * Sets the number of folds for {@link ExperimentType#CROSS_VALIDATION}. Defaults to 10 if not
     * set by the user. Is ignored for other experiment types.
     * The value -1 performs leave-one-out cross-validation. 
     * 
     * @param numFolds
     *            The number of folds
     * @return The builder object
     */
    public AbstractBuilder numFolds(int numFolds)
    {
        if (numFolds < -1 || numFolds == 0) {
            throw new IllegalArgumentException("The number of folds be either [-1]=Leave-one-out CV or an integer value > 0");
        }
        
        this.numFolds = numFolds;
        return this;
    }
    
    protected int getCvFolds()
    {
        LogFactory.getLog(getClass()).debug("Number of folds set to [" + numFolds + "]");
        return numFolds;
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
    public AbstractBuilder preprocessing(AnalysisEngineDescription preprocessing)
    {
        this.preprocessing = preprocessing;
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
    public AbstractBuilder learningCurveLimit(int learningCurveLimit)
    {
        if (learningCurveLimit <= 0) {
            throw new IllegalArgumentException(
                    "Learning curve limit must be a positive integer greater zero but was ["
                            + learningCurveLimit + "]");
        }
        this.learningCurveLimit = learningCurveLimit;
        return this;
    }
    
    protected Dimension<?> getAsDimensionReaders()
    {
        if (!readerMap.keySet().contains(DIM_READER_TRAIN)) {
            throw new IllegalStateException("You must provide at least a training data reader");
        }

        return Dimension.createBundle(DIM_READERS, readerMap);
    }

    
    /**
     * Sets the learning mode of the experiment
     * 
     * @param learningMode
     *            The learning mode
     * @return The builder object
     */
    public AbstractBuilder learningMode(LearningMode learningMode)
    {
        if (learningMode == null) {
            throw new NullPointerException("Learning mode is null");
        }

        this.learningMode = learningMode.toString();
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
    public AbstractBuilder additionalDimensions(Dimension<?>... dim)
    {

        for (Dimension<?> d : dim) {
            if (d == null) {
                throw new NullPointerException("The added dimension is null");
            }
        }

        additionalDimensions = new ArrayList<>(Arrays.asList(dim));
        return this;
    }
    
    public AbstractBuilder additionalDimensions(@SuppressWarnings("unchecked") Map<String, Object>... dimensions)
    {
        additionalMapDimensions = new ArrayList<>(Arrays.asList(dimensions));
        return this;
    }

    protected Dimension<?> getAsDimensionsBipartionThreshold()
    {
        return Dimension.create(DIM_BIPARTITION_THRESHOLD, bipartitionThreshold);
    }
    
    /**
     * Sets the feature mode of the experiment
     * 
     * @param featureMode
     *            The feature mode
     * @return The builder object
     */
    public AbstractBuilder featureMode(FeatureMode featureMode)
    {
        if (featureMode == null) {
            throw new NullPointerException("Feature mode is null");
        }

        this.featureMode = featureMode.toString();
        return this;
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
    
    protected void readersCheckExperimentCrossValidation()
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

    protected void readersCheckExperimentTrainTestCheck()
    {
        if (!(experiment instanceof DeepLearningExperimentTrainTest)
                && !(experiment instanceof DeepLearningExperimentLearningCurveTrainTest)
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
    

    /**
     * Executes the experiment
     * 
     * @throws Exception In case of an invalid configuration or missing mandatory
     *                   values
     */
    public void run() throws Exception {
        Experiment_ImplBase build = build();
        Lab.getInstance().run(build);
    }
    
    /**
     * Sets the output folder to which the model is saved when the experiment type
     * is set to save model
     * 
     * @param filePath path to the file
     * @return the builder itself
     */
    public AbstractBuilder outputFolder(String filePath) {
        if (filePath == null) {
            throw new NullPointerException("The provided output folder path is null");
        }

        this.outputFolder = new File(filePath);
        if (!this.outputFolder.exists()) {
            boolean mkdirs = outputFolder.mkdirs();
            if (!mkdirs) {
                throw new IllegalStateException("Could not create output folder [" + filePath + "]");
            }
        }
        return this;
    }
    
    /**
     * Sets the output folder to which the model is saved when the experiment type
     * is set to save model
     * 
     * @param folder
     *         path to the file
     * @return the builder itself
     */
    public AbstractBuilder outputFolder(File folder) {
        if (folder == null) {
            throw new NullPointerException("The provided output folder is null");
        }

        this.outputFolder = folder;
        if (!this.outputFolder.exists()) {
            boolean mkdirs = outputFolder.mkdirs();
            if (!mkdirs) {
                throw new IllegalStateException("Could not create output folder [" + folder + "]");
            }
        }
        return this;
    }
    
    /**
     * Sets user-specific reports for the experiments. Calling this method multiple
     * times overwrites all changes from the previous calls.
     * 
     * @param reports One or more reports
     * @return The builder object
     */
    public AbstractBuilder reports(ReportBase... reports) {
        this.reports = new ArrayList<>();
        for (ReportBase r : reports) {
            LogFactory.getLog(getClass())
                    .debug("Add report [" + r.getClass().getSimpleName() + "] to experimental setup");
            this.reports.add(r);
        }

        return this;
    }
    
    protected void setExperiment() throws Exception
    {
        if (type != null) {
            configureExperiment(type, experimentName);
            return;
        }
        else if (experiment == null) {
            throw new IllegalStateException("Please set an experiment");
        }
        if (experimentName != null) {
            experiment.setExperimentName(experimentName);
        }
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

    
}
