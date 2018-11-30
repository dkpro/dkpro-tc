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
package org.dkpro.tc.io.libsvm.reports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;
import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.storage.StorageService.AccessMode;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class LibsvmDataFormatBaselineMajorityClassIdReport
    extends LibsvmDataFormatOutcomeIdReport
{

    String majorityClass;

    public LibsvmDataFormatBaselineMajorityClassIdReport()
    {

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
    protected void baslinePreparation() throws Exception
    {
        File folder = getContext().getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        File file = new File(folder, FILENAME_DATA_IN_CLASSIFIER_FORMAT);
        determineMajorityClass(file);
    }

    @Override
    protected String getPrediction(String p)
    {
        return majorityClass;
    }

    private void determineMajorityClass(File file) throws Exception
    {

        FrequencyDistribution<String> fd = new FrequencyDistribution<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8));

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                String[] split = line.split("\t");
                fd.addSample(split[0], 1);
            }

        }
        finally {
            IOUtils.closeQuietly(reader);
        }

        majorityClass = fd.getSampleWithMaxFreq();
    }

    @Override
    protected File getTargetOutputFile()
    {
        File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
        return new File(evaluationFolder, BASELINE_MAJORITIY_ID_OUTCOME_KEY);
    }

}