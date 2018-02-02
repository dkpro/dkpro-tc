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
package org.dkpro.tc.core.task.deep;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.tc.core.Constants.DIM_FEATURE_MODE;
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
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.uima.task.impl.UimaTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.task.deep.anno.IdentificationCollector;
import org.dkpro.tc.core.task.deep.anno.VectorizationDocDoc2MultiLabel;
import org.dkpro.tc.core.task.deep.anno.VectorizationDoc2Regression;
import org.dkpro.tc.core.task.deep.anno.VectorizationDoc2SingleLabel;
import org.dkpro.tc.core.task.deep.anno.VectorizationSeq2SeqOfLabel;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

public class VectorizationTask
    extends UimaTaskBase
{

    public static final String OUTPUT_KEY = "output";

    public static final String DATA_INPUT_KEY = "input";
    public static final String MAPPING_INPUT_KEY = "mappingInput";

    @Discriminator(name = DIM_FILES_ROOT)
    private File filesRoot;
    
    @Discriminator(name = DIM_FILES_TRAINING)
    private Collection<String> files_training;
    
    @Discriminator(name = DIM_FILES_VALIDATION)
    private Collection<String> files_validation;
    
    @Discriminator(name = DIM_FEATURE_MODE)
    private String featureMode;
    
    @Discriminator(name = DIM_LEARNING_MODE)
    private String learningMode;    
    
    @Discriminator(name = DeepLearningConstants.DIM_MAXIMUM_LENGTH)
    private int maximumLength;
    
    @Discriminator(name = DeepLearningConstants.DIM_VECTORIZE_TO_INTEGER)
    private boolean integerVectorization;

    private boolean isTesting = false;

    public void setTesting(boolean isTesting)
    {
        this.isTesting = isTesting;
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        File outputDir = aContext.getFolder(OUTPUT_KEY, AccessMode.READWRITE);
        File mappingDir = aContext.getFolder(MAPPING_INPUT_KEY, AccessMode.READONLY);

        return learningModeDependedVectorizationAnnotator(outputDir, mappingDir);
    }

    private AnalysisEngineDescription learningModeDependedVectorizationAnnotator(File outputDir,
            File mappingDir)
                throws ResourceInitializationException
    {
        if (featureMode == null) {
            throw new ResourceInitializationException(
                    new IllegalStateException("Learning model is [null]"));
        }
        
        AggregateBuilder builder = new AggregateBuilder();

		// records which document ids are in the train / test set (this is not
		// clear for cross-validation tasks)
		builder.add(createEngineDescription(IdentificationCollector.class,
								IdentificationCollector.PARAM_TARGET_DIRECTORY, outputDir, 
								IdentificationCollector.PARAM_MODE, featureMode, 
								IdentificationCollector.PARAM_USER_SET_MAXIMUM_LENGTH, maximumLength));

		AnalysisEngineDescription engine = null;

		switch (featureMode) {
		case Constants.FM_DOCUMENT:
			switch (learningMode) {
			case Constants.LM_REGRESSION:
				engine = createEngineDescription(VectorizationDoc2Regression.class,
						VectorizationDoc2Regression.PARAM_TARGET_DIRECTORY, outputDir,
						VectorizationDoc2Regression.PARAM_PREPARATION_DIRECTORY, mappingDir,
						VectorizationDoc2Regression.PARAM_TO_INTEGER, integerVectorization);
				builder.add(engine);
				break;
			case Constants.LM_SINGLE_LABEL:
				engine = createEngineDescription(VectorizationDoc2SingleLabel.class,
						VectorizationDoc2SingleLabel.PARAM_TARGET_DIRECTORY, outputDir,
						VectorizationDoc2SingleLabel.PARAM_PREPARATION_DIRECTORY, mappingDir,
						VectorizationDoc2SingleLabel.PARAM_TO_INTEGER, integerVectorization);
				builder.add(engine);
				break;
			case Constants.LM_MULTI_LABEL:
				engine = AnalysisEngineFactory.createEngineDescription(VectorizationDocDoc2MultiLabel.class,
						VectorizationDocDoc2MultiLabel.PARAM_TARGET_DIRECTORY, outputDir,
						VectorizationDocDoc2MultiLabel.PARAM_PREPARATION_DIRECTORY, mappingDir,
						VectorizationDocDoc2MultiLabel.PARAM_TO_INTEGER, integerVectorization);
				builder.add(engine);
				break;
			}
			break;
		case Constants.FM_SEQUENCE:
			engine = createEngineDescription(VectorizationSeq2SeqOfLabel.class,
					VectorizationSeq2SeqOfLabel.PARAM_TARGET_DIRECTORY, outputDir,
					VectorizationSeq2SeqOfLabel.PARAM_PREPARATION_DIRECTORY, mappingDir,
					VectorizationDocDoc2MultiLabel.PARAM_TO_INTEGER, integerVectorization);
			builder.add(engine);
			break;
		default:
			throw new ResourceInitializationException(new IllegalStateException("Combination of feature mode ["
					+ featureMode + "] with learning mode [" + learningMode + "] not defined"));
		}

		return builder.createAggregateDescription();
    }

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // TrainTest setup: input files are set as imports
        if (filesRoot == null) {
            File root = aContext.getFolder(DATA_INPUT_KEY, AccessMode.READONLY);
            Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
        // CV setup: filesRoot and files_atrining have to be set as dimension
        else {

            Collection<String> files = isTesting ? files_validation : files_training;
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
    }
}