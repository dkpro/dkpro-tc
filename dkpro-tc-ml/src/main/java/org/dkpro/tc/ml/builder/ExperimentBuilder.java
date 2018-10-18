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
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentLearningCurve;
import org.dkpro.tc.ml.ExperimentLearningCurveTrainTest;
import org.dkpro.tc.ml.ExperimentSaveModel;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.base.ShallowLearningExperiment_ImplBase;
import org.dkpro.tc.ml.report.CrossValidationReport;
import org.dkpro.tc.ml.report.LearningCurveReport;
import org.dkpro.tc.ml.report.TrainTestReport;

/**
 * Builder class that offers a simplified wiring of DKPro TC experiments.
 */
public class ExperimentBuilder implements Constants {
	protected List<TcShallowLearningAdapter> backends;
	protected List<List<String>> arguments;
	protected List<ReportBase> reports;
	protected String learningMode;
	protected String featureMode;
	protected Map<String, Object> readerMap;
	protected List<TcFeatureSet> featureSets;
	protected List<Dimension<?>> additionalDimensions;
	protected ShallowLearningExperiment_ImplBase experiment;
	protected ParameterSpace parameterSpace;
	protected String experimentName;
	protected ExperimentType type;
	protected AnalysisEngineDescription preprocessing;
	protected List<String> featureFilter;
	protected List<Map<String, Object>> additionalMapDimensions;

