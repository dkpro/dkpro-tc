/*******************************************************************************
 * Copyright 2015
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.reporting.FlexTable;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.Task;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.util.ReportUtils;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.Id2Outcome;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorFactory;

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
    private final List<String> discriminatorsToExclude = Arrays.asList(new String[] {
            "files_validation", "files_training" });
  	private boolean softEvaluation = true;
	private boolean individualLabelMeasures = false;

	
    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        FlexTable<String> table = FlexTable.forClass(String.class);

        for (TaskContextMetadata subcontext : getSubtasks()) {
            // FIXME this is a bad hack
            if (subcontext.getType().contains("TestTask")) {
                Map<String, String> discriminatorsMap = store.retrieveBinary(subcontext.getId(),
                        Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();
                String mode = getDiscriminatorValue(discriminatorsMap, DIM_LEARNING_MODE);
                File id2outcomeFile = getContext().getStorageService().getStorageFolder(subcontext.getId(), ID_OUTCOME_KEY);
                Id2Outcome id2outcome = new Id2Outcome(id2outcomeFile, mode);
                EvaluatorBase evaluator = EvaluatorFactory.createEvaluator(id2outcome, softEvaluation, individualLabelMeasures);
                Map<String, Double> resultTempMap = evaluator.calculateEvaluationMeasures();
                Map<String, String> resultMap = new HashMap<String, String>();
                for (String key : resultTempMap.keySet()) {
                	Double value = resultTempMap.get(key);
					resultMap.put(key, String.valueOf(value));
				}

                Map<String, String> values = new HashMap<String, String>();
                Map<String, String> cleanedDiscriminatorsMap = new HashMap<String, String>();

                for (String disc : discriminatorsMap.keySet()) {
                    if (!ReportUtils.containsExcludePattern(disc, discriminatorsToExclude)) {
                        cleanedDiscriminatorsMap.put(disc, discriminatorsMap.get(disc));
                    }
                }
                values.putAll(cleanedDiscriminatorsMap);
                values.putAll(resultMap);

                table.addRow(subcontext.getLabel(), values);
            }
        }
        
        getContext().getLoggingService().message(getContextLabel(),
                ReportUtils.getPerformanceOverview(table));
        // Excel cannot cope with more than 255 columns
        if (table.getColumnIds().length <= 255) {
            getContext()
                    .storeBinary(EVAL_FILE_NAME + "_compact" + SUFFIX_EXCEL, table.getExcelWriter());
        }
        getContext().storeBinary(EVAL_FILE_NAME + "_compact" + SUFFIX_CSV, table.getCsvWriter());
        table.setCompact(false);
        // Excel cannot cope with more than 255 columns
        if (table.getColumnIds().length <= 255) {
            getContext().storeBinary(EVAL_FILE_NAME + SUFFIX_EXCEL, table.getExcelWriter());
        }
        getContext().storeBinary(EVAL_FILE_NAME + SUFFIX_CSV, table.getCsvWriter());

        // output the location of the batch evaluation folder
        // otherwise it might be hard for novice users to locate this
        File dummyFolder = store.getStorageFolder(getContext().getId(), "dummy");
        // TODO can we also do this without creating and deleting the dummy folder?
        getContext().getLoggingService().message(getContextLabel(),
                "Storing detailed results in:\n" + dummyFolder.getParent() + "\n");
        dummyFolder.delete();
    }
    
    private String getDiscriminatorValue(Map<String, String> discriminatorsMap, String discriminatorName)
            throws TextClassificationException
        {
        	for (String key : discriminatorsMap.keySet()) {
    			if(key.split("\\|")[1].equals(discriminatorName)){
    				return discriminatorsMap.get(key);
    			}
    		}
        	throw new TextClassificationException(discriminatorName + " not found in discriminators set.");
        }
}