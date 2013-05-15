package de.tudarmstadt.ukp.dkpro.tc.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;

import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.MLUtils;
import weka.core.Result;
import de.tudarmstadt.ukp.dkpro.tc.core.evaluation.MekaEvaluationUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.meta.AddIdFeatureExtractor;

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
     * Read instances from uncompressed or compressed arff files. Compression is determined by
     * filename suffix. For bz2 files, it is expected that the first two bytes mark the compression
     * types (BZ) - thus, the first bytes of the stream are skipped.
     * 
     * @param instancesFile
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Instances getInstances(File instancesFile, boolean multiLabel)
        throws FileNotFoundException, IOException
    {
        FileInputStream fis = new FileInputStream(instancesFile);
        BufferedInputStream bufStr = new BufferedInputStream(fis);

        InputStream underlyingStream = null;
        if (instancesFile.getName().endsWith(".gz")) {
            underlyingStream = new GZIPInputStream(bufStr);
        }
        else if (instancesFile.getName().endsWith(".bz2")) {
            // skip bzip2 prefix that we added manually
            fis.read();
            fis.read();
            underlyingStream = new CBZip2InputStream(bufStr);
        }
        else {
            underlyingStream = bufStr;
        }

        Reader reader = new InputStreamReader(underlyingStream);
        Instances trainData = new Instances(reader);

        if (multiLabel) {
            String relationTag = trainData.relationName();
            // for multi-label classification, class labels are expected at beginning of attribute
            // set and their number must be specified with the -C parameter in the relation tag
            trainData.setClassIndex(Integer.parseInt(relationTag
                    .substring(relationTag.indexOf("C")).split(" ")[1]));
        }
        else {
            // for single-label classification, class label expected as last attribute
            trainData.setClassIndex(trainData.numAttributes() - 1);
        }
        reader.close();
        return trainData;
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

    /**
     * Instantiates feature extractors from a list of fully qualified class names
     * 
     * @param extractorNames
     *            a list of fully qualified class names
     * @return a list of SimpleFeatureExtractor
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static List<SimpleFeatureExtractor> getExtractorsByName(List<String> extractorNames)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        List<SimpleFeatureExtractor> extractors = new ArrayList<SimpleFeatureExtractor>();
        for (String name : extractorNames) {
            extractors.add((SimpleFeatureExtractor) Class.forName(name).newInstance());
        }
        return extractors;
    }

    public static double[] getMekaThreshold(String threshold, Result r, Instances data)
        throws Exception
    {
        double[] t = new double[r.L];
        if (threshold.equals("PCut1")) {
            // one threshold for all labels (PCut1 in Meka)
            Arrays.fill(
                    t,
                    MekaEvaluationUtils.calibrateThreshold(r.predictions,
                            Double.valueOf(r.getValue("LCard_train"))));
        }
        else if (threshold.equals("PCutL")) {
            // one threshold for each label (PCutL in Meka)
            t = MekaEvaluationUtils.calibrateThresholds(r.predictions,
                    MLUtils.labelCardinalities(data));
            // FIXME
            throw new Exception("Not yet implemented.");
        }
        else {
            // manual threshold
            Arrays.fill(t, Double.valueOf(threshold));
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    public static int getInstanceIdAttributeOffset(Instances data)
    {
        int attOffset = 1;
        Enumeration<Attribute> enumeration = data.enumerateAttributes();
        while (enumeration.hasMoreElements()) {
            Attribute att = enumeration.nextElement();
//            System.out.println(att.name());
            if (att.name().equals(AddIdFeatureExtractor.ID_FEATURE_NAME)) {
                return attOffset;
            }
            attOffset++;
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getClassLabels(Evaluation eval)
    {
        Enumeration<String> classLabels = eval.getHeader().attribute(eval.getHeader().classIndex())
                .enumerateValues();
        List<String> classLabelList = new ArrayList<String>();
        while (classLabels.hasMoreElements()) {
            classLabelList.add(classLabels.nextElement());
        }
        return classLabelList;
    }
}