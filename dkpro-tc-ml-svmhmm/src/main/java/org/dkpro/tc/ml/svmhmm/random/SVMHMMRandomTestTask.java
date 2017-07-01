/*
 * Copyright 2017
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

package org.dkpro.tc.ml.svmhmm.random;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.ml.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.ml.svmhmm.task.SVMHMMTestTask;

/**
 * Random classifier for sequence labeling build upon SVMhmm adapter
 */
public class SVMHMMRandomTestTask
    extends SVMHMMTestTask
{
    static Random random = new Random(System.currentTimeMillis());

    @Override
    public void trainModel(TaskContext taskContext, File trainingFile, double paramC,
            int paramOrderE, int paramOrderT, double paramEpsilon, int paramB)
                throws Exception
    {
        // no training
    }

    @Override
    public void testModel(TaskContext taskContext, File testFile)
        throws Exception
    {
        // file to hold prediction results
        File predictionsFile = taskContext.getFile(
                new SVMHMMAdapter().getFrameworkFilename(
                        TcShallowLearningAdapter.AdapterNameEntries.predictionsFile),
                AccessMode.READWRITE);

        // number of expected outcomes
        List<String> strings = FileUtils.readLines(testFile, "utf-8");
        int numberOfTestInstances = strings.size();

        PrintWriter pw = new PrintWriter(new FileWriter(predictionsFile.getAbsolutePath()));

        for (int i = 0; i < numberOfTestInstances; i++) {
            pw.println(getRandomOutcome());
        }

        IOUtils.closeQuietly(pw);
    }

    protected Integer getRandomOutcome()
    {
        @SuppressWarnings("unchecked")
        List<Object> list = new ArrayList<Object>(this.labelsToIntegersMapping.values());

        // random label
        int i = random.nextInt(list.size());

        return (Integer) list.get(i);
    }
}
