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
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.CRFSuiteTestTask;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter.AdapterNameEntries;

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

    private static final String ID_CONSTANT_VALUE = AddIdFeatureExtractor.ID_FEATURE_NAME + "=";

    @Override
    public void execute()
        throws Exception
    {
        List<String> predictions = getPredictions();

        List<String> testData = getTestData();

        Properties props = generateProperties(predictions, testData);
        getContext().storeBinary(ID_OUTCOME_KEY,
                new PropertiesAdapter(props, "ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD"));
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

    private List<String> getPredictions()
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

    protected static Properties generateProperties(List<String> predictions,
            List<String> testFeatures)
        throws Exception
    {
        Properties props = new Properties();

        int maxLines = predictions.size();

        for (int idx = 0; idx < maxLines; idx++) {
            String prediction = predictions.get(idx);
            String[] split = prediction.split("\t");
            if (split.length != 2) {
                continue;
            }

            String id = extractTCId(testFeatures.get(idx));

            String entry = split[1] + SEPARATOR_CHAR + split[0];
            props.setProperty(id, entry);
        }

        return props;
    }

    private static String extractTCId(String line)
    {
        int begin = line.indexOf(ID_CONSTANT_VALUE);
        int end = line.indexOf("\t", begin + ID_CONSTANT_VALUE.length());

        String id = line.substring(begin + ID_CONSTANT_VALUE.length(), end);
        return id;
    }
}