/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.IOException;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.ClassificationUnitCasMultiplier;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.PreprocessConnector;

/**
 * Performs the preprocessing steps, that were configured by the user, on the documents.
 * 
 * @author zesch
 * 
 */
public class PreprocessTask
    extends UimaTaskBase
{
    /**
     * Public name of the folder under which the training data file will be stored within the task
     */
    public static final String OUTPUT_KEY_TRAIN = "preprocessorOutputTrain";
    /**
     * Public name of the folder under which the test data file will be stored within the task
     */
    public static final String OUTPUT_KEY_TEST = "preprocessorOutputTest";

    private boolean isTesting = false;
    private List<String> operativeViews;

    @Discriminator
    protected Class<? extends CollectionReader> readerTrain;

    @Discriminator
    protected Class<? extends CollectionReader> readerTest;

    @Discriminator
    protected List<Object> readerTrainParams;

    @Discriminator
    protected List<Object> readerTestParams;

    @Discriminator
    private String featureMode;

    /**
     * @param isTesting
     */
    public void setTesting(boolean isTesting)
    {
        this.isTesting = isTesting;
    }

    private AnalysisEngineDescription preprocessingPipeline;

    /**
     * @return
     */
    public AnalysisEngineDescription getPreprocessingPipeline()
    {
        return preprocessingPipeline;
    }

    /**
     * @param aAggregate
     */
    public void setPreprocessingPipeline(AnalysisEngineDescription preprocessingPipeline)
    {
        this.preprocessingPipeline = preprocessingPipeline;
    }

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        Class<? extends CollectionReader> reader = isTesting ? readerTest : readerTrain;
        List<Object> readerParams = isTesting ? readerTestParams : readerTrainParams;

        CollectionReaderDescription readerDesc = createReaderDescription(reader,
                readerParams.toArray());

        return readerDesc;
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        String output = isTesting ? OUTPUT_KEY_TEST : OUTPUT_KEY_TRAIN;
        AnalysisEngineDescription xmiWriter = createEngineDescription(BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION,
                aContext.getStorageLocation(output, AccessMode.READWRITE).getPath(),
                BinaryCasWriter.PARAM_FORMAT, "6+");
        
        // add a special connector as the first analysis engine that just checks whether there are no instances
        // and outputs a meaningful error message then
        AnalysisEngineDescription emptyProblemChecker = AnalysisEngineFactory.createEngineDescription(PreprocessConnector.class);
        preprocessingPipeline = AnalysisEngineFactory.createEngineDescription(emptyProblemChecker, preprocessingPipeline);

        // check whether we are dealing with pair classification and if so, add PART_ONE and
        // PART_TWO views
        if (featureMode.equals(Constants.FM_PAIR)) {
            AggregateBuilder builder = new AggregateBuilder();
            builder.add(createEngineDescription(preprocessingPipeline), CAS.NAME_DEFAULT_SOFA,
                    Constants.PART_ONE);
            builder.add(createEngineDescription(preprocessingPipeline), CAS.NAME_DEFAULT_SOFA,
                    Constants.PART_TWO);
            preprocessingPipeline = builder.createAggregateDescription();
        }
        else if (operativeViews != null) {
            AggregateBuilder builder = new AggregateBuilder();
            for (String viewName : operativeViews) {
                builder.add(createEngineDescription(preprocessingPipeline), CAS.NAME_DEFAULT_SOFA,
                        viewName);
            }
            preprocessingPipeline = builder.createAggregateDescription();
        }
        // in unit or sequence mode, add cas multiplier
        else if (featureMode.equals(Constants.FM_UNIT) || featureMode.equals(Constants.FM_SEQUENCE)) {
            boolean useSequences = featureMode.equals(Constants.FM_SEQUENCE);

            AnalysisEngineDescription casMultiplier = createEngineDescription(
                    ClassificationUnitCasMultiplier.class,
                    ClassificationUnitCasMultiplier.PARAM_USE_SEQUENCES, useSequences);

            return createEngineDescription(preprocessingPipeline, casMultiplier, xmiWriter);
        }
        return createEngineDescription(preprocessingPipeline, xmiWriter);
    }

    /**
     * @param operativeViews
     */
    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }
}