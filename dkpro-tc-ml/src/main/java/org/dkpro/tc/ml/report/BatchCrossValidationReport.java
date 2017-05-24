/*******************************************************************************
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
 ******************************************************************************/
package org.dkpro.tc.ml.report;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.reporting.FlexTable;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.ReportUtils;
import org.dkpro.tc.util.EvaluationReportUtil;

/**
 * Collects the final evaluation results in a cross validation setting.
 * 
 */
public class BatchCrossValidationReport
    extends BatchReportBase
    implements Constants
{
    boolean softEvaluation = true;
    boolean individualLabelMeasures = false;

    public BatchCrossValidationReport()
    {
        // required by groovy
    }
    
    @Override
    public void execute()
        throws Exception
    {

        StorageService store = getContext().getStorageService();

        FlexTable<String> table = FlexTable.forClass(String.class);

        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (!TcTaskTypeUtil.isCrossValidationTask(store, subcontext.getId())) {
                continue;
            }
            Map<String, String> discriminatorsMap = ReportUtils.getDiscriminatorsForContext(store,
                    subcontext.getId(), Constants.DISCRIMINATORS_KEY_TEMP);

            File fileToEvaluate = store.locateKey(subcontext.getId(),
                    Constants.TEST_TASK_OUTPUT_KEY + "/" + Constants.SERIALIZED_ID_OUTCOME_KEY);

            Map<String, String> resultMap = EvaluationReportUtil.getResultsHarmonizedId2Outcome(
                    fileToEvaluate, softEvaluation, individualLabelMeasures);

            Map<String, String> values = new HashMap<String, String>();
            values.putAll(discriminatorsMap);
            values.putAll(resultMap);

            table.addRow(subcontext.getLabel(), values);
        }

        /*
         * TODO: make rows to columns e.g. create a new table and set columns to rows of old table
         * and rows to columns but than must be class FlexTable in this case adapted accordingly:
         * enable setting
         */

        ReportUtils.writeExcelAndCSV(getContext(), getContextLabel(), table, EVAL_FILE_NAME, SUFFIX_EXCEL, SUFFIX_CSV);
    }
}
