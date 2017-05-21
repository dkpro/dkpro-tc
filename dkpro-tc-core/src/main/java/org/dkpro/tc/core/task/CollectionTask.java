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
package org.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.tc.core.Constants.DIM_FILES_TRAINING;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.uima.task.impl.UimaTaskBase;
import org.dkpro.tc.core.task.uima.OutcomeCollector;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

/**
 * Iterates over all documents - train and test together - and collects general
 * information about the entire data set
 */
public class CollectionTask extends UimaTaskBase {

	@Discriminator(name = DIM_FILES_TRAINING)
	private Collection<String> files_training;

	/**
	 * Public name of the task key
	 */
	public static final String OUTPUT_KEY = "output";

	/**
	 * Public name of the folder where meta information will be stored within
	 * the task
	 */

	@Override
	public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
			throws ResourceInitializationException, IOException {
		// TrainTest setup: input files are set as imports
		File train = aContext.getFolder(InitTask.OUTPUT_KEY_TRAIN, AccessMode.READONLY);
		Collection<File> files = FileUtils.listFiles(train, new String[] { "bin" }, true);
		if (!isCrossValidation()) {
			File test = aContext.getFolder(InitTask.OUTPUT_KEY_TEST, AccessMode.READONLY);
			files.addAll(FileUtils.listFiles(test, new String[] { "bin" }, true));
		}
		return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS, files);
	}

	private boolean isCrossValidation() {
		return files_training != null;
	}

	@Override
	public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
			throws ResourceInitializationException, IOException {

		// Collects all outcomes that can occur
		return createEngineDescription(OutcomeCollector.class, OutcomeCollector.PARAM_TARGET_FOLDER,
				aContext.getFolder(OUTPUT_KEY, AccessMode.READWRITE).getPath());
	}

}