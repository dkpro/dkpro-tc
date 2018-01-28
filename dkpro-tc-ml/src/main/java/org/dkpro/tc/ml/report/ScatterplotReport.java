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
package org.dkpro.tc.ml.report;

import java.io.File;
import java.util.Iterator;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.ml.report.util.ScatterplotRenderer;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.core.EvaluationEntry;
import de.unidue.ltl.evaluation.util.convert.DKProTcDataFormatConverter;

public class ScatterplotReport
    extends BatchReportBase
    implements Constants
{

    @Override
    public void execute()
        throws Exception
    {

        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (TcTaskTypeUtil.isCrossValidationTask(getContext().getStorageService(),
                    subcontext.getId())) {
                File id2outcomeFile = getContext().getStorageService().locateKey(subcontext.getId(), Constants.COMBINED_ID_OUTCOME_KEY);

                EvaluationData<Double> data = DKProTcDataFormatConverter.convertRegressionModeId2Outcome(id2outcomeFile);
                                
                double [] gold = new double[(int) data.size()];
                double [] prediction = new double [(int) data.size()];
                Iterator<EvaluationEntry<Double>> iterator = data.iterator();
                
                int i=0;
                while(iterator.hasNext()) {
                	EvaluationEntry<Double> next = iterator.next();
                	gold[i] = next.getGold();
                	prediction[i] = next.getPredicted();
                	i++;
                }
                
                ScatterplotRenderer renderer = new ScatterplotRenderer(gold,
                       prediction);

                getContext().storeBinary("scatterplot.pdf", renderer);
            }
            else if (TcTaskTypeUtil.isMachineLearningAdapterTask(
                    getContext().getStorageService(), subcontext.getId())) {
                File id2outcomeFile = getContext().getStorageService().locateKey(subcontext.getId(),
                        Constants.ID_OUTCOME_KEY);

                Id2Outcome o = new Id2Outcome(id2outcomeFile, Constants.LM_REGRESSION);
                ScatterplotRenderer renderer = new ScatterplotRenderer(o.getGoldValues(),
                        o.getPredictions());
                getContext().storeBinary("scatterplot.pdf", renderer);
            }
        }
    }

}