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

package org.dkpro.tc.ml.crfsuite.reports;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.dkpro.lab.storage.StorageService.AccessMode;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Writes a instanceId / outcome pair for each classification instance.
 */
public class CrfSuiteBaselineMajorityClassIdReport
    extends CrfSuiteOutcomeIDReport
{
    private String majorityClass;

    public CrfSuiteBaselineMajorityClassIdReport()
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
        return getContext().getFile(BASELINE_MAJORITIY_ID_OUTCOME_KEY, AccessMode.READWRITE);
    }

    @Override
    protected void prepareBaseline() throws Exception
    {
        File folder = getContext().getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        File file = new File(folder, FILENAME_DATA_IN_CLASSIFIER_FORMAT);
        determineMajorityClass(file);
    }

    @Override
    protected String getPrediction(Map<String, Integer> map, String s)
    {
        return map.get(majorityClass).toString();
    }

    private void determineMajorityClass(File file) throws Exception
    {

        FrequencyDistribution<String> fd = new FrequencyDistribution<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), UTF_8))) {

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                String[] split = line.split("\t");
                fd.addSample(split[0], 1);
            }

        }

        majorityClass = fd.getSampleWithMaxFreq();
    }
}