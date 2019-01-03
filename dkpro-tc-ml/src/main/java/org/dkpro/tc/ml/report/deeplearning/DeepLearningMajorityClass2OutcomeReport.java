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

package org.dkpro.tc.ml.report.deeplearning;

import java.io.BufferedReader;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.DeepLearningConstants;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
public class DeepLearningMajorityClass2OutcomeReport
    extends DeepLearningId2OutcomeReport
    implements DeepLearningConstants
{

    String majorityClass;

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
    protected void baselinePreparation() throws Exception
    {

        File folder = getContext().getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        File file = new File(folder, FILENAME_OUTCOME_VECTOR);
        determineMajorityClass(file);
    }

    @Override
    protected File getTargetFile()
    {
        return getContext().getFile(BASELINE_MAJORITIY_ID_OUTCOME_KEY, AccessMode.READWRITE);
    }

    private void determineMajorityClass(File f) throws Exception
    {
        FrequencyDistribution<String> fd = new FrequencyDistribution<>();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), UTF_8))){
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(" ");
                for (String v : split) {
                    fd.addSample(v, 1);
                }
            }
        }

        majorityClass = fd.getSampleWithMaxFreq();
    }

    @Override
    protected List<String> update(List<String> predictions)
    {

        List<String> out = new ArrayList<>();

        for (String p : predictions) {
            if (p.isEmpty()) {
                continue;
            }
            String[] split = p.split("\t");
            out.add(split[0] + "\t" + majorityClass);
        }

        return out;
    }

}