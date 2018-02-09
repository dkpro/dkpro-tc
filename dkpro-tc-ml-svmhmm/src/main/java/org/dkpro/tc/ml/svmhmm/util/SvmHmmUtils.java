/*
 * Copyright 2018
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

public final class SvmHmmUtils
{
    /**
     * File name of serialized mapping from String labels to numbers
     */
    public static final String LABELS_TO_INTEGERS_MAPPING_FILE_NAME = "labelsToIntegersMapping_DualTreeBidiMap.bin";

    private SvmHmmUtils()
    {
        // empty
    }

    /**
     * Extract all outcomes from featureVectorsFiles (training, test) that are in LIBSVM format -
     * each line is a feature vector and the first token is the outcome label
     * 
     * @param files
     * 			data files
     * @return	sorted set of strings
     * @throws IOException
     * 			in case of errors
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
     * Extracts the outcome labels from the file; it corresponds to the first token on each line.
     * @param featureVectorsFile
     * 			the feature file
     * @return list of strings
     * @throws IOException
     * 			in case of errors
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
