/*******************************************************************************
 * Copyright 2015
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
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.FlowControllerFactory;
import org.apache.uima.flow.FlowControllerDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.ClassificationUnitCasMultiplier;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.CasDropFlowController;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ConnectorBase;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.PreprocessConnector;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ValidityCheckConnector;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ValidityCheckConnectorPost;

/**
 * Initialization of the TC pipeline
 * 1) checks the validity of the setup
 * 2) runs the preprocessing
 * 3) runs the outcome/unit annotator
 * 4) runs additional validity checks that check the outcome/unit setup
 * 
 */
public class InitTask
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
    protected List<String> featureSet;
    @Discriminator
    protected boolean developerMode;

    private boolean isTesting = false;
    
    private boolean dropInvalidCases = false;

	private AnalysisEngineDescription preprocessing;

    /**
     * Public name of the folder under which the preprocessed training data file will be stored within the task
     */
    public static final String OUTPUT_KEY_TRAIN = "preprocessorOutputTrain";
    /**
     * Public name of the folder under which the preprocessed test data file will be stored within the task
     */
    public static final String OUTPUT_KEY_TEST = "preprocessorOutputTest";

    private List<String> operativeViews;
    
    private TCMachineLearningAdapter mlAdapter;
	
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
    
    // what should actually be done in this task
    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
	    FlowControllerDescription flowController = FlowControllerFactory.createFlowControllerDescription(
        		CasDropFlowController.class,
        		ConnectorBase.PARAM_FEATURE_MODE, featureMode,
        		ConnectorBase.PARAM_LEARNING_MODE, learningMode
	    );
	    
        String output = isTesting ? OUTPUT_KEY_TEST : OUTPUT_KEY_TRAIN;
        AnalysisEngineDescription xmiWriter = createEngineDescription(BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION,
                aContext.getFolder(output, AccessMode.READWRITE).getPath(),
                BinaryCasWriter.PARAM_FORMAT, "6+");
	    
        // special connector that just checks whether there are no instances and outputs a meaningful error message then
        // should be added before preprocessing
        AnalysisEngineDescription emptyProblemChecker = AnalysisEngineFactory.createEngineDescription(PreprocessConnector.class);

        // check whether we are dealing with pair classification and if so, add PART_ONE and
        // PART_TWO views
        if (featureMode.equals(Constants.FM_PAIR)) {
            AggregateBuilder builder = new AggregateBuilder();
            builder.add(createEngineDescription(preprocessing), CAS.NAME_DEFAULT_SOFA,
                    Constants.PART_ONE);
            builder.add(createEngineDescription(preprocessing), CAS.NAME_DEFAULT_SOFA,
                    Constants.PART_TWO);
            preprocessing = builder.createAggregateDescription();
        }
        else if (operativeViews != null) {
            AggregateBuilder builder = new AggregateBuilder();
            for (String viewName : operativeViews) {
                builder.add(createEngineDescription(preprocessing), CAS.NAME_DEFAULT_SOFA,
                        viewName);
            }
            preprocessing = builder.createAggregateDescription();
        }
        // in unit or sequence mode, add cas multiplier
        else if (featureMode.equals(Constants.FM_UNIT) || featureMode.equals(Constants.FM_SEQUENCE)) {
            boolean useSequences = featureMode.equals(Constants.FM_SEQUENCE);

            AnalysisEngineDescription casMultiplier = createEngineDescription(
                    ClassificationUnitCasMultiplier.class,
                    ClassificationUnitCasMultiplier.PARAM_USE_SEQUENCES, useSequences);

            if (dropInvalidCases) {
                return createEngineDescription(flowController, getPreValidityCheckEngine(aContext), emptyProblemChecker, preprocessing, casMultiplier, getPostValidityCheckEngine(aContext), xmiWriter);
            }
            else {
                return createEngineDescription(getPreValidityCheckEngine(aContext), emptyProblemChecker, preprocessing, casMultiplier, getPostValidityCheckEngine(aContext), xmiWriter);
            }            	
        }
        
        if (dropInvalidCases) {
            return createEngineDescription(flowController, getPreValidityCheckEngine(aContext), emptyProblemChecker, preprocessing, getPostValidityCheckEngine(aContext), xmiWriter);
        }
        else {
            return createEngineDescription(getPreValidityCheckEngine(aContext), emptyProblemChecker, preprocessing, getPostValidityCheckEngine(aContext), xmiWriter);
        }        	
    }
    
    private AnalysisEngineDescription getPreValidityCheckEngine(TaskContext aContext)
    		throws ResourceInitializationException
    {
        // check mandatory dimensions
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
        parameters.add(mlAdapter.getDataWriterClass().getName());
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
    
    private AnalysisEngineDescription getPostValidityCheckEngine(TaskContext aContext)
    		throws ResourceInitializationException
    {
        List<Object> parameters = new ArrayList<Object>();
        if (pipelineParameters != null) {
            parameters.addAll(pipelineParameters);
        }

        parameters.add(ValidityCheckConnector.PARAM_LEARNING_MODE);
        parameters.add(learningMode);
        parameters.add(ValidityCheckConnector.PARAM_FEATURE_MODE);
        parameters.add(featureMode);
        parameters.add(ValidityCheckConnector.PARAM_DEVELOPER_MODE);
        parameters.add(developerMode);

        return createEngineDescription(ValidityCheckConnectorPost.class, parameters.toArray());
    }
    
    public void setTesting(boolean isTesting)
    {
        this.isTesting = isTesting;
    }

	public void setMlAdapter(TCMachineLearningAdapter mlAdapter) {
		this.mlAdapter = mlAdapter;
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

	public void setDropInvalidCases(boolean dropInvalidCases) {
		this.dropInvalidCases = dropInvalidCases;
	}
}