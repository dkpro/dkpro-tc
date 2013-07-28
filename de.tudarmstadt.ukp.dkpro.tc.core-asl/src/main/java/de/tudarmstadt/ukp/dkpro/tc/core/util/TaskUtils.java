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
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MetaDependent;

/**
 * Utility methods needed in classification tasks (loading instances, serialization of classifiers
 * etc).
 * 
 * @author Oliver Ferschke
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
     * 
     * @param featureSet
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static List<Class<? extends MetaCollector>> getMetaCollectorsFromFeatures(String[] featureSet)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        
        for (String element : featureSet) {
            FeatureExtractorResource_ImplBase featureExtractor = (FeatureExtractorResource_ImplBase) Class.forName(element).newInstance();
            if (featureExtractor instanceof MetaDependent) {
                MetaDependent metaDepFeatureExtractor = (MetaDependent) featureExtractor;
                metaCollectorClasses.addAll(metaDepFeatureExtractor.getMetaCollectorClasses());
            }
        }
        
        return metaCollectorClasses;

    }
}