/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.ml.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.internal.ResourceManagerFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.ml.uima.FeatureResourceLoader;

public abstract class PreTrainedModelProviderAbstract
    extends JCasAnnotator_ImplBase
    implements Constants
{

    public static final String PARAM_TC_MODEL_LOCATION = "tcModel";
    @ConfigurationParameter(name = PARAM_TC_MODEL_LOCATION, mandatory = true)
    protected File tcModelLocation;

    /**
     * This parameter allows to remove the created {@link TextClassificationTarget} annotation after
     * classification in case they are not needed anymore. Default is to keep the annotation.
     */
    public static final String PARAM_RETAIN_TARGETS = "retainTargets";
    @ConfigurationParameter(name = PARAM_RETAIN_TARGETS, mandatory = true, defaultValue = "true")
    protected boolean retainTargets;

    /**
     * Provides the name and parameters of an UIMA conversion annotator. This conversion annotator
     * is supposed to fill the results in the @{link TextClassificationOutcome} into the desired
     * result annotation, e.g. PoS, NER, etc. If a conversion annotator is provided, the
     * {@link TextClassificationOutcome} annotation is deleted after the annotator has been executed
     */
    public static final String PARAM_CONVERTION_ANNOTATOR = "conversionAnnotator";
    @ConfigurationParameter(name = PARAM_CONVERTION_ANNOTATOR, mandatory = false)
    protected  String[] conversionAnnotator;

    protected String learningMode;
    protected String featureMode;

    // private List<FeatureExtractorResource_ImplBase> featureExtractors;

    protected TcShallowLearningAdapter mlAdapter;

    protected AnalysisEngine engine;

    protected int jcasId;
    protected List<ExternalResourceDescription> featureExtractors;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            featureExtractors = new FeatureResourceLoader(tcModelLocation)
                    .loadExternalResourceDescriptionOfFeatures();
            mlAdapter = initMachineLearningAdapter(tcModelLocation);
            featureMode = initFeatureMode(tcModelLocation);
            learningMode = initLearningMode(tcModelLocation);

            validateUimaParameter();

            AnalysisEngineDescription connector = getSaveModelConnector(
                    tcModelLocation.getAbsolutePath(), mlAdapter, learningMode, featureMode,
                    featureExtractors);

            engine = UIMAFramework.produceAnalysisEngine(connector,
                    getModelFeatureAwareResourceManager(tcModelLocation), null);

        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    protected abstract void validateUimaParameter();

	/*
     * Produces a resource manager that is used when creating the engine which is aware of the class
     * files located in the model folder
     */
    protected static ResourceManager getModelFeatureAwareResourceManager(File tcModelLocation)
        throws ResourceInitializationException, MalformedURLException
    {
        // The features of a model are located in a subfolder where Java does
        // not look for them by default. This avoids that during model execution
        // several features with the same name are on the classpath which might
        // cause undefined behavior as it is not know which feature is first
        // found if several with same name exist. We create a new resource
        // manager here and point the manager explicitly to this subfolder where
        // the features to be used are located.
        ResourceManager resourceManager = ResourceManagerFactory.newResourceManager();
        String classpathOfModelFeatures = tcModelLocation.getAbsolutePath() + "/"
                + Constants.MODEL_FEATURE_CLASS_FOLDER;
        resourceManager.setExtensionClassPath(classpathOfModelFeatures, true);
        return resourceManager;
    }

    protected TcShallowLearningAdapter initMachineLearningAdapter(File tcModelLocation)
        throws Exception
    {
        File modelMeta = new File(tcModelLocation, MODEL_META);
        String fileContent = FileUtils.readFileToString(modelMeta, "utf-8");
        Class<?> classObj = Class.forName(fileContent);
        return (TcShallowLearningAdapter) classObj.newInstance();
    }

    protected String initFeatureMode(File tcModelLocation) throws IOException
    {
        File file = new File(tcModelLocation, MODEL_FEATURE_MODE);
        Properties prop = new Properties();

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            prop.load(fis);
        }
        finally {
            IOUtils.closeQuietly(fis);
        }

        return prop.getProperty(DIM_FEATURE_MODE);
    }

    protected String initLearningMode(File tcModelLocation) throws IOException
    {
        File file = new File(tcModelLocation, MODEL_LEARNING_MODE);
        Properties prop = new Properties();

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            prop.load(fis);
        }
        finally {
            IOUtils.closeQuietly(fis);
        }

        return prop.getProperty(DIM_LEARNING_MODE);
    }

    protected AnalysisEngineDescription getSaveModelConnector(String outputPath,
            TcShallowLearningAdapter adapter, String learningMode, String featureMode,
            List<ExternalResourceDescription> featureExtractor)
        throws ResourceInitializationException
    {
        List<Object> parameters = new ArrayList<>();

        // add the rest of the necessary parameters with the correct types
        parameters.addAll(Arrays.asList(PARAM_TC_MODEL_LOCATION, tcModelLocation,
                ModelSerialization_ImplBase.PARAM_OUTPUT_DIRECTORY, outputPath,
                ModelSerialization_ImplBase.PARAM_DATA_WRITER_CLASS,
                adapter.getDataWriterClass(),
                ModelSerialization_ImplBase.PARAM_LEARNING_MODE, learningMode,
                ModelSerialization_ImplBase.PARAM_FEATURE_EXTRACTORS, featureExtractor,
                ModelSerialization_ImplBase.PARAM_FEATURE_FILTERS, null,
                ModelSerialization_ImplBase.PARAM_IS_TESTING, true,
                ModelSerialization_ImplBase.PARAM_USE_SPARSE_FEATURES, adapter.useSparseFeatures(),
                ModelSerialization_ImplBase.PARAM_FEATURE_MODE, featureMode));

        return AnalysisEngineFactory.createEngineDescription(mlAdapter.getLoadModelConnectorClass(),
                parameters.toArray());
    }

    protected void removeTargets(JCas aJCas)
    {
        for (TextClassificationTarget t : JCasUtil.select(aJCas, TextClassificationTarget.class)) {
            t.removeFromIndexes();
        }
    }

    protected void callConversionEngine(JCas aJCas) throws AnalysisEngineProcessException
    {
        String name = conversionAnnotator[0];
        Object[] parameters = new String[0];
        if (conversionAnnotator.length > 1) {
            parameters = new String[conversionAnnotator.length - 1];
            System.arraycopy(conversionAnnotator, 1, parameters, 0, conversionAnnotator.length - 1);
        }
        try {
            @SuppressWarnings("unchecked")
            Class<? extends AnalysisComponent> forName = (Class<? extends AnalysisComponent>) Class
                    .forName(name);
            AnalysisEngine conversionEngine = AnalysisEngineFactory.createEngine(forName,
                    parameters);
            conversionEngine.process(aJCas);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

        for (TextClassificationOutcome o : JCasUtil.select(aJCas,
                TextClassificationOutcome.class)) {
            o.removeFromIndexes();
        }
    }
}
