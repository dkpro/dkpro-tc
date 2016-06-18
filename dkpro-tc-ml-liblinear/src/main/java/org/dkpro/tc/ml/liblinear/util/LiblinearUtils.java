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
package org.dkpro.tc.ml.liblinear.util;

import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.EPISILON_DEFAULT;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.PARAM_C_DEFAULT;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_L1R_L2LOSS_SVC;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_L1R_LR;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_L2R_L1LOSS_SVC_DUAL;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_L2R_L1LOSS_SVR_DUAL;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_L2R_L2LOSS_SVC;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_L2R_L2LOSS_SVC_DUAL;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_L2R_L2LOSS_SVR;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_L2R_L2LOSS_SVR_DUAL;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_L2R_LR;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_L2R_LR_DUAL;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.SOLVER_MCSVM_CS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;

import de.bwaldvogel.liblinear.SolverType;

public class LiblinearUtils
{
    public static SolverType getSolver(List<String> classificationArguments)
    {
        if (classificationArguments == null) {
            return SolverType.L2R_LR;
        }

        SolverType type = null;
        for (int i = 0; i < classificationArguments.size(); i++) {
            String e = classificationArguments.get(i);
            if (e.equals("-s")) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found parameter [-s] but no solver type was specified");
                }

                String algo = classificationArguments.get(i + 1);
                switch (algo) {
                case SOLVER_L2R_LR:
                    type = SolverType.L2R_LR;
                    break;
                case SOLVER_L2R_L1LOSS_SVC_DUAL:
                    type = SolverType.L2R_L1LOSS_SVC_DUAL;
                    break;
                case SOLVER_L2R_L2LOSS_SVC_DUAL:
                    type = SolverType.L2R_L2LOSS_SVC_DUAL;
                    break;
                case SOLVER_L2R_L2LOSS_SVC:
                    type = SolverType.L2R_L2LOSS_SVC;
                    break;
                case SOLVER_MCSVM_CS:
                    type = SolverType.MCSVM_CS;
                    break;
                case SOLVER_L1R_L2LOSS_SVC:
                    type = SolverType.L1R_L2LOSS_SVC;
                    break;
                case SOLVER_L1R_LR:
                    type = SolverType.L1R_LR;
                    break;
                case SOLVER_L2R_LR_DUAL:
                    type = SolverType.L2R_LR_DUAL;
                    break;
                case SOLVER_L2R_L2LOSS_SVR:
                    type = SolverType.L2R_L2LOSS_SVR;
                    break;
                case SOLVER_L2R_L2LOSS_SVR_DUAL:
                    type = SolverType.L2R_L2LOSS_SVR_DUAL;
                    break;
                case SOLVER_L2R_L1LOSS_SVR_DUAL:
                    type = SolverType.L2R_L1LOSS_SVR_DUAL;
                    break;
                default:
                    throw new IllegalArgumentException("An unknown solver was specified [" + algo
                            + "] which is unknown i.e. check parameter [-s] in your configuration");
                }

            }
        }

        if (type == null) {
            // parameter -s was not specified in the parameters so we set a default value
            type = SolverType.L2R_LR;
        }

        LogFactory.getLog(LiblinearUtils.class).info("Will use solver " + type.toString() + ")");
        return type;
    }

    public static double getParameterC(List<String> classificationArguments)
    {
        if (classificationArguments == null) {
            return PARAM_C_DEFAULT;
        }

        for (int i = 0; i < classificationArguments.size(); i++) {
            String e = classificationArguments.get(i);
            if (e.equals("-c")) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found parameter [-c] but no value was specified");
                }

                Double value;
                try {
                    value = Double.valueOf(classificationArguments.get(i + 1));
                }
                catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "The value of parameter -c has to be a floating point value but was ["
                                    + classificationArguments.get(i + 1) + "]",
                            ex);
                }
                return value;
            }
        }

        LogFactory.getLog(LiblinearUtils.class)
                .info("Parameter c is set to default value [" + PARAM_C_DEFAULT + "]");
        return PARAM_C_DEFAULT;
    }

    public static double getParameterEpsilon(List<String> classificationArguments)
    {
        if (classificationArguments == null) {
            return EPISILON_DEFAULT;
        }

        for (int i = 0; i < classificationArguments.size(); i++) {
            String e = classificationArguments.get(i);
            if (e.equals("-e")) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found parameter [-e] but no value was specified");
                }

                Double value;
                try {
                    value = Double.valueOf(classificationArguments.get(i + 1));
                }
                catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "The value of parameter -e has to be a floating point value but was ["
                                    + classificationArguments.get(i + 1) + "]",
                            ex);
                }
                return value;
            }
        }

        LogFactory.getLog(LiblinearUtils.class).info("Parameter epsilon is set to [0.01]");
        return EPISILON_DEFAULT;
    }

    public static String outcomeMap2String(Map<String, Integer> map)
    {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Integer> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append("\t");
            sb.append(entry.getValue());
            sb.append("\n");
        }

        return sb.toString();
    }

    public static Map<String, Integer> string2outcomeMap(String s)
    {
        Map<String, Integer> outcomeMap = new HashMap<String, Integer>();
        for (String line : s.split("\n")) {
            String[] parts = line.split("\t");
            outcomeMap.put(parts[0], Integer.parseInt(parts[1]));
        }

        return outcomeMap;
    }

    public static void savePredictions(File outputFile, List<Double> predictions)
        throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for (Double prediction : predictions) {
            sb.append(prediction);
            sb.append("\n");
        }
        FileUtils.writeStringToFile(outputFile, sb.toString());
    }

    public static Map<String, Integer> createMapping(File... files)
        throws IOException
    {
        Set<String> uniqueOutcomes = new HashSet<>();
        for (File f : files) {
            uniqueOutcomes.addAll(pickOutcomes(f));
        }

        Map<String, Integer> mapping = new HashMap<>();
        int id = 0;
        for (String o : uniqueOutcomes) {
            mapping.put(o, id++);
        }

        return mapping;
    }

    private static Collection<? extends String> pickOutcomes(File file)
        throws IOException
    {
        Set<String> outcomes = new HashSet<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        String line = null;
        while (((line = br.readLine()) != null)) {
            if (line.isEmpty()) {
                continue;
            }
            int firstTabIdx = line.indexOf("\t");
            outcomes.add(line.substring(0, firstTabIdx));
        }
        br.close();
        return outcomes;
    }

    public static File replaceOutcomeByIntegerValue(File file, Map<String, Integer> outcomeMapping)
        throws IOException
    {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "utf-8"));
        File outFile = File.createTempFile("liblinear" + System.nanoTime(), ".tmp");
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));

        String line = null;
        while (((line = br.readLine()) != null)) {
            if (line.isEmpty()) {
                continue;
            }
            int firstTabIdx = line.indexOf("\t");
            Integer id = outcomeMapping.get(line.substring(0, firstTabIdx));
            bw.write(id + line.substring(firstTabIdx) + "\n");
        }
        br.close();
        bw.close();

        return outFile;
    }
}
