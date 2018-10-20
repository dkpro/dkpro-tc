/**
 * Copyright 2018
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
package org.dkpro.tc.ml.weka.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.storage.StorageService.AccessMode;

import weka.core.Attribute;

/**
 * Writes a instanceId / outcome data for each classification instance.
 */
public class WekaBaselineRandomIdReport
    extends WekaOutcomeIDReport
{

    private Random random = new Random(42);

    private List<String> pool = new ArrayList<>();

    public WekaBaselineRandomIdReport()
    {
        // required by groovy
    }

    @Override
    public void execute() throws Exception
    {

        init();

        if (isRegression) {
            return;
        }

        super.execute();
    }

    @Override
    protected String getPrediction(Double prediction, List<String> labels,
            Attribute gsAtt)
    {
        Map<String, Integer> class2number = classNamesToMapping(labels);
        Integer idx = random.nextInt(pool.size());
        return class2number.get(pool.get(idx)).toString();
    }

    @Override
    protected void prepareBaseline() throws Exception
    {
        File folder = getContext().getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        File file = new File(folder, FILENAME_DATA_IN_CLASSIFIER_FORMAT);
        buildPool(file);
    }

    @Override
    protected File getTargetOutputFile()
    {
        File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
        return new File(evaluationFolder, BASELINE_RANDOM_ID_OUTCOME_KEY);
    }

    private void buildPool(File file) throws Exception
    {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("@")) {
                    continue;
                }

                String[] split = line.split(",");

                String v = split[split.length - 1];
                if (hasInstanceWeighting(v)) {
                    v = split[split.length - 2];
                }

                if (!pool.contains(v)) {
                    pool.add(v);
                }
            }

        }
        finally {
            IOUtils.closeQuietly(reader);
        }

        Collections.shuffle(pool);
    }

    private boolean hasInstanceWeighting(String v)
    {
        return v.startsWith("{") && v.endsWith("}");
    }

}