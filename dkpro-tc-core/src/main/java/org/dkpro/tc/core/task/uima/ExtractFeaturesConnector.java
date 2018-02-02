/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.core.task.uima;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.feature.filter.FeatureFilter;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.util.TaskUtils;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * UIMA analysis engine that is used in the {@link ExtractFeaturesTask} to apply
 * the feature extractors on each CAS.
 */
public class ExtractFeaturesConnector extends ConnectorBase {

	/**
	 * Directory in which the extracted features will be stored
	 */
	public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";

	@ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
	private File outputDirectory;

	/**
	 * Whether an ID should be added to each instance in the feature file
	 */
	public static final String PARAM_ADD_INSTANCE_ID = "addInstanceId";

	@ConfigurationParameter(name = PARAM_ADD_INSTANCE_ID, mandatory = true, defaultValue = "true")
	private boolean addInstanceId;

	@ConfigurationParameter(name = PARAM_FEATURE_FILTERS, mandatory = true)
	private String[] featureFilters;

	@ConfigurationParameter(name = PARAM_OUTCOMES, mandatory = true)
	private String[] outcomes;

	@ConfigurationParameter(name = PARAM_USE_SPARSE_FEATURES, mandatory = true)
	private boolean useSparseFeatures;

	@ConfigurationParameter(name = PARAM_DATA_WRITER_CLASS, mandatory = true)
	private String dataWriterClass;

