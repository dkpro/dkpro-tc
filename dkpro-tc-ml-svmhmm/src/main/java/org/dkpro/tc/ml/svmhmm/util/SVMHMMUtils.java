/*
 * Copyright 2017
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
 */

package org.dkpro.tc.ml.svmhmm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualTreeBidiMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dkpro.tc.ml.svmhmm.writer.SVMHMMDataWriter;

public final class SVMHMMUtils
{
    /**
     * File name of serialized mapping from String labels to numbers
     */
    public static final String LABELS_TO_INTEGERS_MAPPING_FILE_NAME = "labelsToIntegersMapping_DualTreeBidiMap.bin";

    /**
     * CSV file comment
     */
    public static final String CSV_COMMENT = "Columns: gold, predicted, token, seqID";

    /**
     * Format of CSV files
     */
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.withCommentMarker('#');

    /**
     * Where the gold outcomes, predicted outcomes, and tokens are stored
     */
    public static final String GOLD_PREDICTED_OUTCOMES_CSV = "outcomesGoldPredicted.csv";

    private SVMHMMUtils()
    {
        // empty
    }

    /**
     * Extract all outcomes from featureVectorsFiles (training, test) that are in LIBSVM format -
     * each line is a feature vector and the first token is the outcome label
     */
    public static SortedSet<String> extractOutcomeLabelsFromFeatureVectorFiles(File... files)
        throws IOException
    {
        SortedSet<String> result = new TreeSet<>();

        for (File file : files) {
            result.addAll(extractOutcomeLabels(file));
        }

        return result;
    }

    /**
     * Maps names to numbers (numbers are required by SVMLight format)
     */
    public static BidiMap mapVocabularyToIntegers(SortedSet<String> names)
    {
        BidiMap result = new DualTreeBidiMap();

        // start numbering from 1
        int index = 1;
        for (String featureName : names) {
            result.put(featureName, index);
            index++;
        }

        return result;
    }