	int numFolds = -1;
	double bipartitionThreshold = -1;
	File outputFolder;
	private int learningCurveLimit = -1;

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
			this.backends.add(b.getAdapter());
			this.arguments.add(b.getParametrization());
		}

		return this;
	}

	/**
	 * Wires the parameter space. The created parameter space can be passed to
	 * experimental setup.
	 * 
	 * @return a parameter space
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
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

	protected Dimension<?> getAsDimensionsBipartionThreshold() {
		return Dimension.create(DIM_BIPARTITION_THRESHOLD, bipartitionThreshold);
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

	protected Dimension<?> getAsDimensionReaders() {
		if (!readerMap.keySet().contains(DIM_READER_TRAIN)) {
			throw new IllegalStateException("You must provide at least a training data reader");
		}

		return Dimension.createBundle(DIM_READERS, readerMap);
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

	protected Dimension<?> getAsDimensionLearningMode() {
		if (learningMode == null) {
			throw new NullPointerException(
					"No learning mode set, please provide this information via the respective setter method");
		}

		return Dimension.create(DIM_LEARNING_MODE, learningMode);
	}

	protected Dimension<?> getAsDimensionFeatureMode() {
		if (featureMode == null) {
			throw new NullPointerException(
					"No feature mode set, please provide this information via the respective setter method");
		}

		return Dimension.create(DIM_FEATURE_MODE, featureMode);
	}

	protected List<Map<String, Object>> getAdapterInfo() {
		if (backends.size() == 0) {
			throw new IllegalStateException(
					"No machine learning adapter set - Provide at least one machine learning configuration");
		}

		List<Map<String, Object>> maps = new ArrayList<>();

		for (int i = 0; i < backends.size(); i++) {
			TcShallowLearningAdapter a = backends.get(i);
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

	/**
	 * Sets the data reader which reads the training data
	 * 
	 * @param reader the {@link CollectionReaderDescription} that can be created via
	 *               {@link CollectionReaderFactory} from a reader class.
	 * @return the builder object
	 * 
	 */
	public ExperimentBuilder dataReaderTrain(CollectionReaderDescription reader) {
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
	 * @param reader the {@link CollectionReaderDescription} that can be created via
	 *               {@link CollectionReaderFactory} from a reader class.
	 * @return the builder object
	 */
	public ExperimentBuilder dataReaderTest(CollectionReaderDescription reader) throws IllegalStateException {
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
	 * Allows the user to set additional dimensions. This is for advanced users that
	 * use dimensions that are not part of the minimal configuration for an
	 * experiment. This method will remove all previously set additional dimensions
	 * and replaces them with the newly provided one.
	 * 
	 * @param dim a list of dimensions
	 * @return the builder object
	 */
	public ExperimentBuilder additionalDimensions(Dimension<?>... dim) {

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
	public final ExperimentBuilder additionalDimensions(Map<String, Object>... dimensions) {

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
	 * @param learningMode The learning mode
	 * @return The builder object
	 */
	public ExperimentBuilder learningMode(LearningMode learningMode) {
		if (learningMode == null) {
			throw new NullPointerException("Learning mode is null");
		}

		this.learningMode = learningMode.toString();
		return this;
	}

	/**
	 * Sets the feature mode of the experiment
	 * 
	 * @param featureMode The feature mode
	 * @return The builder object
	 */
	public ExperimentBuilder featureMode(FeatureMode featureMode) {
		if (featureMode == null) {
			throw new NullPointerException("Feature mode is null");
		}

		this.featureMode = featureMode.toString();
		return this;
	}

	/**
	 * Sets an externally pre-defined experimental setup
	 * 
	 * @param experiment An experimental setup
	 * @return The builder object
	 */
	public ExperimentBuilder experiment(ShallowLearningExperiment_ImplBase experiment) {

		if (experiment == null) {
			throw new NullPointerException("The experiment is null");
		}

		this.experiment = experiment;
		return this;
	}

	/**
	 * This switch is relevant for {@link ExperimentType#LEARNING_CURVE} and
	 * {@link ExperimentType#LEARNING_CURVE_FIXED_TEST_SET} Sets a maximum number of
	 * train set permutations on each learning curve stage. For instance, on the
	 * first stage of a ten fold run you will get the following folds on the first
	 * two stages: <code>
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
	 * </code>
	 * 
	 * This limitations limits the number of runs in each stage to the number
	 * specified as parameter. This will considerably speed up the learning curve.
	 * 
	 * @param learningCurveLimit The limit which must be non-zero positive integer
	 * @return The builder object
	 */
	public ExperimentBuilder learningCurveLimit(int learningCurveLimit) {
		if(learningCurveLimit <= 0) {
			throw new IllegalArgumentException("Learning curve limit must be a positive integer greater zero but was ["
					+ learningCurveLimit + "]");
		}
		this.learningCurveLimit = learningCurveLimit;
		return this;
	}

	/**
	 * Creates an experimental setup with a pre-defined type
	 * 
	 * @param type           The type of experiment
	 * @param experimentName The name of the experiment
	 * @return The builder object
	 */
	public ExperimentBuilder experiment(ExperimentType type, String experimentName) {
		this.type = type;
		this.experimentName = experimentName;
		return this;
	}

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

	private void readersCheckExperimentCrossValidation() {
		if (!(experiment instanceof ExperimentCrossValidation) && !(experiment instanceof ExperimentLearningCurve)) {
			return;
		}

		if (readerMap.size() < 1) {
			throw new IllegalStateException("No reader set for reading training data");
		} else if (readerMap.size() > 1) {
			LogFactory.getLog(getClass())
					.warn("Experiment type [" + experiment.getClass().getSimpleName() + "] requires only one reader ["
							+ readerMap.size() + "] were found - additional readers will be ignored");
		}
	}

	private void readersCheckExperimentTrainTestCheck() {
		if (!(experiment instanceof ExperimentTrainTest) && !(experiment instanceof ExperimentLearningCurveTrainTest)) {
			return;
		}

		if (readerMap.size() < 2) {
			throw new IllegalStateException("Experiment type [" + experiment.getClass().getSimpleName()
					+ "] requires two readers (train/test) but [" + readerMap.size() + "] readers were provided");
		}
	}

	protected int getCvFolds() {
		// -1 defines leave one out and is, thus, valid as parameter. Any lower number
		// is not
		if (numFolds < -1) {
			throw new IllegalArgumentException("Specified number of folds [" + numFolds
					+ "] is invlaid, set either [-1] for LEAVE-ONE-OUT or a positive value");
		}

		LogFactory.getLog(getClass()).debug("Number of folds set to [" + numFolds + "]");

		return numFolds;
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

	/**
	 * Sets user-specific reports for the experiments. Calling this method multiple
	 * times overwrites all changes from the previous calls.
	 * 
	 * @param reports One or more reports
	 * @return The builder object
	 */
	public ExperimentBuilder reports(ReportBase... reports) {
		this.reports = new ArrayList<>();
		for (ReportBase r : reports) {
			LogFactory.getLog(getClass())
					.debug("Add report [" + r.getClass().getSimpleName() + "] to experimental setup");
			this.reports.add(r);
		}

		return this;
	}

	/**
	 * Sets a {@link AnalysisEngineDescription} which contains all necessary
	 * pre-processing steps. Multiple {@link AnalysisEngineDescription} can be
	 * combined into a single one by nesting multiple descriptions, e.g.
	 * 
	 * <pre>
	 *     AnalysisEngineFactory.createEngineDescription(
	 *              AnalysisEngineFactory.createEngineDescription(abc1.class),
	 *              AnalysisEngineFactory.createEngineDescription(abc2.class),
	 *              AnalysisEngineFactory.createEngineDescription(...),
	 *     );
	 * </pre>
	 * 
	 * @param preprocessing the preprocessing component
	 * @return The builder object
	 */
	public ExperimentBuilder preprocessing(AnalysisEngineDescription preprocessing) {
		this.preprocessing = preprocessing;
		return this;
	}

	/**
	 * Sets the number of folds for {@link ExperimentType#CROSS_VALIDATION}.
	 * Defaults to ten if not set by the user. Is ignored for other experiment
	 * types.
	 * 
	 * @param numFolds The number of folds
	 * @return The builder object
	 */
	public ExperimentBuilder numFolds(int numFolds) {
		this.numFolds = numFolds;
		return this;
	}

	/**
	 * Sets the experiment Name
	 * 
	 * @param experimentName The name
	 * @return The builder object
	 */
	public ExperimentBuilder name(String experimentName) {
		this.experimentName = experimentName;
		return this;
	}

	public ExperimentBuilder bipartitionThreshold(double threshold) {
		this.bipartitionThreshold = threshold;
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
	public ShallowLearningExperiment_ImplBase build() throws Exception {

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

	protected void setExperiment() throws Exception {
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
	 * @throws Exception In case of an invalid configuration or missing mandatory
	 *                   values
	 */
	public void run() throws Exception {
		ShallowLearningExperiment_ImplBase build = build();
		Lab.getInstance().run(build);
	}

	/**
	 * Sets the output folder to which the model is saved when the experiment type
	 * is set to save model
	 * 
	 * @param filePath path to the file
	 * @return the builder itself
	 */
	public ExperimentBuilder outputFolder(String filePath) {
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

}
