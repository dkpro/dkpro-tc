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
import java.util.List;

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

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

/**
 * Collects information about the entire document
 * 
 */
public class PreparationTask
    extends UimaTaskBase
{

    /**
     * Public name of the task key
     */
    public static final String OUTPUT_KEY = "output";
    /**
     * Public name of the folder where meta information will be stored within the task
     */
    public static final String INPUT_KEY_TRAIN = "inputTrain";
    public static final String INPUT_KEY_TEST = "inputTest";

    @Discriminator(name = Constants.DIM_LEARNING_MODE)
    private String learningMode;

    @Discriminator(name = DeepLearningConstants.DIM_PRETRAINED_EMBEDDINGS)
    private File embedding;

    @Discriminator(name = DeepLearningConstants.DIM_MAXIMUM_LENGTH)
    private Integer maximumLength;

    @Discriminator(name = DIM_FILES_ROOT)
    private File filesRoot;

    @Discriminator(name = DIM_FILES_TRAINING)
    private Collection<String> files_training;

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // TrainTest setup: input files are set as imports
        if (filesRoot == null || files_training == null) {
            File trainRoot = aContext.getFolder(INPUT_KEY_TRAIN, AccessMode.READONLY);
            Collection<File> files = FileUtils.listFiles(trainRoot, new String[] { "bin" }, true);
            File testRoot = aContext.getFolder(INPUT_KEY_TEST, AccessMode.READONLY);
            files.addAll(FileUtils.listFiles(testRoot, new String[] { "bin" }, true));

            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files);
        }
        // FIXME: Ensure that we get all data form all folds for embedding creation
        // CV setup: filesRoot and files_atrining have to be set as dimension
        else {
            return createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                    files_training);
        }
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        File folder = aContext.getFolder(OUTPUT_KEY, AccessMode.READONLY);

        AggregateBuilder builder = new AggregateBuilder();

        builder.add(AnalysisEngineFactory.createEngineDescription(PruneEmbeddingAnnotator.class,
                PruneEmbeddingAnnotator.PARAM_TARGET_DIRECTORY, folder,
                PruneEmbeddingAnnotator.PARAM_EMBEDDING_PATH, embedding));

        builder.add(AnalysisEngineFactory.createEngineDescription(MappingAnnotator.class,
                MappingAnnotator.PARAM_TARGET_DIRECTORY, folder));

        builder.add(getMaximumLengthDeterminer(folder));
        return builder.createAggregateDescription();

    }

    private AnalysisEngineDescription getMaximumLengthDeterminer(File folder)
        throws ResourceInitializationException
    {
        if (learningMode == null) {
            throw new ResourceInitializationException(
                    new IllegalStateException("Learning model is [null]"));
        }

        if (maximumLength != null && maximumLength > 0) {
            LogFactory.getLog(getClass())
                    .info("Maximum length was set by user to [" + maximumLength + "]");
            
            writeExpectedMaximumLengthFile(folder);
            
            return AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
        }

        switch (learningMode) {
        case DeepLearningConstants.LM_DOCUMENT_TO_LABEL:
            return AnalysisEngineFactory.createEngineDescription(
                    Document2LabelMaximumLengthAnnotator.class,
                    Document2LabelMaximumLengthAnnotator.PARAM_TARGET_DIRECTORY, folder);
        default:
            throw new ResourceInitializationException(
                    new IllegalStateException("Learning mode [" + learningMode + "] not defined"));
        }

    }

    private void writeExpectedMaximumLengthFile(File folder) throws ResourceInitializationException
    {
        try {
            FileUtils.writeStringToFile(new File(folder, DeepLearningConstants.FILENAME_MAXIMUM_LENGTH), maximumLength.toString(), "utf-8");
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }
}