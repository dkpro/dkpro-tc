/**
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.serialization;

import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_CLASSIFIER;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.BidiMap;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.codehaus.plexus.util.FileUtils;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.ml.uima.TcAnnotatorDocument;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.SVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.SVMHMMUtils;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.writer.SVMHMMDataWriter;

public class LoadModelConnectorSvmhmm extends ModelSerialization_ImplBase {

	@ConfigurationParameter(name = TcAnnotatorDocument.PARAM_TC_MODEL_LOCATION, mandatory = true)
	private File tcModelLocation;

	@ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
	protected FeatureExtractorResource_ImplBase[] featureExtractors;

	@ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
	private String learningMode;

	@ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
	private String featureMode;

	@ConfigurationParameter(name = PARAM_FEATURE_STORE_CLASS, mandatory = true)
	private String featureStoreImpl;

	private File model = null;
	private Path tmpFolderForFeatureFile = null;
	private BidiMap loadMapping;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		try {
			tmpFolderForFeatureFile = Files.createTempDirectory("temp" + System.currentTimeMillis());
			model = new File(tcModelLocation, MODEL_CLASSIFIER);
			loadMapping = loadLabel2IntegerMap();
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}

	private BidiMap loadLabel2IntegerMap() throws IOException {
		File mappingFile = new File(
				tcModelLocation.getAbsolutePath() + "/" + SVMHMMUtils.LABELS_TO_INTEGERS_MAPPING_FILE_NAME);
		return SVMHMMUtils.loadMapping(mappingFile);
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			FeatureStore featureStore = (FeatureStore) Class.forName(featureStoreImpl).newInstance();
			int sequenceId = 0;
			for (TextClassificationSequence seq : JCasUtil.select(jcas, TextClassificationSequence.class)) {

				List<Instance> instances = TaskUtils.getInstancesInSequence(featureExtractors, jcas, seq, true,
						sequenceId++);

				for (Instance instance : instances) {
					featureStore.addInstance(instance);
				}

			}

			SVMHMMDataWriter svmhmmDataWriter = new SVMHMMDataWriter();
			svmhmmDataWriter.write(tmpFolderForFeatureFile.toFile(), featureStore, true, "", false);

			File featureFile = new File(tmpFolderForFeatureFile.toFile() + "/" + new SVMHMMAdapter()
					.getFrameworkFilename(TCMachineLearningAdapter.AdapterNameEntries.featureVectorsFile));
			File augmentedTestFile = SVMHMMUtils.replaceLabelsWithIntegers(featureFile, loadMapping);

			File predictionsFile = FileUtils.createTempFile("svmhmmPrediction", ".txt", null);
			SVMHMMTestTask.callTestCommand(predictionsFile, model, augmentedTestFile);
			
			List<String> getOutcomes = readOutcomes(predictionsFile, loadMapping);
			setPredictedOutcomes(jcas, getOutcomes);
			
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	private List<String> readOutcomes(File predictionsFile, BidiMap loadMapping) throws Exception {
		List<String> outcomes = new ArrayList<>();
		
		List<String> readLines = org.apache.commons.io.FileUtils.readLines(predictionsFile);
		for(String line : readLines){
			Integer i = Integer.valueOf(line);
			String outcome = (String) loadMapping.getKey(i);
			outcomes.add(outcome);
		}
		return outcomes;
	}

	private void setPredictedOutcomes(JCas jcas, List<String> labels) {
		List<TextClassificationOutcome> outcomes = new ArrayList<TextClassificationOutcome>(
				JCasUtil.select(jcas, TextClassificationOutcome.class));
		for (int i = 0; i < outcomes.size(); i++) {
			TextClassificationOutcome o = outcomes.get(i);
			o.setOutcome(labels.get(i));
		}

	}
}