    /**
     * Creates a new file in the same directory as {@code featureVectorsFile} and replaces the first
     * token (outcome label) by its corresponding integer number from the bi-di map
     */
    public static File replaceLabelsWithIntegers(File featureVectorsFile, BidiMap labelsToIntegers)
        throws IOException
    {
        File result = new File(featureVectorsFile.getParent(),
                "mappedLabelsToInt_" + featureVectorsFile.getName());
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(result), "utf-8"));

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(featureVectorsFile), "utf-8"));

        String line = null;
        while ((line = br.readLine()) != null) {
            // split on the first whitespaces, keep the rest
            String[] split = line.split("\\s", 2);
            String label = split[0];
            String remainingContent = split[1];

            // find the integer
            Integer intOutput = (Integer) labelsToIntegers.get(label);

            // print to the output stream
            pw.printf("%d %s%n", intOutput, remainingContent);
        }

        IOUtils.closeQuietly(pw);
        IOUtils.closeQuietly(br);

        return result;
    }

    /**
     * Saves label-integer mapping to a file
     */
    public static void saveMapping(BidiMap mapping, File outputFile)
        throws IOException
    {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                new FileOutputStream(outputFile));
        objectOutputStream.writeObject(mapping);

        IOUtils.closeQuietly(objectOutputStream);
    }

    /**
     * Saves the feature mapping to readable format, each line is a feature id and feature name,
     * sorted by feature id
     */
    public static void saveMappingTextFormat(BidiMap mapping, File outputFile)
        throws IOException
    {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

        // sort values (feature indexes)
        @SuppressWarnings("unchecked")
        SortedSet<Object> featureIndexes = new TreeSet<Object>(mapping.values());

        for (Object featureIndex : featureIndexes) {
            pw.printf(Locale.ENGLISH, "%5d %s%n", (int) featureIndex,
                    mapping.getKey(featureIndex).toString());
        }

        IOUtils.closeQuietly(pw);
    }

    /**
     * Loads a serialized BidiMap from file
     */
    public static BidiMap loadMapping(File inputFile)
        throws IOException
    {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(inputFile));

        try {
            return (BidiMap) inputStream.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Extracts the outcome labels from the file; it corresponds to the first token on each line.
     */
    public static List<String> extractOutcomeLabels(File featureVectorsFile)
        throws IOException
    {
        List<String> result = new ArrayList<>();
        List<String> lines = FileUtils.readLines(featureVectorsFile, "utf-8");
        for (String line : lines) {
            String label = line.split("\\s")[0];

            result.add(label);
        }
        return result;
    }

    /**
     * Reads the featureVectorsFile and splits comment on each line into a list of strings, i.e.
     * "TAG qid:4 1:1 2:1 4:2 # token TAG 4" produces "token", "TAG", "4"
     */
    protected static Iterator<List<String>> extractComments(final File featureVectorsFileStream)
        throws IOException
    {
        return new CommentsIterator(featureVectorsFileStream);

    }

    /**
     * Extracts original tokens that are stored in the comment part of the featureVectorsFile
     */
    public static List<String> extractOriginalTokens(File featureVectorsFile)
        throws IOException
    {
        List<String> result = new ArrayList<>();

        Iterator<List<String>> comments = extractComments(featureVectorsFile);

        try {
            while (comments.hasNext()) {
                List<String> comment = comments.next();
                // original token is the first one in comments
                result.add(comment.get(2));
            }
        }
        catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                    "SvmHmm requires the feature ["
                            + OriginalTextHolderFeatureExtractor.class.getSimpleName()
                            + "] to be in the feature space otherwise this exception might be thrown.",
                    e);
        }
        return result;
    }

    /**
     * Reads the prediction file (each line is a integer) and converts them into original outcome
     * labels using the mapping provided by the bi-directional map
     */
    public static List<String> extractOutcomeLabelsFromPredictions(File predictionsFile,
            BidiMap labelsToIntegersMapping)
                throws IOException
    {
        List<String> result = new ArrayList<>();

        for (String line : FileUtils.readLines(predictionsFile, "utf-8")) {
            Integer intLabel = Integer.valueOf(line);

            String outcomeLabel = (String) labelsToIntegersMapping.getKey(intLabel);

            result.add(outcomeLabel);
        }

        return result;
    }

    /**
     * Returns a list of original sequence IDs extracted from comments
     */
    public static List<Integer> extractOriginalSequenceIDs(File featureVectorsFile)
        throws IOException
    {
        List<Integer> result = new ArrayList<>();

        Iterator<List<String>> comments = extractComments(featureVectorsFile);

        while (comments.hasNext()) {
            List<String> comment = comments.next();
            // sequence number is the third token in the comment token
            result.add(Integer.valueOf(comment.get(1)));
        }

        return result;
    }

    public static List<SortedMap<String, String>> extractMetaDataFeatures(File featureVectorsFile)
        throws IOException
    {
        InputStream inputStream = new FileInputStream(featureVectorsFile);

        List<SortedMap<String, String>> result = new ArrayList<>();

        Iterator<List<String>> allComments = extractComments(featureVectorsFile);

        while (allComments.hasNext()) {
            List<String> instanceComments = allComments.next();

            SortedMap<String, String> instanceResult = new TreeMap<>();

            for (String comment : instanceComments) {
                if (comment.startsWith(SVMHMMDataWriter.META_DATA_FEATURE_PREFIX)) {
                    String[] split = comment.split(":");
                    String key = split[0];
                    String value = split[1];

                    instanceResult.put(key, value);
                }
            }

            result.add(instanceResult);
        }

        IOUtils.closeQuietly(inputStream);

        return result;
    }

    public static double getParameterC(List<String> classificationArguments)
    {
        double p = getDoubleParameter(classificationArguments, "-c");
        if (p == -1) {
            return 1.0;
        }
        
        if(p < 0){
            throw new IllegalArgumentException("Parameter C is < 0");
        }
        
        return p;
    }

    public static double getParameterEpsilon(List<String> classificationArguments)
    {
        double p = getDoubleParameter(classificationArguments, "-e");
        if (p == -1) {
            return 0.5;
        }
        if(p < 0){
            throw new IllegalArgumentException("Epsilon is < 0");
        }
        return p;
    }

    public static int getParameterOrderT_dependencyOfTransitions(
            List<String> classificationArguments)
    {
        int p = getIntegerParameter(classificationArguments, "-t");
        if (p == -1) {
            return 1;
        }
        
        if(p < 0){
            throw new IllegalArgumentException("Parameter order-t is < 0");
        }
        
        return p;
    }

    public static int getParameterOrderE_dependencyOfEmissions(List<String> classificationArguments)
    {
        int p = getIntegerParameter(classificationArguments, "-m");
        if (p == -1) {
            return 0;
        }
        if (p != 0 && p != 1) {
            throw new IllegalArgumentException(
                    "Dependency of emission parameter [-e] has to be either zero (default) or one");
        }
        
        if(p < 0){
            throw new IllegalArgumentException("Parameter order-e is < 0");
        }
        
        return p;
    }
    
    public static int getParameterBeamWidth(List<String> classificationArguments)
    {
        int p = getIntegerParameter(classificationArguments, "-b");
        if (p == -1) {
            return 0;
        }
        if(p < 0){
            throw new IllegalArgumentException("Parameter beam width is < 0");
        }
        return p;
    }

    static int getIntegerParameter(List<String> classificationArguments, String sw)
    {
        for (int i = 0; i < classificationArguments.size(); i++) {
            String e = classificationArguments.get(i);
            if (e.equals(sw)) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found switch [" + sw + "] but no value was specified");
                }
                Integer val = 0;
                String stringVal = classificationArguments.get(i + 1);
                try {
                    val = Integer.valueOf(stringVal);
                }
                catch (Exception ex) {
                    throw new IllegalArgumentException("The provided parameter for [" + sw
                            + "] is not an integer value [" + stringVal + "]");
                }
                return val;
            }
        }

        return -1;
    }

    static double getDoubleParameter(List<String> classificationArguments, String sw)
    {
        for (int i = 0; i < classificationArguments.size(); i++) {
            String e = classificationArguments.get(i);
            if (e.equals(sw)) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found switch [" + sw + "] but no value was specified");
                }
                Double val = 0.0;
                String stringVal = classificationArguments.get(i + 1);
                try {
                    val = Double.valueOf(stringVal);
                }
                catch (Exception ex) {
                    throw new IllegalArgumentException("The provided parameter for [" + sw
                            + "] is not a double value [" + stringVal + "]");
                }
                return val;
            }
        }

        return -1;
    }

}
