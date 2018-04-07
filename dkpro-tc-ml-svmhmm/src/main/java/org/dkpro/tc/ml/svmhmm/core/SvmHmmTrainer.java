/**
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
package org.dkpro.tc.ml.svmhmm.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

/**
 * Wrapper for training and testing using SVM_HMM C implementation with default parameters. Consult
 * {@code http://www.cs.cornell.edu/people/tj/svm_light/svm_hmm.html} for parameter settings.
 * 
 * <pre>
 * Parameters:
 *   -c      Typical SVM parameter C trading-off slack vs. magnitude of the weight-vector. 
 *               NOTE: The default value for this parameter is unlikely to work well for your
 *               particular problem. A good value for C must be selected via cross-validation, ideally
 *               exploring values over several orders of magnitude. NOTE: Unlike in V1.01, the value of C is
 *               divided by the number of training examples. So, to get results equivalent to V1.01, multiply
 *               C by the number of training examples. Default value is set to 1.
 *   -e      Parameter "-e &lt;EPSILON&gt;": This specifies the precision to which constraints are
 *               required to be satisfied by the solution. The smaller EPSILON, the longer and the more memory
 *               training takes, but the solution is more precise. However, solutions more accurate than 0.5
 *               typically do not improve prediction accuracy.
 *   -t      Order of dependencies of transitions in HMM. Can be any number larger than 1. (default 1)
 *   -m      Order of dependencies of emissions in HMM. Can be any number
 *               larger than 0. (default 0) UPDATE: according to svm_struct_api.c: must be either 0 or 1;
 *               fails for &gt;1
 *   -b       A non-zero value turns on (approximate) beam search to replace
 *               the exact Viterbi algorithm both for finding the most violated constraint, as well as for
 *               computing predictions. The value is the width of the beam used (e.g. 100). (default 0).
 * </pre>
 */
public class SvmHmmTrainer
    extends SvmHmm
{
    private static RuntimeProvider runtimeProvider;

    public File train(File data, File model, String... parameters) throws Exception
    {
        List<String> command = buildTrainCommand(data, model, parameters);
        runCommand(command);
        return model;
    }

    public static File getTrainExecutable() throws Exception
    {

        if (runtimeProvider == null) {
            runtimeProvider = new RuntimeProvider("classpath:/org/dkpro/tc/ml/svmhmm/");
        }

        return runtimeProvider.getFile("svm_hmm_learn");
    }

    public static List<String> buildTrainCommand(File trainingFile, File targetModelLocation,
            String[] parameters)
        throws Exception
    {

        if (parameters.length % 2 != 0) {
            throw new IllegalStateException(
                    "Parameter number must be even provided as two separate values, e.g \"-c\", \"5.0\" ");
        }

        areSwitchsKnwon(parameters);

        List<String> result = new ArrayList<>();
        result.add(getTrainExecutable().getAbsolutePath());

        for (String s : new String[] { "-c", "--e", "--t", "-e", "-b", "-m" }) {
            result.addAll(processForSwitch(s, parameters));
        }

        // // svm struct params
        // result.add("-c");
        // result.add();
        // result.add(String.format(Locale.ENGLISH, "%f", paramC));
        // result.add("--e");
        // result.add(Integer.toString(paramOrderE));
        // result.add("--t");
        // result.add(Integer.toString(paramOrderT));
        // result.add("-e");
        // result.add(String.format(Locale.ENGLISH, "%f", paramEpsilon));
        // result.add("--b");
        // result.add(Integer.toString(paramB));

        // training file
        result.add(trainingFile.getAbsolutePath());

        // output model
        result.add(targetModelLocation.getAbsolutePath());

        return result;
    }

    private static void areSwitchsKnwon(String[] parameters)
    {

        List<String> asList = Arrays.asList(new String[] { "-c", "--e", "--t", "-e", "-b", "-m" });
        for (int i = 0; i < parameters.length; i += 2) {
            if (!asList.contains(parameters[i])) {
                throw new IllegalArgumentException("Unknonw switch [" + parameters[i] + "]");
            }
        }

    }

    private static Collection<? extends String> processForSwitch(String s, String[] parameters)
    {
        List<String> out = new ArrayList<>();

        for (int i = 0; i < parameters.length; i += 2) {
            if (parameters[i].equals(s)) {
                out.add(s);
                out.add(parameters[i + 1]);
                return out;
            }
        }

        out.add(s);
        out.add(getDefault(s));

        return out;
    }

    private static String getDefault(String s)
    {
        if (s.equals("-c")) {
            return "1";
        }
        else if (s.equals("-e")) {
            return "0.5";
        }
        else if (s.equals("-m")) {
            return "0";
        }
        else if (s.equals("--e")) {
            return "0";
        }
        else if (s.equals("--t")) {
            return "1";
        }
        else if (s.equals("-b")) {
            return "1";
        }

        throw new IllegalStateException("The switch [" + s + "] is unknown");
    }

}
