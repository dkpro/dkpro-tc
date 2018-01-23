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
package org.dkpro.tc.core.task;

import static org.dkpro.tc.core.Constants.DIM_READER_TEST;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;

/**
 * The InitTask writes the outcomes they encountered. Normally the training task should have seen
 * all outcomes, however one cannot blindly trust that this is the case. We, hence, record in
 * train/test the outcomes and merge them here to ensure that we know all outcomes in our data.
 */
public class OutcomeCollectionTask
    extends ExecutableTaskBase
{
    
    @Discriminator(name = DIM_READER_TEST)
    protected CollectionReaderDescription readerTest;

    /**
     * Public name of the task key
     */
    public static final String OUTPUT_KEY = "output";

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        Set<String> outcomes = new HashSet<>();

        File trainFolder = aContext.getFolder(InitTask.OUTPUT_KEY_TRAIN, AccessMode.READONLY);
        File trainOutcomes = new File(trainFolder, Constants.FILENAME_OUTCOMES);
        outcomes.addAll(FileUtils.readLines(trainOutcomes, "utf-8"));

        if (isTrainTest(aContext)) {
            File testFolder = aContext.getFolder(InitTask.OUTPUT_KEY_TEST, AccessMode.READONLY);
            File testOutcomes = new File(testFolder, Constants.FILENAME_OUTCOMES);
            outcomes.addAll(FileUtils.readLines(testOutcomes, "utf-8"));
        }

        File target = aContext.getFile(OUTPUT_KEY + "/" + Constants.FILENAME_OUTCOMES,
                AccessMode.READWRITE);
        FileUtils.writeLines(target, "utf-8", outcomes);

    }

    private boolean isTrainTest(TaskContext aContext)
    {
        File testOutputLocation = null;
        testOutputLocation  = aContext.getFolder(InitTask.OUTPUT_KEY_TEST, AccessMode.READONLY);
        File testOutcomes = new File(testOutputLocation, Constants.FILENAME_OUTCOMES);
        return testOutcomes.exists();
    }

}