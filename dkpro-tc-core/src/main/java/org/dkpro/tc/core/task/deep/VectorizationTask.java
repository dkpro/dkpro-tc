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
import static org.dkpro.tc.core.Constants.DIM_FILES_VALIDATION;
import static org.dkpro.tc.core.Constants.DIM_LEARNING_MODE;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.uima.task.impl.UimaTaskBase;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

public class VectorizationTask extends UimaTaskBase {

	public static final String OUTPUT_KEY = "output";

	public static final String INPUT_KEY = "input";

	@Discriminator(name = DIM_FILES_ROOT)
	private File filesRoot;
	@Discriminator(name = DIM_FILES_TRAINING)
	private Collection<String> files_training;
	@Discriminator(name = DIM_FILES_VALIDATION)
	private Collection<String> files_validation;
	@Discriminator(name = DIM_LEARNING_MODE)
	private String learningMode;

	private boolean isTesting = false;

	public void setTesting(boolean isTesting) {
		this.isTesting = isTesting;
	}

	@Override
	public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
			throws ResourceInitializationException, IOException {
		File outputDir = aContext.getFolder(OUTPUT_KEY, AccessMode.READWRITE);

		return AnalysisEngineFactory.createEngineDescription(VectorizationAnnotator.class,
				VectorizationAnnotator.PARAM_TARGET_DIRECTORY, outputDir);
	}

	@Override
	public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
			throws ResourceInitializationException, IOException {
		// TrainTest setup: input files are set as imports
		if (filesRoot == null) {
			File root = aContext.getFolder(INPUT_KEY, AccessMode.READONLY);
			Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);
			return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS, files);
		}
		// CV setup: filesRoot and files_atrining have to be set as dimension
		else {

			Collection<String> files = isTesting ? files_validation : files_training;
			return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS, files);
		}
	}
}