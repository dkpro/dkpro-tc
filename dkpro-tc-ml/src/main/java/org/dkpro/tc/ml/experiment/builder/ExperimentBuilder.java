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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.ml.base.Experiment_ImplBase;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.ExperimentCrossValidation;
import org.dkpro.tc.ml.experiment.ExperimentLearningCurve;
import org.dkpro.tc.ml.experiment.ExperimentLearningCurveTrainTest;
import org.dkpro.tc.ml.experiment.ExperimentSaveModel;
import org.dkpro.tc.ml.experiment.ExperimentTrainTest;
import org.dkpro.tc.ml.report.CrossValidationReport;
import org.dkpro.tc.ml.report.LearningCurveReport;
import org.dkpro.tc.ml.report.TrainTestReport;

/**
 * Builder class that offers a simplified wiring of DKPro TC experiments.
 */
public class ExperimentBuilder extends AbstractBuilder {
    
	protected List<TcShallowLearningAdapter> backends;
	protected List<List<Object>> arguments;
	protected List<TcFeatureSet> featureSets;
	protected List<String> featureFilter;
	
	/**
	 * Creates an experiment builder object.
	 */
	public ExperimentBuilder() {
		//groovy
	}

	/**
	 * Sets one or multiple machine learning adapter configurations. Several
	 * configurations of the same adapter have to be passed as separated
	 * {@link MLBackend} configurations. Calling this method will remove all
	 * previously set {@link MLBackend} configurations.
	 * 
	 * @param backends one or more machine learning backends
	 * @return the builder object
	 */
	public ExperimentBuilder machineLearningBackend(MLBackend... backends) {
		this.backends = new ArrayList<>();
		this.arguments = new ArrayList<>();

		for (MLBackend b : backends) {
			this.backends.add(b.getAdapterShallow());
			this.arguments.add(b.getParametrization());
		}

		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public ParameterSpace getParameterSpace() {
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

	@SuppressWarnings("unchecked")
	protected Dimension<?> getFeatureFilters() {
		return Dimension.create(DIM_FEATURE_FILTERS, featureFilter);
	}

	protected Dimension<?> getAsDimensionMachineLearningAdapter() {
		List<Map<String, Object>> adapterMaps = getAdapterInfo();

		@SuppressWarnings("unchecked")
		Map<String, Object>[] array = adapterMaps.toArray(new Map[0]);
		Dimension<Map<String, Object>> mlaDim = Dimension.createBundle(DIM_MLA_CONFIGURATIONS, array);
		return mlaDim;
	}

	protected Dimension<?> getAsDimensionFeatureSets() {
		if (featureSets == null) {
			throw new NullPointerException("Set either a feature set [" + TcFeatureSet.class.getName()
					+ "]or provide at least a single feature");
		}

		if (featureSets.isEmpty()) {
			throw new IllegalStateException("No feature sets provided, please provide at least one feature set i.e. ["
					+ TcFeatureSet.class.getName() + "]");
		}

		return Dimension.create(DIM_FEATURE_SET, featureSets.toArray(new TcFeatureSet[0]));
	}

	protected List<Map<String, Object>> getAdapterInfo() {
		if (backends.size() == 0) {
			throw new IllegalStateException(
					"No machine learning adapter set - Provide at least one machine learning configuration");
		}

		List<Map<String, Object>> maps = new ArrayList<>();

		for (int i = 0; i < backends.size(); i++) {
			TcShallowLearningAdapter a = backends.get(i);
			List<Object> list = arguments.get(i);

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
	 * {@link #features(TcFeature...)} has to be called to create a valid
	 * experimental setup. Calling this method will remove all previously set
	 * feature sets.
	 * 
	 * @param featureSet one or more feature sets
	 * @return the builder object
	 */
	public ExperimentBuilder featureSets(TcFeatureSet... featureSet) {
		for (TcFeatureSet fs : featureSet) {
			sanityCheckFeatureSet(fs);
		}
		this.featureSets = new ArrayList<>(Arrays.asList(featureSet));
		return this;
	}

	/**
	 * Sets one or more feature filters by their full name i.e. .class.getName()
	 * 
	 * @param filter one or more filter names
	 * @return the builder object
	 */
	public ExperimentBuilder featureFilter(String... filter) {

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
	 * Sets several features to be used in an experiment. If this method is used a
	 * single {@link TcFeatureSet} is created in the background. If multiple feature
	 * sets shall be used use {@link #featureSets(TcFeatureSet...)} Calling this
	 * method will remove all previously set feature configurations
	 * 
	 * @param features one or more features
	 * @return the builder object
	 */
	public ExperimentBuilder features(TcFeature... features) {
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

	protected void sanityCheckFeatureSet(TcFeatureSet featureSet) {
		if (featureSet == null) {
			throw new NullPointerException("The provided feature set is null");
		}
		if (featureSet.isEmpty()) {
			throw new IllegalStateException("The provided feature set contains no features");
		}
	}

	@Override
	public ExperimentBuilder dataReaderTrain(CollectionReaderDescription reader) {
		super.dataReaderTrain(reader);
		return this;
	}

	@Override
	public ExperimentBuilder dataReaderTest(CollectionReaderDescription reader) throws IllegalStateException {
		super.dataReaderTest(reader);
		return this;
	}

	@Override
	public ExperimentBuilder learningMode(LearningMode learningMode) {
	    super.learningMode(learningMode);
		return this;
	}

	@Override
	public ExperimentBuilder featureMode(FeatureMode featureMode) {
		super.featureMode(featureMode);
		return this;
	}

	@Override
	public ExperimentBuilder learningCurveLimit(int learningCurveLimit) {
		super.learningCurveLimit(learningCurveLimit);
		return this;
	}

	@Override
	public ExperimentBuilder experiment(ExperimentType type, String experimentName) {
	    super.experiment(type, experimentName);
		return this;
	}

	@Override
	protected ExperimentBuilder configureExperiment(ExperimentType type, String experimentName) throws Exception {
		switch (type) {
		case TRAIN_TEST:
			experiment = new ExperimentTrainTest(experimentName);
			experiment.addReport(new TrainTestReport());
			readersCheckExperimentTrainTestCheck();
			break;
		case CROSS_VALIDATION:
			int folds = getCvFolds();
			experiment = new ExperimentCrossValidation(experimentName, folds);
			experiment.addReport(new CrossValidationReport());
			readersCheckExperimentCrossValidation();
			break;
		case LEARNING_CURVE:
			folds = getCvFolds();
			experiment = new ExperimentLearningCurve(experimentName, folds, learningCurveLimit);
			experiment.addReport(new LearningCurveReport());
			readersCheckExperimentCrossValidation();
			break;
		case LEARNING_CURVE_FIXED_TEST_SET:
			folds = getCvFolds();
			experiment = new ExperimentLearningCurveTrainTest(experimentName, folds, learningCurveLimit);
			experiment.addReport(new LearningCurveReport());
			readersCheckExperimentTrainTestCheck();
			break;
		case SAVE_MODEL:
			sanityCheckSaveModelExperiment();
			experiment = new ExperimentSaveModel(experimentName, outputFolder);
			break;

		}
		return this;
	}

	protected void sanityCheckSaveModelExperiment() {
		if (outputFolder == null) {
			throw new IllegalStateException("The output folder to which the model will be stored is not set.");
		}

		if (backends == null) {
			throw new IllegalStateException(
					"No machine learning backend select - set exactly one machine learning backend for save model experiments");
		}

		if (backends.size() > 1) {
			throw new IllegalStateException("Only one machine learning backend can be specified for model saving");
		}
	}

	@Override
	public ExperimentBuilder reports(ReportBase... reports) {
		super.reports(reports);
		return this;
	}

	@Override
	public ExperimentBuilder preprocessing(AnalysisEngineDescription preprocessing) {
		super.preprocessing(preprocessing);
		return this;
	}

	@Override
	public ExperimentBuilder numFolds(int numFolds) {
		super.numFolds(numFolds);
		return this;
	}

	@Override
	public ExperimentBuilder name(String experimentName) {
		super.name(experimentName);
		return this;
	}

	public ExperimentBuilder bipartitionThreshold(double threshold) {
		super.bipartitionThreshold(threshold);
		return this;
	}

	/**
	 * Wires the provided parameter to an experiment. The experiment object can be
	 * executed by calling:
	 * 
	 * <pre>
	 *      Lab.getInstance().run(...)
	 * </pre>
	 * 
	 * The method {@link #run()} performs automatically above step as convenience
	 * service.
	 * 
	 * @return The experiment
	 * @throws Exception In case of invalid configurations
	 */
	public Experiment_ImplBase build() throws Exception {

		setExperiment();
		setParameterSpace();
		setPreprocessing();
		setReports();

		return experiment;
	}

	protected void setReports() {
		if (reports != null) {
			for (ReportBase r : reports) {
				experiment.addReport(r);
			}
		}
	}

	protected void setPreprocessing() {
		if (preprocessing != null) {
			experiment.setPreprocessing(preprocessing);
		}
	}

	protected void setParameterSpace() {
		if (parameterSpace == null) {
			parameterSpace = getParameterSpace();
		}
		experiment.setParameterSpace(parameterSpace);
	}
	
	@Override
	public void run() throws Exception {
		super.run();
	}

	@Override
	public ExperimentBuilder outputFolder(String filePath) {
		super.outputFolder(filePath);
		return this;
	}

	@Override
    public ExperimentBuilder outputFolder(File file) {
        super.outputFolder(file);
        return this;
    }
}
