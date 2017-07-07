/**
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.ml.weka.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.task.uima.FeatureType;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;

import com.google.gson.Gson;

import weka.core.Utils;

/*
 * Datawriter for the Weka machine learning tool.
 */
public class MekaDataWriterFast
    implements DataWriter, Constants
{
    BufferedWriter bw = null;
    Gson gson = new Gson();
    private boolean applyWeighting;
    private File outputFolder;
    private File arffTarget;
    boolean isRegression;

    private String[] outcomes;

    private boolean isAlreadyInitialized;
    private StringBuilder headerFile;

    private static final String NUMERIC = "numeric";
    private static final String STRING = "string";
    private static final String ENUM = "enum";
    private static final String BOOLEAN = "boolean";

    int numberFeatures = -1;
    Map<String, Integer> name2id;
    Map<String, String> name2featureType;

    @Override
    public void init(File outputFolder, boolean useSparse, String learningMode,
            boolean applyWeighting, String[] outcomes)
                throws Exception
    {
        this.outputFolder = outputFolder;
        this.applyWeighting = applyWeighting;
        this.outcomes = outcomes;

        arffTarget = new File(outputFolder, WekaClassificationAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile));

        // Caution: DKPro Lab imports (aka copies!) the data of the train task
        // as test task. We use
        // appending mode for streaming. We might errornously append the old
        // training file with
        // testing data!
        // Force delete the old training file to make sure we start with a
        // clean, empty file
        if (arffTarget.exists()) {
            FileUtils.forceDelete(arffTarget);
        }

        isRegression = learningMode.equals(LM_REGRESSION);

        if (!arffTarget.exists()) {
            arffTarget.getParentFile().mkdirs();
        }

    }

    @Override
    public void writeGenericFormat(Collection<Instance> instances)
        throws Exception
    {
        initGeneric();

        bw.write(gson.toJson(instances.toArray(new Instance[0])) + System.lineSeparator());

        bw.close();
        bw = null;
    }

    private void initGeneric()
        throws IOException
    {
        if (bw != null) {
            return;
        }
        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(outputFolder, GENERIC_FEATURE_FILE), true), "utf-8"));
    }

    @Override
    public void transformFromGeneric()
        throws Exception
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(outputFolder, GENERIC_FEATURE_FILE)), "utf-8"));

        String line = null;
        while ((line = reader.readLine()) != null) {
            Instance[] restoredInstances = gson.fromJson(line, Instance[].class);
            writeClassifierFormat(Arrays.asList(restoredInstances), classiferReadsCompressed());
        }

        reader.close();
        FileUtils.deleteQuietly(new File(outputFolder, GENERIC_FEATURE_FILE));
    }

    @Override
    public void writeClassifierFormat(Collection<Instance> instances, boolean compress)
        throws Exception
    {
        BufferedWriter writer = reOpenArffWriter();
        String header = initArffOutput();
        if (header != null) {
            // first call only
            writer.write(header);
        }

        for (Instance inst : instances) {

            // multi label encoded
            List<String> insOutcomes = inst.getOutcomes();

            int[] ml = new int[outcomes.length];
            for (int i = 0; i < outcomes.length; i++) {
                if (insOutcomes.contains(outcomes[i])) {
                    ml[i] = 1;
                }
                else {
                    ml[i] = 0;
                }

            }
            for (int i : ml) {
                writer.write(i + ",");
            }
            
            
            String[] values = new String[numberFeatures];
            for (Feature f : inst.getFeatures()) {
                Integer integer = name2id.get(f.getName());
                values[integer] = transformValue(f.getName(), f.getValue());
            }

            for (int i = 0; i < values.length; i++) {
                String v = values[i];
                if (v == null) {
                    writer.write("?");
                }
                else {
                    writer.write(v);
                }
                if (i + 1 < values.length) {
                    writer.write(",");
                }
            }

            if (applyWeighting) {
                double weight = inst.getWeight();
                writer.write(",{");
                writer.write(weight + "");
                writer.write("}");
            }

            writer.write(System.lineSeparator());
        }

        writer.close();

    }

    private String transformValue(String name, Object object)
    {
        String type = name2featureType.get(name);
        switch (type) {
        case STRING:
        case ENUM:
            return Utils.quote(object.toString());
        case NUMERIC:
            return object.toString();
        case BOOLEAN:
            return ((Boolean) object ? "1.0" : "0.0");
        }

        throw new IllegalArgumentException("Feature type of [" + name + "] is unknown");
    }

    private BufferedWriter reOpenArffWriter()
        throws Exception
    {
        return new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(arffTarget, true), "utf-8"));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String initArffOutput()
        throws IOException
    {
        if (isAlreadyInitialized) {
            return null;
        }

        name2featureType = new HashMap<>();
        headerFile = new StringBuilder();
        name2id = new HashMap<>();

        List<String> lines = FileUtils.readLines(
                new File(outputFolder, Constants.FILENAME_FEATURES_DESCRIPTION), "utf-8");
        Collections.sort(lines);

        numberFeatures = lines.size();

        // build map
        for (int i = 0; i < lines.size(); i++) {
            String[] split = lines.get(i).split("\t");
            name2id.put(split[0], i);
        }

        // build header
        headerFile.append(
                "@relation 'dkpro-tc-generated -C "+outcomes.length+"'" + System.lineSeparator() + System.lineSeparator());
        
        
        //outcomes
        for (int i = 0; i < outcomes.length; i++) {
            headerFile.append("@attribute " + outcomes[i] + " {0,1}" + System.lineSeparator());
        }
        
        
        for (int i = 0; i < lines.size(); i++) {
            String[] split = lines.get(i).split("\t");

            String escapedName = Utils.quote(split[0]);

            FeatureType ft = FeatureType.valueOf(split[1]);
            if (ft == FeatureType.NUM || ft == FeatureType.NUM_FLOATING_POINT
                    || ft == FeatureType.NUM_INTEGER) {
                headerFile
                        .append("@attribute " + escapedName + " numeric" + System.lineSeparator());
                name2featureType.put(split[0], NUMERIC);
            }
            else if (ft == FeatureType.BOOLEAN) {
                headerFile
                        .append("@attribute " + escapedName + " numeric" + System.lineSeparator());
                name2featureType.put(split[0], BOOLEAN);
            }
            else if (ft == FeatureType.ENUM) {
                Class<Enum> c = null;
                try {
                    headerFile.append("@attribute " + escapedName + " {");
                    c = (Class<Enum>) Class.forName(split[2]);
                    Enum[] enumConstants = c.getEnumConstants();
                    for (int j = 0; j < enumConstants.length; j++) {
                        Enum e = enumConstants[j];
                        headerFile.append(e.toString());
                        if (j + 1 < enumConstants.length) {
                            headerFile.append(",");
                        }
                    }
                    headerFile.append("}" + System.lineSeparator());
                    name2featureType.put(split[0], ENUM);
                }
                catch (ClassNotFoundException e) {
                    throw new IOException(e);
                }
            }
            else { // String
                headerFile.append("@attribute " + escapedName + " string" + System.lineSeparator());
                name2featureType.put(split[0], STRING);
            }

        }

        
        headerFile.append("@data" + System.lineSeparator());

        isAlreadyInitialized = true;

        return headerFile.toString();
    }

    @Override
    public boolean canStream()
    {
        return true;
    }

    @Override
    public boolean classiferReadsCompressed()
    {
        return true;
    }

    @Override
    public String getGenericFileName()
    {
        return GENERIC_FEATURE_FILE;
    }

    @Override
    public void close()
        throws Exception
    {
    }

}
