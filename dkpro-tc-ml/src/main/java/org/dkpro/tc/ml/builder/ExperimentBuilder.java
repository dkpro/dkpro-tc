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
public class ExperimentBuilder
    implements Constants
{
    List<TcShallowLearningAdapter> adapter = new ArrayList<>();
    List<String[]> arguments = new ArrayList<>();
    String learningMode;
    String featureMode;
    Map<String, Object> readers = null;
    List<TcFeatureSet> featureSets = new ArrayList<>();
    List<Dimension<?>> additionalDimensions = new ArrayList<>();
    ShallowLearningExperiment_ImplBase experiment;

    /**
     * Creates an experiment builder object.
     * 
     */
    public ExperimentBuilder()
    {

    }
    
    public ExperimentBuilder(ExperimentType type, CollectionReaderDescription trainReader,
            CollectionReaderDescription testReader, LearningMode lm, FeatureMode fm,
            TcShallowLearningAdapter adapter, TcFeatureSet featureSet) throws Exception
    {
        setExperiment(type, "TcExperiment");
        
        addReader(trainReader, false);
        addReader(testReader, true);
        setLearningMode(lm);
        setFeatureMode(fm);
        addAdapterConfiguration(adapter);
        addFeatureSet(featureSet);
    }

    /**
     * Adds a machine learning adapter configuration. Several configurations for the same adapter
     * (or for different ones) can be added by calling this method multiple times. Each
     * configuration is executed automatically. Using several configurations of the same adapter is
     * furthermore quickly executed as the expensive feature extraction step is executed only once
     * and then reused as often as there are configurations of a the same adapter.
     * 
     * @param adapter
     *            the adapter that shall be executed
     * @param arguments
     *            the parametrization of this adapter - optional
     */
    public void addAdapterConfiguration(TcShallowLearningAdapter adapter, String... arguments)
    {
        this.adapter.add(adapter);
        this.arguments.add(arguments);
    }

    /**
     * Wires all provided information into a parameter space object that can be provided to an
     * experiment
     * 
     * @return the parameter space filled with the provided information
     */
    public ParameterSpace buildParameterSpace()
    {
        List<Dimension<?>> dimensions = new ArrayList<>();

        dimensions.add(getAsDimensionMachineLearningAdapter());
        dimensions.add(getAsDimensionFeatureMode());
        dimensions.add(getAsDimensionLearningMode());
        dimensions.add(getAsDimensionFeatureSets());
        dimensions.add(getAsDimensionReaders());
        dimensions.addAll(additionalDimensions);

        ParameterSpace ps = new ParameterSpace();
        ps.setDimensions(dimensions.toArray(new Dimension<?>[0]));

        return ps;
    }
    
    public void runExperiment() throws Exception
    {

        if (experiment == null) {
            throw new NullPointerException("The experiment has not been set");
        }

        ParameterSpace pSpace = buildParameterSpace();
        experiment.setParameterSpace(pSpace);
        Lab.getInstance().run(experiment);
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
            String[] strings = arguments.get(i);

            List<Object> o = new ArrayList<>();
            o.add(a);
            for (String s : strings) {
                o.add(s);
            }

            Map<String, Object> m = new HashedMap<>();
            m.put(DIM_CLASSIFICATION_ARGS, o);
            m.put(DIM_DATA_WRITER, a.getDataWriterClass());
            m.put(DIM_FEATURE_USE_SPARSE, a.useSparseFeatures() + "");

            maps.add(m);
        }
        return maps;
    }

    /**
     * Sets the readers of an experiment. Overwrites any previously added readers.
     * 
     * @param dimReaders
     */
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

    public void addFeatureSet(TcFeatureSet featureSet)
    {
        sanityCheckFeatureSet(featureSet);
        this.featureSets.add(featureSet);
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
     * Provides a reader that is added to the setup.
     * 
     * @param reader
     *            the reader
     * @param isTrain
     *            indicates if the reader provides information for training or testing data
     * @throws IllegalStateException
     *             if more than two reader instances are added
     */
    public void addReader(CollectionReaderDescription reader, boolean isTrain)
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
    }

    /**
     * Adds additional user-defined dimensions that are not part of the minimal necessary
     * configuration.
     * 
     * @param dim
     *            A dimension for adding to the experiment
     */
    public void addAdditionalDimension(Dimension<?> dim)
    {

        if (dim == null) {
            throw new NullPointerException("The added dimension is null");
        }

        additionalDimensions.add(dim);
    }

    private void sanityCheckReaders() throws IllegalStateException
    {
        if (readers.size() > 2) {
            throw new IllegalStateException(
                    "More than two readers have been added. Train-test experiments require two data readers, one for train, one for test. Cross-validation experiments require only one.");
        }

    }

    public void setLearningMode(LearningMode learningMode)
    {
        if (learningMode == null) {
            throw new NullPointerException("Learning mode is null");
        }

        this.learningMode = learningMode.toString();
    }
    
    public void setFeatureMode(FeatureMode featureMode)
    {
        if (featureMode == null) {
            throw new NullPointerException("Feature mode is null");
        }

        this.featureMode = featureMode.toString();
    }

    public void setExperiment(ShallowLearningExperiment_ImplBase experiment)
    {

        if (experiment == null) {
            throw new NullPointerException("The experiment is null");
        }

        this.experiment = experiment;
    }
    
    public void setExperiment(ExperimentType type, String experimentName, int... numFolds)
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
    }
    
    public void addExperimentReports(ReportBase... reports)
    {
        if (experiment == null) {
            throw new NullPointerException("The experiment is not set");
        }
        for (ReportBase r : reports) {
            experiment.addReport(r);
        }
    }
    
    public void setExperimentPreprocessing(AnalysisEngineDescription preprocessing)
    {
        if (experiment == null) {
            throw new NullPointerException("The experiment is not initialized");
        }
        experiment.setPreprocessing(preprocessing);
    }
    
    public void setExperimentName(String experimentName)
    {
        if (experiment == null) {
            throw new NullPointerException("The experiment is not initialized");
        }
        experiment.setExperimentName(experimentName);
    }

}
