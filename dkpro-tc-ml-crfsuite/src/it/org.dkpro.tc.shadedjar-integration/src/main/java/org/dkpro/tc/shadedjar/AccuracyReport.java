/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.shadedjar;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
import org.dkpro.tc.evaluation.measures.label.Accuracy;

public class AccuracyReport
    extends BatchReportBase
    implements Constants
{
    public static final String ACC = "accuracy.txt";

    @Override
    public void execute()
        throws Exception
    {
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().contains("TestTask")) {
                StorageService storageService = getContext().getStorageService();

                File id2outcome = storageService.locateKey(subcontext.getId(),
                        Constants.ID_OUTCOME_KEY);
                Id2Outcome o = new Id2Outcome(id2outcome, Constants.LM_SINGLE_LABEL);
                EvaluatorBase createEvaluator = EvaluatorFactory.createEvaluator(o, true, false);
                Double double1 = createEvaluator.calculateEvaluationMeasures()
                        .get(Accuracy.class.getSimpleName());

                FileUtils.write(new File("target", ACC), double1 + "");
            }
        }
    }

}