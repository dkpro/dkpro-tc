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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.lab.Lab;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.ml.base.Experiment_ImplBase;
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
    extends AbstractBuilder
{
    protected List<TcDeepLearningAdapter> backends;
    protected List<Object> userCodePath;

    File outputFolder;
    
    protected String pythonPath;
    protected String embeddingPath;
    protected int maxLen;
    protected boolean vectorize = true;

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
        if(pythonPath==null) {
            throw new IllegalStateException("No path to python provided");
        }
        
        return Dimension.create(DIM_PYTHON_INSTALLATION, pythonPath);
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

    @Override
    public DeepExperimentBuilder dataReaderTrain(CollectionReaderDescription reader)
    {
        super.dataReaderTrain(reader);
        return this;
    }

    @Override
    public DeepExperimentBuilder dataReaderTest(CollectionReaderDescription reader)
        throws IllegalStateException
    {
        super.dataReaderTest(reader);
        return this;
    }

    @Override
    public DeepExperimentBuilder additionalDimensions(Dimension<?>... dim)
    {
        super.additionalDimensions(dim);
        return this;
    }

    @Override
    @SafeVarargs
    public final DeepExperimentBuilder additionalDimensions(Map<String, Object>... dimensions)
    {
        super.additionalDimensions(dimensions);
        return this;
    }

    @Override
    public DeepExperimentBuilder learningMode(LearningMode learningMode)
    {
        super.learningMode(learningMode);
        return this;
    }

    @Override
    public DeepExperimentBuilder featureMode(FeatureMode featureMode)
    {
        super.featureMode(featureMode);
        return this;
    }

    /**
     * Sets an externally pre-defined experimental setup
     * 
     * @param experiment
     *            An experimental setup
     * @return The builder object
     */
    public DeepExperimentBuilder experiment(Experiment_ImplBase experiment)
    {

        if (experiment == null) {
            throw new NullPointerException("The experiment is null");
        }

        this.experiment = experiment;
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

    @Override
    public DeepExperimentBuilder reports(ReportBase... reports) {
        super.reports(reports);
        return this;
    }

    @Override
    public DeepExperimentBuilder preprocessing(AnalysisEngineDescription preprocessing) {
        super.preprocessing(preprocessing);
        return this;
    }

    @Override
    public DeepExperimentBuilder numFolds(int numFolds) {
        super.numFolds(numFolds);
        return this;
    }

    @Override
    public DeepExperimentBuilder name(String experimentName)
    {
        super.name(experimentName);
        return this;
    }

    @Override
    public DeepExperimentBuilder bipartitionThreshold(double threshold)
    {
        super.bipartitionThreshold(threshold);
        return this;
    }

    
    public Experiment_ImplBase build() throws Exception
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
        Experiment_ImplBase build = build();
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

    /**
     * Sets the path to the Python installation for which the deep learning software is installed
     * 
     * @param path
     *        absolute path in the file system
     * @return
     */
    public DeepExperimentBuilder pythonPath(String path)
    {
        this.pythonPath = path;
        return this;
    }

    /**
     * Path to a pre-trained word embedding that shall be used for initialization.
     * @param path
     *         absolute path in the file system
     * @return
     */
    public DeepExperimentBuilder embeddingPath(String path)
    {
        this.embeddingPath = path;
        return this;
    }

    /**
     * Set the maximum length of a document that is used for vectorization. Longer text will be cut-off to this length and shorter ones will be padded to fit this size.
     * Default is to use no padding, which will lead to different size data. 
     * @param len
     *          maximum length
     * @return
     */
    public DeepExperimentBuilder maximumLength(int len)
    {
        this.maxLen = len;
        return this;
    }

    /**
     * If integer vectorization shall be performed. Default is to perform integer vectorization
     * 
     * @param vectorize
     *          boolean if vectorization is requested
     * @return
     *      the builder object
     */
    public DeepExperimentBuilder vectorizeToInteger(boolean vectorize)
    {
        this.vectorize = vectorize;
        return this;
    }

}
