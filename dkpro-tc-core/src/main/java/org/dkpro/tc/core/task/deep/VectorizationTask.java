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
import static org.dkpro.tc.core.Constants.*;

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
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.task.deep.anno.VectorizationAnnotatorDocument2MultiLabel;
import org.dkpro.tc.core.task.deep.anno.VectorizationAnnotatorSequence2SequenceOfLabel;

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

        switch (featureMode) {
        case Constants.FM_DOCUMENT:
            switch (learningMode) {
            case Constants.LM_SINGLE_LABEL:
            case Constants.LM_REGRESSION:
                return AnalysisEngineFactory.createEngineDescription(
                        VectorizationAnnotatorDocument2MultiLabel.class,
                        VectorizationAnnotatorDocument2MultiLabel.PARAM_TARGET_DIRECTORY, outputDir,
                        VectorizationAnnotatorDocument2MultiLabel.PARAM_PREPARATION_DIRECTORY,
                        mappingDir, VectorizationAnnotatorDocument2MultiLabel.PARAM_TO_INTEGER,
                        integerVectorization);
            case Constants.LM_MULTI_LABEL:
                return AnalysisEngineFactory.createEngineDescription(
                        VectorizationAnnotatorDocument2MultiLabel.class,
                        VectorizationAnnotatorDocument2MultiLabel.PARAM_TARGET_DIRECTORY, outputDir,
                        VectorizationAnnotatorDocument2MultiLabel.PARAM_PREPARATION_DIRECTORY,
                        mappingDir, VectorizationAnnotatorDocument2MultiLabel.PARAM_TO_INTEGER,
                        integerVectorization);
            }
        case Constants.FM_SEQUENCE:
            return AnalysisEngineFactory.createEngineDescription(
                    VectorizationAnnotatorSequence2SequenceOfLabel.class,
                    VectorizationAnnotatorSequence2SequenceOfLabel.PARAM_TARGET_DIRECTORY, outputDir,
                    VectorizationAnnotatorSequence2SequenceOfLabel.PARAM_PREPARATION_DIRECTORY, mappingDir,
                    VectorizationAnnotatorDocument2MultiLabel.PARAM_TO_INTEGER, integerVectorization);
        default:
            throw new ResourceInitializationException(
                    new IllegalStateException("Feature mode [" + featureMode + "] not defined"));
        }

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