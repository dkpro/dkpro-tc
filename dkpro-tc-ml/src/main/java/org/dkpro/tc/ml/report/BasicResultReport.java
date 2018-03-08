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
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.compress.utils.IOUtils;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.report.util.MetricComputationUtil;
import org.dkpro.tc.ml.report.util.SortedKeyProperties;

/**
 * A result report which creates a few basic measures and writes them to the output folder of a run to
 * provide by default at least some result values.
 */
public class BasicResultReport
    extends TcBatchReportBase
    implements Constants
{

    static String OUTPUT_FILE = "results.txt";

    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        File id2outcomeFile = getContext().getStorageService().locateKey(getContext().getId(),
                ID_OUTCOME_KEY);
        
        String learningMode = getDiscriminator(getContext().getStorageService(), getContext().getId(), DIM_LEARNING_MODE);

        Map<String, String> resultMap = MetricComputationUtil.getResults(id2outcomeFile, learningMode);
        
        Properties pa = new SortedKeyProperties();
		for (Entry<String, String> e : resultMap.entrySet()) {
			pa.setProperty(e.getKey(), e.getValue());
		}
        
        
        // Baseline results
        File baseline2outcomeFile = getContext().getStorageService().locateKey(getContext().getId(),
                BASELINE_MAJORITIY_ID_OUTCOME_KEY);
        //FIXME: Remove if-check after implementing this report for all adapters
        if(baseline2outcomeFile != null && baseline2outcomeFile.exists()){
        	Map<String, String> baseline = MetricComputationUtil.getResults(baseline2outcomeFile, learningMode);
        	String suffix = ".MajorityClassBaseline";
    		for (Entry<String, String> e : baseline.entrySet()) {
    			pa.setProperty(e.getKey() + suffix, e.getValue());
    		}
        }
        
        // Random baseline results
        File randomBaseline2outcomeFile = getContext().getStorageService().locateKey(getContext().getId(),
                BASELINE_RANDOM_ID_OUTCOME_KEY);
        //FIXME: Remove if-check after implementing this report for all adapters
        if(randomBaseline2outcomeFile != null && randomBaseline2outcomeFile.exists()){
        	Map<String, String> randomBaseline = MetricComputationUtil.getResults(randomBaseline2outcomeFile, learningMode);
        	String suffix = ".RandomClassBaseline";
    		for (Entry<String, String> e : randomBaseline.entrySet()) {
    			pa.setProperty(e.getKey()+suffix, e.getValue());
    		}
        }
        
        
        File key = store.locateKey(getContext().getId(), OUTPUT_FILE);
        FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(key);
			pa.store(fos, "Results");
		} finally {
			IOUtils.closeQuietly(fos);
		}
    }
 
}