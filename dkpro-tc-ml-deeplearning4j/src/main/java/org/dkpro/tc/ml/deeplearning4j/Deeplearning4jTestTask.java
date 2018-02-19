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
package org.dkpro.tc.ml.deeplearning4j;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.ml.deeplearning4j.user.TcDeepLearning4jUser;

public class Deeplearning4jTestTask extends ExecutableTaskBase implements Constants {
	public static final String PREDICTION_FILE = "prediction.txt";

	@Discriminator(name = DeepLearningConstants.DIM_USER_CODE)
	private TcDeepLearning4jUser userCode;

	@Discriminator(name = DeepLearningConstants.DIM_MAXIMUM_LENGTH)
	private Integer maximumLength;
	
	@Discriminator(name = DeepLearningConstants.DIM_SEED_VALUE)
	private Integer seed;
	
	@Discriminator(name = DIM_BIPARTITION_THRESHOLD)
	private double threshold;

	@Override
	public void execute(TaskContext aContext) throws Exception {
		File trainDataVector = getDataVector(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA);
		File trainOutcomeVector = getDataOutcome(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA);

		File testDataVector = getDataVector(aContext, TEST_TASK_INPUT_KEY_TEST_DATA);
		File testOutcomeVector = getDataOutcome(aContext, TEST_TASK_INPUT_KEY_TEST_DATA);

		File embeddingPath = getEmbedding(aContext);

		File outputTarget = aContext.getFile(PREDICTION_FILE, AccessMode.READWRITE);
		
		if (seed==null){
			seed = 123456789;
		}

		userCode.run(trainDataVector, trainOutcomeVector, testDataVector, testOutcomeVector, embeddingPath, seed,
				outputTarget);
	}

	private File getDataOutcome(TaskContext aContext, String key) throws FileNotFoundException {
		File folder = aContext.getFolder(key, AccessMode.READONLY);
		File vector = new File(folder, DeepLearningConstants.FILENAME_OUTCOME_VECTOR);

		if (!vector.exists()) {
			throw new FileNotFoundException("Could not locate file [" + DeepLearningConstants.FILENAME_OUTCOME_VECTOR
					+ "] in folder [" + folder.getAbsolutePath() + "]");
		}
		return vector;
	}

	private File getDataVector(TaskContext aContext, String key) throws FileNotFoundException {
		File folder = aContext.getFolder(key, AccessMode.READONLY);
		File vector = new File(folder, DeepLearningConstants.FILENAME_INSTANCE_VECTOR);

		if (!vector.exists()) {
			throw new FileNotFoundException("Could not locate file [" + DeepLearningConstants.FILENAME_INSTANCE_VECTOR
					+ "] in folder [" + folder.getAbsolutePath() + "]");
		}
		return vector;
	}

	private File getEmbedding(TaskContext aContext) {
		File folder = aContext.getFolder(TcDeepLearningAdapter.EMBEDDING_FOLDER, AccessMode.READONLY);
		File embedding = new File(folder, DeepLearningConstants.FILENAME_PRUNED_EMBEDDING);

		if (!embedding.exists()) {
			LogFactory.getLog(getClass())
					.debug("Did not find an embedding at location [" + folder.getAbsolutePath() + "]");
			return null;
		}

		return embedding;
	}

}
