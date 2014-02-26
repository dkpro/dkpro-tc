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
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.internal.ReflectionUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ExtractFeaturesConnector;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * Utility methods needed in classification tasks (loading instances, serialization of classifiers
 * etc).
 * 
 * @author Oliver Ferschke
 * @author zesch
 * 
 */
public class TaskUtils
{
    /**
     * Loads the JSON file as a system resource, parses it and returnd the JSONObject.
     * 
     * @param path
     *            path to the config file
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
     * @param serializedFile
     *            model output file
     * @param serializableObject
     *            the object to serialize
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
            fos.write("BZ".getBytes());
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

    public static AnalysisEngineDescription getFeatureExtractorConnector(List<Object> parameters,
            String outputPath, String dataWriter, String learningMode, String featureMode,
            boolean addInstanceId, String... featureExtractorClassNames)
        throws ResourceInitializationException
    {
        // convert parameters to string as external resources only take string parameters
        List<Object> convertedParameters = new ArrayList<Object>();
        if (parameters != null) {
            for (Object parameter : parameters) {
                convertedParameters.add(parameter.toString());
            }
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
        parameters.addAll(Arrays.asList(ExtractFeaturesConnector.PARAM_OUTPUT_DIRECTORY,
                outputPath, ExtractFeaturesConnector.PARAM_DATA_WRITER_CLASS, dataWriter,
                ExtractFeaturesConnector.PARAM_LEARNING_MODE, learningMode,
                ExtractFeaturesConnector.PARAM_FEATURE_EXTRACTORS, extractorResources,
                ExtractFeaturesConnector.PARAM_FEATURE_MODE, featureMode,
                ExtractFeaturesConnector.PARAM_ADD_INSTANCE_ID, addInstanceId));

        return AnalysisEngineFactory.createEngineDescription(ExtractFeaturesConnector.class,
                parameters.toArray());
    }

    /**
     * @param jcas
     * @param featureExtractors
     * @param featureMode
     * @return
     * @throws AnalysisEngineProcessException
     */
    public static Instance extractFeatures(JCas jcas,
            FeatureExtractorResource_ImplBase[] featureExtractors, String featureMode)
        throws AnalysisEngineProcessException
    {
        Instance instance = new Instance();
        for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
            try {
                if (featExt instanceof DocumentFeatureExtractor
                        && featureMode.equals(Constants.FM_DOCUMENT)) {
                    instance.addFeatures(((DocumentFeatureExtractor) featExt).extract(jcas));
                }
                else if (featExt instanceof PairFeatureExtractor
                        && featureMode.equals(Constants.FM_PAIR)) {
                    JCas view1 = jcas.getView(Constants.PART_ONE);
                    JCas view2 = jcas.getView(Constants.PART_TWO);
                    instance.addFeatures(((PairFeatureExtractor) featExt).extract(view1, view2));
                }
                else if (featExt instanceof ClassificationUnitFeatureExtractor
                        && featureMode.equals(Constants.FM_UNIT)) {
                    TextClassificationUnit classificationUnit = null;
                    Collection<TextClassificationUnit> classificationUnits = JCasUtil.select(jcas,
                            TextClassificationUnit.class);
                    if (classificationUnits.size() == 1) {
                        classificationUnit = classificationUnits.iterator().next();
                    }
                    else if (classificationUnits.size() > 1) {
                        throw new AnalysisEngineProcessException(
                                "There are more than one TextClassificationUnit anotations in the JCas.",
                                null);
                    }
                    instance.addFeatures(((ClassificationUnitFeatureExtractor) featExt).extract(
                            jcas, classificationUnit));
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
            catch (CASException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        return instance;
    }
}