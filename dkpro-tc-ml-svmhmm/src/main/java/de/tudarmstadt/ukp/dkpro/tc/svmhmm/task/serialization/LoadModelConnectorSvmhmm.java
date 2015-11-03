/**
 * Copyright 2015
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
 */
package de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.serialization;

import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_CLASSIFIER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import de.tudarmstadt.ukp.dkpro.tc.core.util.SaveModelUtils;
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
			SaveModelUtils.verifyTcVersion(tcModelLocation, getClass());
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
			
			setOutcomes(jcas, predictionsFile, loadMapping);
			
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	private void setOutcomes(JCas jcas, File predictionsFile, BidiMap loadMapping) throws Exception {
	    List<TextClassificationOutcome> outcomes = new ArrayList<TextClassificationOutcome>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
	    int idx=0;
		
		//avoid holding all predictions in RAM (might use a lot of RAM if a few million predictions are being made)
		BufferedReader br = new BufferedReader(new FileReader(predictionsFile));
		String line=null;
		while((line = br.readLine()) != null){
		    Integer i = Integer.valueOf(line);
            String outcome = (String) loadMapping.getKey(i);
            outcomes.get(idx).setOutcome(outcome);
            idx++;
		}
		br.close();
	}

}