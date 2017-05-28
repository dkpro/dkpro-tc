/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.core.task.deep;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.tc.core.Constants.DIM_FILES_ROOT;
import static org.dkpro.tc.core.Constants.DIM_FILES_TRAINING;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.uima.task.impl.UimaTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

/**
 * Collects information about the entire document
 */
public class PreparationTask extends UimaTaskBase {

	/**
	 * Public name of the task key
	 */
	public static final String OUTPUT_KEY = "output";
	/**
	 * Public name of the folder where meta information will be stored within
	 * the task
	 */
	public static final String INPUT_KEY_TRAIN = "inputTrain";
	public static final String INPUT_KEY_TEST = "inputTest";

	@Discriminator(name = Constants.DIM_FEATURE_MODE)
	private String mode;

	@Discriminator(name = DeepLearningConstants.DIM_PRETRAINED_EMBEDDINGS)
	private File embedding;

	@Discriminator(name = DeepLearningConstants.DIM_MAXIMUM_LENGTH)
	private Integer maximumLength;

	@Discriminator(name = DIM_FILES_ROOT)
	private File filesRoot;

	@Discriminator(name = DIM_FILES_TRAINING)
	private Collection<String> files_training;

	@Discriminator(name = DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER)
	private boolean integerVectorization;

	private TcDeepLearningAdapter mlDeepLearningAdapter;

	@Override
	public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
			throws ResourceInitializationException, IOException {
		File trainRoot = aContext.getFolder(INPUT_KEY_TRAIN, AccessMode.READONLY);
		Collection<File> files = FileUtils.listFiles(trainRoot, new String[] { "bin" }, true);

		if (!isCrossValidation()) {
			File testRoot = aContext.getFolder(INPUT_KEY_TEST, AccessMode.READONLY);
			files.addAll(FileUtils.listFiles(testRoot, new String[] { "bin" }, true));
		}
		return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS, files);
	}

	private boolean isCrossValidation() {
		return filesRoot != null;
	}

	@Override
	public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
			throws ResourceInitializationException, IOException {
		File folder = aContext.getFolder(OUTPUT_KEY, AccessMode.READONLY);

		AggregateBuilder builder = new AggregateBuilder();

		if (integerVectorization) {
			builder.add(AnalysisEngineFactory.createEngineDescription(MappingAnnotator.class,
					MappingAnnotator.PARAM_TARGET_DIRECTORY, folder, MappingAnnotator.PARAM_START_INDEX,
					mlDeepLearningAdapter.lowestIndex()));
		}else{
			builder.add(AnalysisEngineFactory.createEngineDescription(VocabularyOutcomeCollector.class,
					VocabularyOutcomeCollector.PARAM_TARGET_DIRECTORY, folder));
		}

		builder.add(getMaximumLengthDeterminer(folder));
		return builder.createAggregateDescription();

	}

	private AnalysisEngineDescription getMaximumLengthDeterminer(File folder) throws ResourceInitializationException {
		if (mode == null) {
			throw new ResourceInitializationException(new IllegalStateException("Learning model is [null]"));
		}

		if (maximumLength != null && maximumLength > 0) {
			LogFactory.getLog(getClass()).info("Maximum length was set by user to [" + maximumLength + "]");

			writeExpectedMaximumLengthFile(folder);

			return AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
		}

		switch (mode) {
		case Constants.FM_DOCUMENT:
			return AnalysisEngineFactory.createEngineDescription(MaximumLengthAnnotatorDocument2Label.class,
					MaximumLengthAnnotatorDocument2Label.PARAM_TARGET_DIRECTORY, folder);
		case Constants.FM_SEQUENCE:
			return AnalysisEngineFactory.createEngineDescription(MaximumLengthAnnotatorSequence2Label.class,
					MaximumLengthAnnotatorSequence2Label.PARAM_TARGET_DIRECTORY, folder);
		default:
			throw new ResourceInitializationException(
					new IllegalStateException("Mode [" + mode + "] not defined for deep learning experiements"));
		}

	}

	private void writeExpectedMaximumLengthFile(File folder) throws ResourceInitializationException {
		try {
			FileUtils.writeStringToFile(new File(folder, DeepLearningConstants.FILENAME_MAXIMUM_LENGTH),
					maximumLength.toString(), "utf-8");
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	public void setMachineLearningAdapter(TcDeepLearningAdapter mlDeepLearningAdapter) {
		this.mlDeepLearningAdapter = mlDeepLearningAdapter;
	}
}