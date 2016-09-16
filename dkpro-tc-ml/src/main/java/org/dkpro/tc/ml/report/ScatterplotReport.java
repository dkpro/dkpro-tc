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
package org.dkpro.tc.ml.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.ml.report.util.ScatterplotRenderer;

public class ScatterplotReport
    extends BatchReportBase
    implements Constants
{
	
    @Override
    public void execute()
        throws Exception
    {
    	
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (TaskTypeIdentificationUtil.isCrossValidationTask(getContext().getStorageService(), subcontext.getId())) 
            {    
		        File id2outcomeFile = getContext().getStorageService().locateKey(
		        		subcontext.getId(), Constants.TEST_TASK_OUTPUT_KEY + "/" + Constants.SERIALIZED_ID_OUTCOME_KEY);
		        
		        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(id2outcomeFile));
		        Id2Outcome o = (Id2Outcome) inputStream.readObject();
		        inputStream.close();
		        
				ScatterplotRenderer renderer = new ScatterplotRenderer(o.getGoldValues(), o.getPredictions());
		
		        getContext().storeBinary("scatterplot.pdf", renderer);
	        }
            else if (TaskTypeIdentificationUtil.isMachineLearningAdapterTask(getContext().getStorageService(), subcontext.getId()))
            {    
		        File id2outcomeFile = getContext().getStorageService().locateKey(
		        		subcontext.getId(), Constants.ID_OUTCOME_KEY);
		        
		        Id2Outcome o = new Id2Outcome(id2outcomeFile, Constants.LM_REGRESSION);
		        
				ScatterplotRenderer renderer = new ScatterplotRenderer(o.getGoldValues(), o.getPredictions());
		
		        getContext().storeBinary("scatterplot.pdf", renderer);
	        }
        }
    }
}