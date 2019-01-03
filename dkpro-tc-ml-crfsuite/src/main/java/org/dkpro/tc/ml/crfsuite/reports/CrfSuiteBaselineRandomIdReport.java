/*******************************************************************************
 * Copyright 2019
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

package org.dkpro.tc.ml.crfsuite.reports;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.dkpro.lab.storage.StorageService.AccessMode;

/**
 * Writes a instanceId / outcome pair for each classification instance.
 */
public class CrfSuiteBaselineRandomIdReport
    extends CrfSuiteOutcomeIDReport
{
    private Random random = new Random(42);
    private List<String> pool = new ArrayList<>();

    public CrfSuiteBaselineRandomIdReport()
    {
        // requried by groovy
    }

    @Override
    public void execute() throws Exception
    {

        boolean isRegression = getDiscriminator(getContext(), DIM_LEARNING_MODE)
                .equals(LM_REGRESSION);
        if (isRegression) {
            return;
        }

        super.execute();
    }

    @Override
    protected File getTargetFile()
    {
        return getContext().getFile(BASELINE_RANDOM_ID_OUTCOME_KEY, AccessMode.READWRITE);
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
    protected String getPrediction(Map<String, Integer> map, String s)
    {
        if (pool.size() == 1) {
            return pool.get(0);
        }

        return "" + random.nextInt(pool.size() - 1);
    }

    private void buildPool(File file) throws Exception
    {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), UTF_8))) {

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                String[] split = line.split("\t");
                if (!pool.contains(split[0])) {
                    pool.add(split[0]);
                }
            }

        }
        Collections.sort(pool);
    }
}