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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.tc.core.Constants.*;
import static org.dkpro.tc.core.DeepLearningConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.uima.task.impl.UimaTaskBase;
import org.dkpro.tc.core.task.deep.anno.FilterVocabularyByEmbeddingAnnotator;
import org.dkpro.tc.core.task.deep.anno.IdentificationCollector;
import org.dkpro.tc.core.task.uima.AssignIdConnector;
import org.dkpro.tc.core.task.uima.PreprocessConnector;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;

public class InitTaskDeep
    extends UimaTaskBase
{

    @Discriminator(name = DIM_READER_TRAIN)
    protected CollectionReaderDescription readerTrain;
    @Discriminator(name = DIM_READER_TEST)
    protected CollectionReaderDescription readerTest;
    @Discriminator(name = DIM_FEATURE_MODE)
    private String mode;
    @Discriminator(name = DIM_MAXIMUM_LENGTH)
    private Integer maximumLength;
    @Discriminator(name = DIM_PRETRAINED_EMBEDDINGS)
    private File embedding;
    @Discriminator(name = DIM_USE_ONLY_VOCABULARY_COVERED_BY_EMBEDDING)
    private boolean dropVocabWithoutEmbedding;

    private boolean isTesting = false;

    private AnalysisEngineDescription preprocessing;

    /**
     * Public name of the folder under which the preprocessed training data file will be stored
     * within the task
     */
    public static final String OUTPUT_KEY_TRAIN = "preprocessorOutputTrain";
    /**
     * Public name of the folder under which the preprocessed test data file will be stored within
     * the task
     */
    public static final String OUTPUT_KEY_TEST = "preprocessorOutputTest";

    private List<String> operativeViews;

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        CollectionReaderDescription readerDesc;
        if (!isTesting) {
            if (readerTrain == null) {
                throw new ResourceInitializationException(
                        new IllegalStateException("readerTrain is null"));
            }

            readerDesc = readerTrain;
        }
        else {
            if (readerTest == null) {
                throw new ResourceInitializationException(
                        new IllegalStateException("readerTest is null"));
            }

            readerDesc = readerTest;
        }

        return readerDesc;
    }

    // what should actually be done in this task
    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        String output = isTesting ? OUTPUT_KEY_TEST : OUTPUT_KEY_TRAIN;

        File folder = aContext.getFolder(output, AccessMode.READWRITE);

        AnalysisEngineDescription xmiWriter = createEngineDescription(BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION, folder.getPath(),
                BinaryCasWriter.PARAM_FORMAT, "6+");

        // special connector that just checks whether there are no instances and outputs a
        // meaningful error message then
        // should be added before preprocessing
        AnalysisEngineDescription emptyProblemChecker = AnalysisEngineFactory
                .createEngineDescription(PreprocessConnector.class);

        if (operativeViews != null) {
            AggregateBuilder builder = new AggregateBuilder();
            for (String viewName : operativeViews) {
                builder.add(createEngineDescription(preprocessing), CAS.NAME_DEFAULT_SOFA,
                        viewName);
            }
            preprocessing = builder.createAggregateDescription();
        }
        
        
        AggregateBuilder builder = new AggregateBuilder();
        
        if(dropVocabWithoutEmbedding){
            builder.add(createEngineDescription(FilterVocabularyByEmbeddingAnnotator.class,
                    FilterVocabularyByEmbeddingAnnotator.PARAM_EMBEDDING, embedding));
        }
        builder.add(emptyProblemChecker);
        builder.add(preprocessing);
        builder.add(xmiWriter);
        builder.add(createEngineDescription(IdentificationCollector.class,
                IdentificationCollector.PARAM_TARGET_DIRECTORY, folder,
                IdentificationCollector.PARAM_MODE, mode,
                IdentificationCollector.PARAM_USER_SET_MAXIMUM_LENGTH, maximumLength));

        return builder.createAggregateDescription();
    }

    public void setTesting(boolean isTesting)
    {
        this.isTesting = isTesting;
    }

    public AnalysisEngineDescription getPreprocessing()
    {
        return preprocessing;
    }

    public void setPreprocessing(AnalysisEngineDescription preprocessing)
    {
        this.preprocessing = preprocessing;
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }

}