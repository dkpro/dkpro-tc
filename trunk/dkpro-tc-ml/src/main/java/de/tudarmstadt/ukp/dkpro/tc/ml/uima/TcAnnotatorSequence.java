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
package de.tudarmstadt.ukp.dkpro.tc.ml.uima;

import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_FEATURE_EXTRACTORS;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_META;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_PARAMETERS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.DenseFeatureStore;

public class TcAnnotatorSequence
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_TC_MODEL_LOCATION = "tcModel";
    @ConfigurationParameter(name = PARAM_TC_MODEL_LOCATION, mandatory = true)
    protected File tcModelLocation;

    public static final String PARAM_NAME_SEQUENCE_ANNOTATION = "sequenceAnnotation";
    @ConfigurationParameter(name = PARAM_NAME_SEQUENCE_ANNOTATION, mandatory = true)
    private String nameSequence;

    public static final String PARAM_NAME_UNIT_ANNOTATION = "unitAnnotation";
    @ConfigurationParameter(name = PARAM_NAME_UNIT_ANNOTATION, mandatory = true)
    private String nameUnit;

    private String learningMode = Constants.LM_SINGLE_LABEL;
    private String featureMode = Constants.FM_SEQUENCE;

    // private List<FeatureExtractorResource_ImplBase> featureExtractors;
    private List<String> featureExtractors;
    private List<Object> parameters;

    private TCMachineLearningAdapter mlAdapter;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            mlAdapter = (TCMachineLearningAdapter) Class.forName(
                    FileUtils.readFileToString(new File(tcModelLocation, MODEL_META)))
                    .newInstance();
        }
        catch (InstantiationException e) {
            throw new ResourceInitializationException(e);
        }
        catch (IllegalAccessException e) {
            throw new ResourceInitializationException(e);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        parameters = new ArrayList<>();
        try {
            for (String parameter : FileUtils
                    .readLines(new File(tcModelLocation, MODEL_PARAMETERS))) {
                if (!parameter.startsWith("#")) {
                    String[] parts = parameter.split("=");
                    parameters.add(parts[0]);
                    parameters.add(parts[1]);
                }
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        featureExtractors = new ArrayList<>();
        try {
            for (String featureExtractor : FileUtils.readLines(new File(tcModelLocation,
                    MODEL_FEATURE_EXTRACTORS))) {
                featureExtractors.add(featureExtractor);
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        // featureExtractors = new ArrayList<>();
        // try {
        // for (String featureExtractor : FileUtils.readLines(new File(tcModelLocation,
        // "features.txt"))) {
        // featureExtractors.add(
        // (FeatureExtractorResource_ImplBase) Class.forName(featureExtractor).newInstance()
        // );
        // }
        // } catch (InstantiationException e) {
        // throw new ResourceInitializationException(e);
        // } catch (IllegalAccessException e) {
        // throw new ResourceInitializationException(e);
        // } catch (ClassNotFoundException e) {
        // throw new ResourceInitializationException(e);
        // } catch (IOException e) {
        // throw new ResourceInitializationException(e);
        // }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {

        addTCSequenceAnnotation(jcas);
        addTCUnitAndOutcomeAnnotation(jcas);

        // create new UIMA annotator in order to separate the parameter spaces
        // this annotator will get initialized with its own set of parameters loaded from the model
        try {
            AnalysisEngineDescription connector = getSaveModelConnector(parameters,
                    tcModelLocation.getAbsolutePath(), mlAdapter.getDataWriterClass(learningMode)
                            .toString(), learningMode, featureMode,
                    DenseFeatureStore.class.getName(), featureExtractors.toArray(new String[0]));
            AnalysisEngine engine = AnalysisEngineFactory.createEngine(connector);

            // process and classify
            engine.process(jcas);
        }
        catch (ResourceInitializationException e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private void addTCUnitAndOutcomeAnnotation(JCas jcas)
    {
        Type type = jcas.getCas().getTypeSystem().getType(nameUnit);

        Collection<AnnotationFS> unitAnnotation = CasUtil.select(jcas.getCas(), type);
        for (AnnotationFS unit : unitAnnotation) {
            TextClassificationUnit tcs = new TextClassificationUnit(jcas, unit.getBegin(),
                    unit.getEnd());
            tcs.addToIndexes();
            TextClassificationOutcome tco = new TextClassificationOutcome(jcas, unit.getBegin(),
                    unit.getEnd());
            tco.setOutcome("dummyValue");
            tco.addToIndexes();
        }
    }

    private void addTCSequenceAnnotation(JCas jcas)
    {
        Type type = jcas.getCas().getTypeSystem().getType(nameSequence);

        Collection<AnnotationFS> sequenceAnnotation = CasUtil.select(jcas.getCas(), type);
        for (AnnotationFS seq : sequenceAnnotation) {
            TextClassificationSequence tcs = new TextClassificationSequence(jcas, seq.getBegin(),
                    seq.getEnd());
            tcs.addToIndexes();
        }
    }

    /**
     * @param featureExtractorClassNames
     *            @return A fully configured feature extractor connector
     * @throws ResourceInitializationException
     */
    private AnalysisEngineDescription getSaveModelConnector(List<Object> parameters,
            String outputPath, String dataWriter, String learningMode, String featureMode,
            String featureStore, String... featureExtractorClassNames)
        throws ResourceInitializationException
    {
        // convert parameters to string as external resources only take string parameters
        List<Object> convertedParameters = new ArrayList<Object>();
        if (parameters != null) {
            for (Object parameter : parameters) {
                convertedParameters.add(parameter.toString());
            }
        }
        else {
            parameters = new ArrayList<Object>();
        }

        List<ExternalResourceDescription> extractorResources = new ArrayList<ExternalResourceDescription>();
        for (String featureExtractor : featureExtractorClassNames) {
            try {
                extractorResources.add(ExternalResourceFactory.createExternalResourceDescription(
                        Class.forName(featureExtractor).asSubclass(Resource.class),
                        convertedParameters.toArray()));
            }
            catch (ClassNotFoundException e) {
                throw new ResourceInitializationException(e);
            }
        }

        // add the rest of the necessary parameters with the correct types
        parameters.addAll(Arrays.asList(TcAnnotatorSequence.PARAM_TC_MODEL_LOCATION,
                tcModelLocation, ModelSerialization_ImplBase.PARAM_OUTPUT_DIRECTORY, outputPath,
                ModelSerialization_ImplBase.PARAM_DATA_WRITER_CLASS, dataWriter,
                ModelSerialization_ImplBase.PARAM_LEARNING_MODE, learningMode,
                ModelSerialization_ImplBase.PARAM_FEATURE_EXTRACTORS, extractorResources,
                ModelSerialization_ImplBase.PARAM_FEATURE_FILTERS, null,
                ModelSerialization_ImplBase.PARAM_IS_TESTING, true,
                ModelSerialization_ImplBase.PARAM_FEATURE_MODE, featureMode,
                ModelSerialization_ImplBase.PARAM_FEATURE_STORE_CLASS, featureStore));

        return AnalysisEngineFactory.createEngineDescription(
                mlAdapter.getLoadModelConnectorClass(), parameters.toArray());
    }

}
