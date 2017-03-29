/**
 * Copyright 2017
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
package org.dkpro.tc.examples.single.sequence;

import java.io.File;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;

/**
 * This is a slightly ugly solution for recording the DKPro Lab output folder of an experiment to
 * read result files in JUnit tests
 */
public class ContextMemoryReport
    extends BatchReportBase
{

    /**
     * Name of the folder which will contain the id2outcome.txt that shall be used for evaluation
     * for TrainTest scenarios this is the *TestTask class of the respective machine learning
     * adapter e.g. Weka for CrossValidation experiments it is the ExperimentCrossValidation folder
     */
    public static String key; // this has to be set BEFORE the pipeline runs

    public static File id2outcome;

    @Override
    public void execute()
        throws Exception
    {
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (key != null && subcontext.getType().contains(key)) {
                StorageService storageService = getContext().getStorageService();

                if (key.contains("TestTask")) {
                    id2outcome = storageService.locateKey(subcontext.getId(),
                            Constants.ID_OUTCOME_KEY);
                }
                else {
                    id2outcome = storageService.locateKey(subcontext.getId(),
                            "id2homogenizedOutcome.txt");
                }
                return;
            }
        }
    }
}
