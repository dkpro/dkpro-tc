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
package de.tudarmstadt.ukp.dkpro.tc.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.internal.ReflectionUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationFocus;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.InstanceIdFeature;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ExtractFeaturesConnector;

/**
 * Utility methods needed in classification tasks (loading instances, serialization of classifiers
 * etc).
 *
 * @author Oliver Ferschke
 * @author zesch
 */
public class TaskUtils
{
    /**
     * Loads the JSON file as a system resource, parses it and returnd the JSONObject.
     *
     * @param path path to the config file
     * @return the JSONObject containing all config parameters
     * @throws IOException
     */
    public static JSONObject getConfigFromJSON(String path)
            throws IOException
    {
        String jsonPath = FileUtils.readFileToString(new File(ClassLoader.getSystemResource(path)
                .getFile()));
        return (JSONObject) JSONSerializer.toJSON(jsonPath);
    }

    /**
     * Saves a serializable object of type <T> to disk. Output file may be uncompressed, gzipped or
     * bz2-compressed. Compressed files must have a .gz or .bz2 suffix.
     *
     * @param serializedFile     model output file
     * @param serializableObject the object to serialize
     * @throws IOException
     */
    public static void serialize(File serializedFile, Object serializableObject)
            throws IOException
    {

        FileOutputStream fos = new FileOutputStream(serializedFile);
        BufferedOutputStream bufStr = new BufferedOutputStream(fos);

        OutputStream underlyingStream = null;
        if (serializedFile.getName().endsWith(".gz")) {
            underlyingStream = new GZIPOutputStream(bufStr);
        }
        else if (serializedFile.getName().endsWith(".bz2")) {
            underlyingStream = new CBZip2OutputStream(bufStr);
            // manually add bz2 prefix to make it compatible to normal bz2 tools
            // prefix has to be skipped when reading the stream with CBZip2
            fos.write("BZ".getBytes("UTF-8"));
        }
        else {
            underlyingStream = bufStr;
        }
        ObjectOutputStream serializer = new ObjectOutputStream(underlyingStream);
        try {
            serializer.writeObject(serializableObject);

        }
        finally {
            serializer.flush();
            serializer.close();
        }
    }

    /**
     * Loads serialized Object from disk. File can be uncompressed, gzipped or bz2-compressed.
     * Compressed files must have a .gz or .bz2 suffix.
     *
     * @param serializedFile
     * @return the deserialized Object
     * @throws IOException
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> T deserialize(File serializedFile)
            throws IOException
    {
        FileInputStream fis = new FileInputStream(serializedFile);
        BufferedInputStream bufStr = new BufferedInputStream(fis);

        InputStream underlyingStream = null;
        if (serializedFile.getName().endsWith(".gz")) {
            underlyingStream = new GZIPInputStream(bufStr);
        }
        else if (serializedFile.getName().endsWith(".bz2")) {
            // skip bzip2 prefix that we added manually
            fis.read();
            fis.read();
            underlyingStream = new CBZip2InputStream(bufStr);
        }
        else {
            underlyingStream = bufStr;
        }

        ObjectInputStream deserializer = new ObjectInputStream(underlyingStream);

        Object deserializedObject = null;
        try {
            deserializedObject = deserializer.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException("The serialized file was probably corrupted.", e);
        }
        finally {
            deserializer.close();
        }
        return (T) deserializedObject;
    }

    // /**
    // * Instantiates feature extractors from a list of fully qualified class names
    // *
    // * @param extractorNames
    // * a list of fully qualified class names
    // * @return a list of SimpleFeatureExtractor
    // * @throws ClassNotFoundException
    // * @throws IllegalAccessException
    // * @throws InstantiationException
    // */
    // public static List<FeatureExtractor> getExtractorsByName(List<String> extractorNames)
    // throws ClassNotFoundException, IllegalAccessException, InstantiationException
    // {
    // List<FeatureExtractor> extractors = new ArrayList<FeatureExtractor>();
    // for (String name : extractorNames) {
    // extractors.add((FeatureExtractor) Class.forName(name).newInstance());
    // }
    // return extractors;
    // }

    /**
     * Get a list of MetaCollector classes from a list of feature extractors.
     */
    public static Set<Class<? extends MetaCollector>> getMetaCollectorsFromFeatureExtractors(
            List<String> featureSet)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        Set<Class<? extends MetaCollector>> metaCollectorClasses = new HashSet<Class<? extends MetaCollector>>();

