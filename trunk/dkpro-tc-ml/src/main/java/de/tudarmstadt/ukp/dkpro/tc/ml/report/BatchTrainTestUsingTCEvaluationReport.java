/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.ml.report;

import java.io.File;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.Id2Outcome;

/**
 * Collects the final evaluation results in a train/test setting.
 * 
 * @author daxenberger
 * @author Andriy Nadolskyy
 * 
 */
public class BatchTrainTestUsingTCEvaluationReport
    extends BatchReportBase
    implements Constants
{
    @Override
    public void execute()
        throws Exception
    {
        Id2Outcome overallOutcome = new Id2Outcome();

        for (TaskContextMetadata subcontext : getSubtasks()) {
            // FIXME this is a bad hack
            if (subcontext.getType().contains("TestTask")) {
                File id2outcomeFile = getContext().getStorageService().getStorageFolder(subcontext.getId(), ID_OUTCOME_KEY);
                Id2Outcome id2outcome = new Id2Outcome(id2outcomeFile);
                overallOutcome.addAll(id2outcome.getOutcomes());
            }
        }

        getContext().storeBinary(Constants.ID_OUTCOME_KEY, new PropertiesAdapter(overallOutcome.getProperties()));
    }
}