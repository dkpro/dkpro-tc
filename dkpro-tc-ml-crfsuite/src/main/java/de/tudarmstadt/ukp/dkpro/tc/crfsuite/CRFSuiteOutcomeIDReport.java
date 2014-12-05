/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

/**
 * Writes a instanceId / outcome pair for each classification instance.
 * 
 * @author zesch
 * 
 */
public class CRFSuiteOutcomeIDReport
    extends ReportBase
{
    /**
     * Name of the file where the instanceID / outcome pairs are stored
     */
    public static final String ID_OUTCOME_KEY = "id2outcome.txt";
    /**
     * Character that is used for separating fields in the output file
     */
    public static final String SEPARATOR_CHAR = ";";

    private static final String ID_CONSTANT_VALUE = Constants.ID_FEATURE_NAME + "=";

    @Override
    public void execute()
        throws Exception
    {
        List<String> labelGoldVsActual = getGoldAndPredictions();

        HashMap<String, Integer> mapping = createMappingLabel2Number(labelGoldVsActual);

        List<String> testData = getTestData();

        Properties props = generateProperties(mapping, labelGoldVsActual, testData);

        // add "#labels' line with all labels
        StringBuilder sb = new StringBuilder();
        sb.append("labels");
        for (String label : mapping.keySet()) {
            sb.append(" " + label);
        }

        getContext().storeBinary(
                ID_OUTCOME_KEY,
                new PropertiesAdapter(props, "ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD"
                        + "\n" + sb.toString()));
    }

    private HashMap<String, Integer> createMappingLabel2Number(List<String> aLabelGoldVsActual)
    {
        HashSet<String> labels = new HashSet<String>();

        for (String line : aLabelGoldVsActual) {
            String[] split = line.split("\t");
            if (split.length == 0) {
                continue;
            }
            if (split.length >= 1) {
                labels.add(split[0]);
            }
            if (split.length >= 2) {
                labels.add(split[1]);
            }
        }

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        int i = 0;
        for (String label : labels) {
            map.put(label, i);
            i++;
        }

        return map;
    }

    private List<String> getTestData()
        throws Exception
    {
        File storage = getContext().getStorageLocation(
                CRFSuiteTestTask.TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);

        File testFile = new File(storage.getAbsolutePath()
                + "/"
                + CRFSuiteAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.featureVectorsFile));

        List<String> readLines = FileUtils.readLines(testFile, "UTF-8");

        return readLines;
    }

    private List<String> getGoldAndPredictions()
        throws Exception
    {
        File storage = getContext().getStorageLocation(CRFSuiteTestTask.TEST_TASK_OUTPUT_KEY,
                AccessMode.READONLY);
        File predictionFile = new File(storage.getAbsolutePath()
                + "/"
                + CRFSuiteAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.predictionsFile));
        List<String> readLines = FileUtils.readLines(predictionFile, "UTF-8");

        return readLines;
    }

    protected static Properties generateProperties(HashMap<String, Integer> aMapping,
            List<String> predictions, List<String> testFeatures)
        throws Exception
    {
        Properties props = new Properties();

        int maxLines = predictions.size();

        for (int idx = 1; idx < maxLines; idx++) {
            String entry = predictions.get(idx);
            String[] split = entry.split("\t");
            if (split.length != 2) {
                continue;
            }

            String id = extractTCId(testFeatures.get(idx-1));
            int numPred = aMapping.get(split[1]);
            int numGold = aMapping.get(split[0]);
            String propEntry = numPred + SEPARATOR_CHAR + numGold;
            props.setProperty(id, propEntry);
        }

        return props;
    }

    private static String extractTCId(String line)
    {
        int begin = line.indexOf(ID_CONSTANT_VALUE);
        int end = line.indexOf("\t", begin + ID_CONSTANT_VALUE.length());
        
        // Assuming the ID is at the end of the line:
        String id = line.substring(begin + ID_CONSTANT_VALUE.length(), line.length());
        // But in case it's not:
        if(end != -1){
        	id = line.substring(begin + ID_CONSTANT_VALUE.length(), end);
        }
        return id;
    }
}