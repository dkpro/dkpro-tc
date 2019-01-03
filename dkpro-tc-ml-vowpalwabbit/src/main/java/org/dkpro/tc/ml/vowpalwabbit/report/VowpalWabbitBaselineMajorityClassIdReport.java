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
package org.dkpro.tc.ml.vowpalwabbit.report;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.dkpro.lab.storage.StorageService.AccessMode;
/**
 * Writes a instanceId / outcome data for each classification instance.
 */
public class VowpalWabbitBaselineMajorityClassIdReport extends VowpalWabbitOutcomeIDReport {

    private Random random = new Random(42);
    private List<String> pool = new ArrayList<>();

    public VowpalWabbitBaselineMajorityClassIdReport()
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
        buildPool(file);
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
                String[] split = line.split(" ");
                String o = split[0];
                if (!pool.contains(o)) {
                    pool.add(o);
                }
            }
        }

        Collections.shuffle(pool);
    }

    @Override
    protected String getPrediction(String p)
    {
        if(pool.size() == 1) {
            return pool.get(0);
        }
        
        Integer idx = random.nextInt(pool.size() - 1);
        return pool.get(idx);
    }

    @Override
    protected File getTargetOutputFile()
    {
        File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
        return new File(evaluationFolder, BASELINE_MAJORITIY_ID_OUTCOME_KEY);
    }
}