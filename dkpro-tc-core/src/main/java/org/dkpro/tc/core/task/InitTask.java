/*******************************************************************************
 * Copyright 2016
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
import static org.dkpro.tc.core.Constants.DIM_BIPARTITION_THRESHOLD;
import static org.dkpro.tc.core.Constants.DIM_DEVELOPER_MODE;
import static org.dkpro.tc.core.Constants.DIM_FEATURE_MODE;
import static org.dkpro.tc.core.Constants.DIM_FEATURE_SET;
import static org.dkpro.tc.core.Constants.DIM_LEARNING_MODE;
import static org.dkpro.tc.core.Constants.DIM_PIPELINE_PARAMS;
import static org.dkpro.tc.core.Constants.DIM_READER_TEST;
import static org.dkpro.tc.core.Constants.DIM_READER_TRAIN;
import static org.dkpro.tc.core.Constants.FM_PAIR;
import static org.dkpro.tc.core.Constants.PART_ONE;
import static org.dkpro.tc.core.Constants.PART_TWO;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.CustomResourceSpecifier;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminable;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.uima.task.impl.UimaTaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.io.ReaderInvocationHandler;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.task.uima.AssignIdConnector;
import org.dkpro.tc.core.task.uima.DocumentModeAnnotator;
import org.dkpro.tc.core.task.uima.PreprocessConnector;
import org.dkpro.tc.core.task.uima.ValidityCheckConnector;
import org.dkpro.tc.core.task.uima.ValidityCheckConnectorPost;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;

/**
 * Initialization of the TC pipeline 1) checks the validity of the setup 2) runs the preprocessing
 * 3) runs the outcome/unit annotator 4) runs additional validity checks that check the outcome/unit
 * setup
 * 
 */
public class InitTask
    extends UimaTaskBase
{

    @Discriminator(name = DIM_READER_TRAIN)
    protected CollectionReaderDescription readerTrain;
    @Discriminator(name = DIM_READER_TEST)
    protected CollectionReaderDescription readerTest;
    @Discriminator(name = DIM_LEARNING_MODE)
    private String learningMode;
    @Discriminator(name = DIM_FEATURE_MODE)
    private String featureMode;
    @Discriminator(name = DIM_BIPARTITION_THRESHOLD)
    private String threshold;
    @Discriminator(name = "ABC")
    private List<DynamicDiscriminableFunctionBase<ExternalResourceDescription>> featureExtractors;
    @Discriminator(name = DIM_DEVELOPER_MODE)
    protected boolean developerMode;

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

            readerDesc = createDiscriminableReader(readerTrain);
        }
        else {
            if (readerTest == null) {
                throw new ResourceInitializationException(
                        new IllegalStateException("readerTest is null"));
            }

            readerDesc = createDiscriminableReader(readerTest);
        }

        return readerDesc;
    }

    private CollectionReaderDescription createDiscriminableReader(
            CollectionReaderDescription reader)
    {
        // use a dynamic proxy to inject a Discriminable interface
        return (CollectionReaderDescription) Proxy.newProxyInstance(
                reader.getClass().getClassLoader(),
                new Class<?>[] { CollectionReaderDescription.class, Discriminable.class },
                new ReaderInvocationHandler(reader));
    }

    // what should actually be done in this task
    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        String output = isTesting ? OUTPUT_KEY_TEST : OUTPUT_KEY_TRAIN;
        AnalysisEngineDescription xmiWriter = createEngineDescription(BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION,
                aContext.getFolder(output, AccessMode.READWRITE).getPath(),
                BinaryCasWriter.PARAM_FORMAT, "6+");

        // special connector that just checks whether there are no instances and outputs a
        // meaningful error message then
        // should be added before preprocessing
        AnalysisEngineDescription emptyProblemChecker = AnalysisEngineFactory
                .createEngineDescription(PreprocessConnector.class);

        // check whether we are dealing with pair classification and if so, add PART_ONE and
        // PART_TWO views
        if (featureMode.equals(FM_PAIR)) {
            AggregateBuilder builder = new AggregateBuilder();
            builder.add(createEngineDescription(preprocessing), CAS.NAME_DEFAULT_SOFA, PART_ONE);
            builder.add(createEngineDescription(preprocessing), CAS.NAME_DEFAULT_SOFA, PART_TWO);
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

        return createEngineDescription(
                createEngineDescription(DocumentModeAnnotator.class,
                        DocumentModeAnnotator.PARAM_FEATURE_MODE, featureMode),
                // assign each CAS an unique id
                createEngineDescription(AssignIdConnector.class),

        // tc pre validity check
                getPreValidityCheckEngine(aContext), emptyProblemChecker,

        // user preprocessing
                preprocessing,

        // tc post validity check
                getPostValidityCheckEngine(aContext),

        // write CAS to HDD
                xmiWriter);
    }

    private AnalysisEngineDescription getPreValidityCheckEngine(TaskContext aContext)
        throws ResourceInitializationException
    {
        // check mandatory dimensions
        if (featureExtractors == null) {
            throw new ResourceInitializationException(new TextClassificationException(
                    "No feature extractors have been added to the experiment."));
        }

        List<Object> parameters = new ArrayList<Object>();

        parameters.add(ValidityCheckConnector.PARAM_LEARNING_MODE);
        parameters.add(learningMode);
        parameters.add(ValidityCheckConnector.PARAM_DATA_WRITER_CLASS);
        parameters.add(mlAdapter.getDataWriterClass().getName());
        parameters.add(ValidityCheckConnector.PARAM_FEATURE_MODE);
        parameters.add(featureMode);
        parameters.add(ValidityCheckConnector.PARAM_BIPARTITION_THRESHOLD);
        parameters.add(threshold);
        parameters.add(ValidityCheckConnector.PARAM_FEATURE_EXTRACTORS);
        parameters.add(getFeatureExtractorNames(featureExtractors));
        parameters.add(ValidityCheckConnector.PARAM_DEVELOPER_MODE);
        parameters.add(developerMode);

        return createEngineDescription(ValidityCheckConnector.class, parameters.toArray());
    }

    private Object getFeatureExtractorNames(
            List<DynamicDiscriminableFunctionBase<ExternalResourceDescription>> featureExtractors2)
    {
        String[] featureExtractorNames = new String[featureExtractors.size()];

        for (int i = 0; i < featureExtractorNames.length; i++) {

            String implName;
            if (featureExtractors.get(i).getActualValue()
                    .getResourceSpecifier() instanceof CustomResourceSpecifier) {
                implName = ((CustomResourceSpecifier) featureExtractors.get(i).getActualValue()
                        .getResourceSpecifier()).getResourceClassName();
            }
            else {
                implName = featureExtractors.get(i).getActualValue().getImplementationName();
            }
            featureExtractorNames[i] = (implName);
        }
        return featureExtractorNames;
    }

    private AnalysisEngineDescription getPostValidityCheckEngine(TaskContext aContext)
        throws ResourceInitializationException
    {
        List<Object> parameters = new ArrayList<Object>();

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

    public void setMlAdapter(TCMachineLearningAdapter mlAdapter)
    {
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

}