/*
 * Copyright 2014
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
 */

package de.tudarmstadt.ukp.dkpro.tc.svmhmm.random;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.SVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Random classifier for sequence labeling build upon SVMhmm adapter
 *
 * @author Ivan Habernal
 */
public class SVMHMMRandomTestTask
        extends SVMHMMTestTask
{
    static Random random = new Random(System.currentTimeMillis());

    @Override
    protected void trainModel(TaskContext taskContext, File trainingFile)
            throws Exception
    {
        // no training
    }

    @Override
    protected void testModel(TaskContext taskContext, File testFile)
            throws Exception
    {
        // file to hold prediction results
        File predictionsFile = new File(taskContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                StorageService.AccessMode.READWRITE), new SVMHMMAdapter().getFrameworkFilename(
                TCMachineLearningAdapter.AdapterNameEntries.predictionsFile));

        // number of expected outcomes
        List<String> strings = FileUtils.readLines(testFile);
        int numberOfTestInstances = strings.size();

        PrintWriter pw = new PrintWriter(new FileWriter(predictionsFile.getAbsolutePath()));

        for (int i = 0; i < numberOfTestInstances; i++) {
            pw.println(getRandomOutcome());
        }

        IOUtils.closeQuietly(pw);
    }

    protected Integer getRandomOutcome()
    {
        List<Object> list = new ArrayList<Object>(this.labelsToIntegersMapping.values());

        // random label
        int i = random.nextInt(list.size());

        return (Integer) list.get(i);
    }
}
