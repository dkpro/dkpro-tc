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

import static org.dkpro.tc.core.util.ReportUtils.getDiscriminatorValue;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcTaskTypeUtil;
import org.dkpro.tc.core.util.ReportUtils;
import org.dkpro.tc.core.util.TcFlexTable;

import de.unidue.ltl.evaluation.core.EvaluationData;
import de.unidue.ltl.evaluation.measures.categorial.Fscore;
import de.unidue.ltl.evaluation.measures.categorial.multi.ExactMatchRatio;
import de.unidue.ltl.evaluation.measures.categorial.single.Accuracy;
import de.unidue.ltl.evaluation.measures.correlation.PearsonCorrelation;
import de.unidue.ltl.evaluation.measures.correlation.SpearmanCorrelation;
import de.unidue.ltl.evaluation.measures.regression.MeanAbsoluteError;
import de.unidue.ltl.evaluation.measures.regression.MeanSquaredError;
import de.unidue.ltl.evaluation.measures.regression.RSquared;
import de.unidue.ltl.evaluation.util.convert.DKProTcDataFormatConverter;

/**
 * Collects the final evaluation results in a train/test setting.
 */
public class BatchTrainTestReport
    extends TcBatchReportBase
    implements Constants
{
    private final List<String> discriminatorsToExclude = Arrays
            .asList(new String[] { DIM_FILES_VALIDATION, DIM_FILES_TRAINING });
    
    public BatchTrainTestReport()
    {
        // required by groovy
    }

    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        TcFlexTable<String> table = TcFlexTable.forClass(String.class);

        for (TaskContextMetadata subcontext : getSubtasks()) {

            if (!TcTaskTypeUtil.isMachineLearningAdapterTask(store,
                    subcontext.getId())) {
                continue;
            }
            Map<String, String> discriminatorsMap = getDiscriminators(store, subcontext.getId());
            discriminatorsMap = ReportUtils.clearDiscriminatorsByExcludePattern(discriminatorsMap,
                    discriminatorsToExclude);

            // add the results into the discriminator map
            File id2o = getId2Outcome();
            String mode = getDiscriminatorValue(discriminatorsMap, DIM_LEARNING_MODE);
            
            Map<String, String> resultMap = getResults(id2o, mode);
            discriminatorsMap.putAll(resultMap);

            table.addRow(subcontext.getLabel(), discriminatorsMap);
        }

        ReportUtils.writeExcelAndCSV(

        getContext(), getContextLabel(), table, EVAL_FILE_NAME, SUFFIX_EXCEL, SUFFIX_CSV);
    }

	private Map<String, String> getResults(File id2o, String mode) throws Exception {

		Map<String, String> map = new HashMap<>();

		if (mode.equals(Constants.LM_SINGLE_LABEL)) {
			EvaluationData<String> data = DKProTcDataFormatConverter.convertSingleLabelModeId2Outcome(id2o);

			Accuracy<String> acc = new Accuracy<>(data);
			map.put(acc.getClass().getName(), "" + acc.getResult());

			Fscore<String> fmeasure = new Fscore<>(data);
			map.put("Micro FScore", "" + fmeasure.getMicroFscore());
			map.put("Macro FScore", "" + fmeasure.getMacroFscore());
		}
		else if(mode.equals(Constants.LM_REGRESSION)){
			EvaluationData<Double> data = DKProTcDataFormatConverter.convertRegressionModeId2Outcome(id2o);

			RSquared rsq = new RSquared(data);
			map.put(rsq.getClass().getName(), "" + rsq.getResult());
			
			PearsonCorrelation pc = new PearsonCorrelation(data);
			map.put(pc.getClass().getName(), "" + pc.getResult());
			
			SpearmanCorrelation sc = new SpearmanCorrelation(data);
			map.put(sc.getClass().getName(), "" + sc.getResult());
			
			MeanSquaredError mse = new MeanSquaredError(data);
			map.put(mse.getClass().getName(), "" + mse.getResult());
			
			MeanAbsoluteError mae = new MeanAbsoluteError(data);
			map.put(mae.getClass().getName(), "" + mae.getResult());
			
		}
		else if (mode.equals(Constants.LM_MULTI_LABEL)){
			
			EvaluationData<String> data = DKProTcDataFormatConverter.convertMultiLabelModeId2Outcome(id2o);
			
			ExactMatchRatio<String> emr = new ExactMatchRatio<>(data);
			map.put(emr.getClass().getName(), "" + emr.getResult());
		}

		return map;
	}

	private Map<String, String> getDiscriminators(StorageService store, String id)
    {
        return store.retrieveBinary(id, Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();
    }
}