	@ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true, defaultValue = Constants.LM_SINGLE_LABEL)
	private String learningMode;

	@ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true, defaultValue = Constants.FM_DOCUMENT)
	private String featureMode;

	@ConfigurationParameter(name = PARAM_DEVELOPER_MODE, mandatory = true, defaultValue = "false")
	private boolean developerMode;

	@ConfigurationParameter(name = PARAM_APPLY_WEIGHTING, mandatory = true, defaultValue = "false")
	private boolean applyWeighting;

	@ConfigurationParameter(name = PARAM_IS_TESTING, mandatory = true)
	private boolean isTesting;

	@ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
	protected FeatureExtractorResource_ImplBase[] featureExtractors;

	DataWriter dsw;

	TreeSet<String> featureNames;
	boolean writeFeatureNames = true;

	BufferedWriter documentIdLogger; // writes the document ids stored in the
										// DocumentMetaData object

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		try {
			
			initDocumentMetaDataLogger();

			if (isTesting) {
				File featureNamesFile = new File(outputDirectory, Constants.FILENAME_FEATURES);
				featureNames = new TreeSet<>(FileUtils.readLines(featureNamesFile, "utf-8"));
			}

			if (featureExtractors.length == 0) {
				context.getLogger().log(Level.SEVERE, "No feature extractors have been defined.");
				throw new ResourceInitializationException();
			}

			dsw = (DataWriter) Class.forName(dataWriterClass).newInstance();
			dsw.init(outputDirectory, useSparseFeatures, learningMode, applyWeighting, outcomes);
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	private void initDocumentMetaDataLogger() throws Exception {
		documentIdLogger = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(outputDirectory, Constants.FILENAME_DOCUMENT_META_DATA_LOG))));
		documentIdLogger.write(
				"# Order in which JCas documents have been processed and their information from DocumentMetaData");
		documentIdLogger.write("\n");
		documentIdLogger.write("#ID\tTitle");
		documentIdLogger.write("\n");
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		LogFactory.getLog(getClass()).info("--- feature extraction for CAS with id ["
				+ JCasUtil.selectSingle(jcas, JCasId.class).getId() + "] ---");
		
		DocumentMetaData dmd = null;
		try{
			dmd = JCasUtil.selectSingle(jcas, DocumentMetaData.class);
			documentIdLogger.write(dmd.getDocumentId() + "\t" + dmd.getDocumentTitle());
			documentIdLogger.write("\n");
		}catch(Exception e){
			//annotation missing
		}

		if (featureNames == null) {
			getFeatureNames(jcas);
		}

		List<Instance> instances = new ArrayList<Instance>();
		try {
			if (featureMode.equals(Constants.FM_SEQUENCE)) {
				instances = TaskUtils.getMultipleInstancesSequenceMode(featureExtractors, jcas, addInstanceId,
						useSparseFeatures);
			} else if (featureMode.equals(Constants.FM_UNIT)) {
				instances = TaskUtils.getMultipleInstancesUnitMode(featureExtractors, jcas, addInstanceId,
						useSparseFeatures);
			} else {
				instances.add(TaskUtils.getSingleInstance(featureMode, featureExtractors, jcas, developerMode,
						addInstanceId, useSparseFeatures));
			}

			/*
			 * filter-out feature names which did not occur during training if
			 * we are in the testing stage
			 */
			instances = enforceMatchingFeatures(instances);

			if (featureFilters.length > 0 || !dsw.canStream()) {
				dsw.writeGenericFormat(instances);
			} else {
				dsw.writeClassifierFormat(instances);
			}

		} catch (Exception e1) {
			throw new AnalysisEngineProcessException(e1);
		}

		if (writeFeatureNames) {
			writeFeatureNames();
			writeFeatureNames = false;
		}
	}

	private void getFeatureNames(JCas jcas) throws AnalysisEngineProcessException {
		// We run one time through feature extraction to get all features names
		try {
			List<Instance> instances = new ArrayList<>();
			if (featureMode.equals(Constants.FM_SEQUENCE)) {
				instances = TaskUtils.getMultipleInstancesSequenceMode(featureExtractors, jcas, addInstanceId, false);
			} else if (featureMode.equals(Constants.FM_UNIT)) {
				instances = TaskUtils.getMultipleInstancesUnitMode(featureExtractors, jcas, addInstanceId, false);
			} else {
				instances.add(TaskUtils.getSingleInstance(featureMode, featureExtractors, jcas, developerMode,
						addInstanceId, false));
			}

			Map<String, FeatureDescription> featDesc = new HashMap<>();
			featureNames = new TreeSet<>();
			for (Feature f : instances.get(0).getFeatures()) {
				featureNames.add(f.getName());

				if (!featDesc.containsKey(f.getName())) {
					featDesc.put(f.getName(), determineType(f));
				}
			}

			FileUtils.writeLines(new File(outputDirectory, Constants.FILENAME_FEATURES), "utf-8", featureNames);

			StringBuilder sb = new StringBuilder();
			List<String> keyList = new ArrayList<String>(featDesc.keySet());
			Collections.sort(keyList);
			for (String k : keyList) {
				FeatureDescription fd = featDesc.get(k);
				sb.append(k + "\t" + fd.getDescription());
				if (fd.getEnumType() != null) {
					sb.append("\t" + fd.getEnumType());
				}
				sb.append(System.lineSeparator());
			}
			FileUtils.writeStringToFile(new File(outputDirectory, Constants.FILENAME_FEATURES_DESCRIPTION),
					sb.toString(), "utf-8");

		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private FeatureDescription determineType(Feature f) {
		Object value = f.getValue();
		if (value instanceof Double) {
			return new FeatureDescription(FeatureType.NUM_FLOATING_POINT);
		} else if (value instanceof Integer) {
			return new FeatureDescription(FeatureType.NUM_INTEGER);
		} else if (value instanceof Number) {
			return new FeatureDescription(FeatureType.NUM);
		} else if (value instanceof Enum) {
			FeatureDescription featureDescription = new FeatureDescription(FeatureType.ENUM);
			featureDescription.setEnumType((Enum) value);
			return featureDescription;
		} else if (value instanceof Boolean) {
			return new FeatureDescription(FeatureType.BOOLEAN);
		}

		return new FeatureDescription(FeatureType.UNKNOWN);
	}

	private List<Instance> enforceMatchingFeatures(List<Instance> instances) {
		if (!isTesting) {
			return instances;
		}

		List<Instance> out = new ArrayList<>();

		for (Instance i : instances) {
			List<Feature> newFeatures = new ArrayList<>();
			for (Feature feat : i.getFeatures()) {
				if (!featureNames.contains(feat.getName())) {
					continue;
				}
				newFeatures.add(feat);
			}
			i.setFeatures(newFeatures);
			out.add(i);
		}
		return out;
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		try {

			if (featureFilters.length > 0) {
				applyFilter(new File(outputDirectory, dsw.getGenericFileName()));
			}

			if (!isTesting) {
				writeFeatureNames();
			}

			if (featureFilters.length > 0 || !dsw.canStream()) {
				// if we use generic mode we have to finalize the feature
				// extraction by transforming
				// the generic file into the classifier-specific data format
				dsw.transformFromGeneric();
			}

			dsw.close();
			
			documentIdLogger.close();

		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	private void writeFeatureNames() throws AnalysisEngineProcessException {
		try {
			FileUtils.writeLines(new File(outputDirectory, Constants.FILENAME_FEATURES), featureNames);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void applyFilter(File jsonTempFile) throws AnalysisEngineProcessException {
		// apply filters that influence the whole feature store
		// filters are applied in the order that they appear as parameters
		for (String filterString : featureFilters) {
			FeatureFilter filter;
			try {
				filter = (FeatureFilter) Class.forName(filterString).newInstance();

				if (filter.isApplicableForTraining() && !isTesting || filter.isApplicableForTesting() && isTesting) {
					filter.applyFilter(jsonTempFile);
				}
			} catch (Exception e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
	}
}