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
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ValidityCheckConnector;

/**
 * Checks that everything has been configured properly and throws more meaningful exception
 * otherwise than would have been thrown downstream. This should be the first task in the TC
 * pipeline.
 * 
 * @author zesch
 * 
 */
public class ValidityCheckTask
    extends UimaTaskBase
{

    @Discriminator
    protected Class<? extends CollectionReader> readerTrain;
    @Discriminator
    protected Class<? extends CollectionReader> readerTest;
    @Discriminator
    protected List<Object> readerTrainParams;
    @Discriminator
    protected List<Object> readerTestParams;
    @Discriminator
    protected List<Object> pipelineParameters;
    @Discriminator
    private String learningMode;
    @Discriminator
    private String featureMode;
    @Discriminator
    private String threshold;
    @Discriminator
    private String dataWriter;
    @Discriminator
    protected List<String> featureSet;
    @Discriminator
    protected boolean developerMode;

    private boolean isTesting = false;

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

            readerDesc = createReaderDescription(readerTrain,
                    readerTrainParams.toArray());
        }
        else {
            if (readerTest == null) {
                throw new ResourceInitializationException(
                        new IllegalStateException("readerTest is null"));
            }

            readerDesc = createReaderDescription(readerTest,
                    readerTestParams.toArray());
        }

        return readerDesc;
    }

    /**
     * Set testing mode.
     * 
     * @param isTesting
     *            true if testing mode should be active
     */
    public void setTesting(boolean isTesting)
    {
        this.isTesting = isTesting;
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // check mandatory dimensions
        if (dataWriter == null) {
            throw new ResourceInitializationException("You need to set the dataWriter dimension.",
                    null);
        }

        if (featureSet == null) {
            throw new ResourceInitializationException(new TextClassificationException(
                    "No feature extractors have been added to the experiment."));
        }

        List<Object> parameters = new ArrayList<Object>();
        if (pipelineParameters != null) {
            parameters.addAll(pipelineParameters);
        }

        parameters.add(ValidityCheckConnector.PARAM_LEARNING_MODE);
        parameters.add(learningMode);
        parameters.add(ValidityCheckConnector.PARAM_DATA_WRITER_CLASS);
        parameters.add(dataWriter);
        parameters.add(ValidityCheckConnector.PARAM_FEATURE_MODE);
        parameters.add(featureMode);
        parameters.add(ValidityCheckConnector.PARAM_BIPARTITION_THRESHOLD);
        parameters.add(threshold);
        parameters.add(ValidityCheckConnector.PARAM_FEATURE_EXTRACTORS);
        parameters.add(featureSet);
        parameters.add(ValidityCheckConnector.PARAM_DEVELOPER_MODE);
        parameters.add(developerMode);

        return createEngineDescription(ValidityCheckConnector.class, parameters.toArray());
    }
}