        for (String element : featureSet) {
            FeatureExtractorResource_ImplBase featureExtractor = (FeatureExtractorResource_ImplBase) Class
                    .forName(element).newInstance();
            if (featureExtractor instanceof MetaDependent) {
                MetaDependent metaDepFeatureExtractor = (MetaDependent) featureExtractor;
                metaCollectorClasses.addAll(metaDepFeatureExtractor.getMetaCollectorClasses());
            }
        }

        return metaCollectorClasses;
    }

    /**
     * Get a list of required type names.
     */
    public static Set<String> getRequiredTypesFromFeatureExtractors(List<String> featureSet)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        Set<String> requiredTypes = new HashSet<String>();

        for (String element : featureSet) {
            TypeCapability annotation = ReflectionUtil.getAnnotation(Class.forName(element),
                    TypeCapability.class);

            if (annotation != null) {
                requiredTypes.addAll(Arrays.asList(annotation.inputs()));
            }
        }

        return requiredTypes;
    }

    /**
     * @param featureExtractorClassNames @return A fully configured feature extractor connector
     * @throws ResourceInitializationException
     */
    public static AnalysisEngineDescription getFeatureExtractorConnector(List<Object> parameters,
            String outputPath, String dataWriter, String learningMode, String featureMode,
            String featureStore, boolean addInstanceId, boolean developerMode, boolean isTesting,
            String... featureExtractorClassNames)
            throws ResourceInitializationException
    {
    	return getFeatureExtractorConnector(
    			parameters, 
    			outputPath, 
    			dataWriter, 
    			learningMode, 
    			featureMode, 
    			featureStore, 
    			addInstanceId, 
    			developerMode, 
    			isTesting, 
    			Collections.<String>emptyList(), 
    			featureExtractorClassNames
    	);
    }
    
    /**
     * @param featureExtractorClassNames @return A fully configured feature extractor connector
     * @throws ResourceInitializationException
     */
    public static AnalysisEngineDescription getFeatureExtractorConnector(List<Object> parameters,
            String outputPath, String dataWriter, String learningMode, String featureMode,
            String featureStore, boolean addInstanceId, boolean developerMode, boolean isTesting, List<String> filters,
            String... featureExtractorClassNames)
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
        parameters.addAll(Arrays.asList(
        		ExtractFeaturesConnector.PARAM_OUTPUT_DIRECTORY, outputPath, 
                ExtractFeaturesConnector.PARAM_DATA_WRITER_CLASS, dataWriter,
                ExtractFeaturesConnector.PARAM_LEARNING_MODE, learningMode,
                ExtractFeaturesConnector.PARAM_FEATURE_EXTRACTORS, extractorResources,
                ExtractFeaturesConnector.PARAM_FEATURE_FILTERS, filters.toArray(),
                ExtractFeaturesConnector.PARAM_FEATURE_MODE, featureMode,
                ExtractFeaturesConnector.PARAM_ADD_INSTANCE_ID, addInstanceId,
                ExtractFeaturesConnector.PARAM_DEVELOPER_MODE, developerMode,
                ExtractFeaturesConnector.PARAM_IS_TESTING, isTesting,
                ExtractFeaturesConnector.PARAM_FEATURE_STORE_CLASS, featureStore
        ));

        return AnalysisEngineFactory.createEngineDescription(ExtractFeaturesConnector.class,
                parameters.toArray());
    }

    /**
     * @param featureMode
     * @param featureExtractors
     * @param jcas
     * @param developerMode
     * @param addInstanceId
     * @return
     * @throws AnalysisEngineProcessException
     */
    public static Instance getSingleInstance(String featureMode,
            FeatureExtractorResource_ImplBase[] featureExtractors, JCas jcas,
            boolean developerMode, boolean addInstanceId)
            throws AnalysisEngineProcessException
    {

        Instance instance = new Instance();
        
        if (featureMode.equals(Constants.FM_DOCUMENT)) {
            try {
                if (addInstanceId) {
                    instance.addFeature(InstanceIdFeature.retrieve(jcas));
                }
                
                for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                    if (!(featExt instanceof DocumentFeatureExtractor)) {
                        throw new TextClassificationException(
                                "Using non-document FE in document mode: "
                                        + featExt.getResourceName());
                    }
                    instance.setOutcomes(getOutcomes(jcas, null));
                    instance.addFeatures(((DocumentFeatureExtractor) featExt).extract(jcas));
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        else if (featureMode.equals(Constants.FM_PAIR)) {
            try {
                if (addInstanceId) {
                    instance.addFeature(InstanceIdFeature.retrieve(jcas));
                }
                
                for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                    if (!(featExt instanceof PairFeatureExtractor)) {
                        throw new TextClassificationException("Using non-pair FE in pair mode: "
                                + featExt.getResourceName());
                    }
                    JCas view1 = jcas.getView(Constants.PART_ONE);
                    JCas view2 = jcas.getView(Constants.PART_TWO);

                    instance.setOutcomes(getOutcomes(jcas, null));
                    instance.addFeatures(((PairFeatureExtractor) featExt).extract(view1, view2));
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
            catch (CASException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        else if (featureMode.equals(Constants.FM_UNIT)) {
            try {
            	TextClassificationFocus focus = JCasUtil.selectSingle(jcas,
                        TextClassificationFocus.class);
                Collection<TextClassificationUnit> classificationUnits = JCasUtil
                        .selectCovered(jcas, TextClassificationUnit.class, focus);

                if (classificationUnits.size() != 1) {
                    throw new AnalysisEngineProcessException(
                            "There is more than one TextClassificationUnit annotation in the JCas.",
                            null);
                }

                TextClassificationUnit unit = classificationUnits.iterator().next();

                if (addInstanceId) {
                    instance.addFeature(InstanceIdFeature.retrieve(jcas, unit));
                }
                
                for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                    if (!(featExt instanceof ClassificationUnitFeatureExtractor)) {
                        if (featExt instanceof DocumentFeatureExtractor && developerMode) {
                            // we're ok
                        }
                        else {
                            throw new TextClassificationException(
                                    "Using non-unit FE in unit mode: " + featExt.getResourceName());
                        }
                    }
                    
                    instance.setOutcomes(getOutcomes(jcas, unit));
                    instance.addFeatures(((ClassificationUnitFeatureExtractor) featExt).extract(
                            jcas, unit));
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
  
        return instance;
    }

    /**
     * @param featureMode
     * @param featureExtractors
     * @param jcas
     * @param developerMode
     * @param addInstanceId
     * @param sequenceId
     * @return
     * @throws AnalysisEngineProcessException
     */
    public static List<Instance> getMultipleInstances(
            FeatureExtractorResource_ImplBase[] featureExtractors, JCas jcas,
            boolean addInstanceId, int sequenceId)
            throws AnalysisEngineProcessException
    {
        List<Instance> instances = new ArrayList<Instance>();

        TextClassificationFocus focus = JCasUtil.selectSingle(jcas, TextClassificationFocus.class);

        for (TextClassificationUnit unit : JCasUtil.selectCovered(jcas, TextClassificationUnit.class, focus)) {

            Instance instance = new Instance();
                   
            if (addInstanceId) {
                instance.addFeature(InstanceIdFeature.retrieve(jcas, unit, sequenceId));
            }
            
            // execute feature extractors and add features to instance
            try {
                for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                    if (!(featExt instanceof ClassificationUnitFeatureExtractor)) {
                        throw new TextClassificationException(
                                "Using non-unit FE in sequence mode: " + featExt.getResourceName());
                    }
                    instance.addFeatures(((ClassificationUnitFeatureExtractor) featExt).extract(
                            jcas, unit));
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }

            // set and write outcome label(s)
            instance.setOutcomes(getOutcomes(jcas, unit));
            instance.setSequenceId(sequenceId);
            instance.setSequencePosition(unit.getId());

            instances.add(instance);
        }

        return instances;
    }

    public static List<String> getOutcomes(JCas jcas, AnnotationFS unit)
            throws AnalysisEngineProcessException
    {
        Collection<TextClassificationOutcome> outcomes;
        if (unit == null) {
            outcomes = JCasUtil.select(jcas, TextClassificationOutcome.class);
        }
        else {
            outcomes = JCasUtil.selectCovered(jcas, TextClassificationOutcome.class, unit);
        }

        if (outcomes.size() == 0) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "No outcome annotations present in current CAS."));
        }

        List<String> stringOutcomes = new ArrayList<String>();
        for (TextClassificationOutcome outcome : outcomes) {
            stringOutcomes.add(outcome.getOutcome());
        }

        return stringOutcomes;
    }
}