/*******************************************************************************
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
 ******************************************************************************/

package org.dkpro.tc.ml.vowpalwabbit.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;

import com.google.gson.Gson;

public class VowpalWabbitDataWriter
    implements DataWriter
{
    public static final String OUTCOME_MAPPING = "outcomeMapping.txt";
    public static final String STRING_MAPPING = "stringValueMapping.txt";
    public static final String INDEX2INSTANCEID = "index2instanceMapping.txt";
    File outputDirectory;
    boolean useSparse;
    String learningMode;
    boolean applyWeigthing;
    // protected int maxId = 1; //vowpalWabbit doesn't like zeros as labels
    BufferedWriter bw = null;
    Gson gson = new Gson();
    File classifierFormatOutputFile;
    Map<String, String> outcomeMap;
    Map<String, String> stringToIntegerMap;
    Map<String, String> index2instanceId;
    int maxStringId = 1;
    int maxInstanceId;
    String featureMode;

    @Override
    public void writeGenericFormat(List<Instance> instances) throws AnalysisEngineProcessException
    {
        try {
            initGeneric();

            // bulk-write - in sequence mode this keeps the instances together
            // that
            // belong to the same sequence!
            Instance[] array = instances.toArray(new Instance[0]);
            bw.write(gson.toJson(array) + "\n");

            bw.close();
            bw = null;
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void initGeneric() throws IOException
    {
        if (bw != null) {
            return;
        }
        bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(
                                new File(outputDirectory, Constants.GENERIC_FEATURE_FILE), true),
                        "utf-8"));

    }

    @Override
    public void transformFromGeneric() throws Exception
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(outputDirectory, Constants.GENERIC_FEATURE_FILE)),
                "utf-8"));

        String line = null;
        while ((line = reader.readLine()) != null) {
            Instance[] instance = gson.fromJson(line, Instance[].class);
            List<Instance> ins = new ArrayList<>(Arrays.asList(instance));
            writeClassifierFormat(ins);
        }

        reader.close();
        FileUtils.deleteQuietly(new File(outputDirectory, Constants.GENERIC_FEATURE_FILE));
    }

    @Override
    public void writeClassifierFormat(List<Instance> instances)
        throws AnalysisEngineProcessException
    {
        try {
            initClassifierFormat();

            if (isSequenceMode()) {
                writeDataSequentially(instances);
            }
            else {
                writeInstanceData(instances);
            }
            closeStream();

            writeMapping(outputDirectory, OUTCOME_MAPPING, outcomeMap);
            writeMapping(outputDirectory, STRING_MAPPING, stringToIntegerMap);
            writeMapping(outputDirectory, INDEX2INSTANCEID, index2instanceId);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private void writeDataSequentially(List<Instance> instances) throws Exception
    {
        Collections.sort(instances, new Comparator<Instance>()
        {
            @Override
            public int compare(Instance o1, Instance o2)
            {
            	return Integer.compare(o1.getSequenceId(), o2.getSequenceId());
            }
        });
        List<List<Instance>> groupedBySequenceId = group(instances);

        for (List<Instance> instOfSeq : groupedBySequenceId) {
            writeInstanceData(instOfSeq);
            bw.write("\n");
        }

    }

    private List<List<Instance>> group(List<Instance> instances)
    {
        List<List<Instance>> groups = new ArrayList<>();

        // group by sequence id
        Map<Integer, List<Instance>> collect = instances.stream()
                .collect(Collectors.groupingBy(Instance::getSequenceId));

        // sort sequences by sequence position
        collect.forEach((x, y) -> {
            Collections.sort(y, new Comparator<Instance>()
            {

                @Override
                public int compare(Instance o1, Instance o2)
                {
                	return Integer.compare(o1.getSequencePosition(), o2.getSequencePosition());
                }
            });
            groups.add(y);
        });

        return groups;
    }

    private boolean isSequenceMode()
    {
        return featureMode.equals(Constants.FM_SEQUENCE);
    }

    private void writeInstanceData(List<Instance> instances) throws Exception
    {
        for (Instance instance : instances) {
            recordInstanceId(instance, maxInstanceId++, index2instanceId);
            bw.write(outcome(instance.getOutcome(), isRegression()) + " |");
            List<Feature> features = new ArrayList<Feature>(instance.getFeatures());
            for (int i = 0; i < features.size(); i++) {
                Feature feature = features.get(i);

                if (feature.getName().equals(Constants.ID_FEATURE_NAME)) {
                    continue;
                }

                String name = feature.getName();
                String value = getValue(feature);

                bw.write(name + ":" + value);

                if (i + 1 < features.size()) {
                    bw.write(" ");
                }
            }
            bw.write("\n");
        }
    }

    private void closeStream() throws IOException
    {
        bw.close();
        bw = null;
    }

    // build a map between the dkpro instance id and the index in the file
    private void recordInstanceId(Instance instance, int i, Map<String, String> index2instanceId)
    {
        Collection<Feature> features = instance.getFeatures();
        for (Feature f : features) {
            if (f.getName().equals(Constants.ID_FEATURE_NAME)) {
                index2instanceId.put(i + "", f.getValue() + "");
                return;
            }
        }
    }

    private void writeMapping(File outputDirectory, String outcomeMapping, Map<String, String> m)
        throws IOException
    {
        StringBuilder sb = new StringBuilder();
        if (!isRegression()) {

            for (Entry<String, String> e : m.entrySet()) {
                sb.append(e.getKey() + "\t" + e.getValue() + "\n");
            }
        }

        FileUtils.writeStringToFile(new File(outputDirectory, outcomeMapping), sb.toString(),
                "utf-8");
    }

    private String getValue(Feature feature)
    {

        if (feature.getType().equals(FeatureType.STRING)
                || feature.getType().equals(FeatureType.NOMINAL)) {
            String value = feature.getValue().toString();
            String idx = stringToIntegerMap.get(value);
            if (idx == null) {
                stringToIntegerMap.put(value, "" + maxStringId++);
                idx = stringToIntegerMap.get(value);
            }
            return idx.toString();
        }
        return feature.getValue().toString();
    }

    private String outcome(String outcome, boolean isRegression)
    {
        if (isRegression) {
            return outcome;
        }

        return outcomeMap.get(outcome).toString();
    }

    /**
     * Creates a mapping from the label names to integer values to identify labels by integers
     * 
     * @param outcomes
     */
    private void buildOutcomeMap(String[] outcomes)
    {
        if (isRegression()) {
            return;
        }
        outcomeMap = new HashMap<>();
        Integer i = getStartIndexForOutcomeMap();
        List<String> outcomesSorted = new ArrayList<>(Arrays.asList(outcomes));
        Collections.sort(outcomesSorted);
        for (String o : outcomesSorted) {
            outcomeMap.put(o, "" + i++);
        }
    }

    protected Integer getStartIndexForOutcomeMap()
    {
        // We start at one - not zero
        return 1;
    }

    private boolean isRegression()
    {
        return learningMode.equals(Constants.LM_REGRESSION);
    }

    private void initClassifierFormat() throws Exception
    {
        if (bw != null) {
            return;
        }

        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(classifierFormatOutputFile, true), "utf-8"));

    }

    @Override
    public void init(File outputDirectory, boolean useSparse, String learningMode,
            String featureMode, boolean applyWeighting, String[] outcomes)
        throws Exception
    {
        this.outputDirectory = outputDirectory;
        this.useSparse = useSparse;
        this.learningMode = learningMode;
        this.featureMode = featureMode;
        this.applyWeigthing = applyWeighting;

        classifierFormatOutputFile = new File(outputDirectory,
                Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);

        // Caution: DKPro Lab imports (aka copies!) the data of the train task
        // as test task. We use
        // appending mode for streaming. We might errornously append the old
        // training file with
        // testing data!
        // Force delete the old training file to make sure we start with a
        // clean, empty file
        if (classifierFormatOutputFile.exists()) {
            FileUtils.forceDelete(classifierFormatOutputFile);
        }

        File genericOutputFile = new File(outputDirectory, getGenericFileName());
        if (genericOutputFile.exists()) {
            FileUtils.forceDelete(genericOutputFile);
        }

        buildOutcomeMap(outcomes);
        stringToIntegerMap = new HashMap<>();
        index2instanceId = new HashMap<>();
    }

    @Override
    public boolean canStream()
    {
        return true;
    }

    @Override
    public String getGenericFileName()
    {
        return Constants.GENERIC_FEATURE_FILE;
    }

    @Override
    public void close() throws Exception
    {

    }

}
