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
import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.report.util.MetricComputationUtil;
import org.dkpro.tc.ml.report.util.SortedKeyProperties;

/**
 * A result report which creates a few basic measures and writes them to the output folder of a run
 * to provide by default at least some result values.
 */
public class BasicResultReport
    extends TcAbstractReport
    implements Constants
{
    public static boolean printResultsToSysout = true;

    private static String OUTPUT_FILE = "results.txt";
    
    public BasicResultReport()
    {
        
    }
    
    @Override
    public void execute() throws Exception
    {
        String learningMode = getDiscriminator(getContext().getStorageService(),
                getContext().getId(), DIM_LEARNING_MODE);

        Properties pa = new SortedKeyProperties();
        pa = addPredictedResults(pa, learningMode);
        pa = addMajorityBaselineResults(pa, learningMode);
        pa = addRandomBaselineResult(pa, learningMode);

        writeConfusionMatrixForSingleLabel(learningMode);
        writeFScoreForSingleLabel(learningMode);

        writeToDisk(pa);
    }

    private void writeFScoreForSingleLabel(String learningMode) throws Exception
    {
        if (!learningMode.equals(LM_SINGLE_LABEL)) {
            return;
        }

        File fscoreFile = getContext().getStorageService().locateKey(getContext().getId(),
                FILE_SCORE_PER_CATEGORY + ".tsv");

        File id2o = getContext().getStorageService().locateKey(getContext().getId(),
                ID_OUTCOME_KEY);

        ResultPerCategoryCalculator r = new ResultPerCategoryCalculator(id2o, learningMode);
        r.writeResults(fscoreFile);
    }

    private void writeToDisk(Properties pa) throws Exception
    {
        StorageService store = getContext().getStorageService();
        File key = store.locateKey(getContext().getId(), OUTPUT_FILE);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(key);
            pa.store(fos, "Results");
        }
        finally {
            IOUtils.closeQuietly(fos);
        }
    }

    private void writeConfusionMatrixForSingleLabel(String learningMode) throws Exception
    {
        if (!learningMode.equals(LM_SINGLE_LABEL)) {
            return;
        }
        File id2outcomeFile = getContext().getStorageService().locateKey(getContext().getId(),
                ID_OUTCOME_KEY);

        MetricComputationUtil.writeConfusionMatrix(id2outcomeFile,
                new File(id2outcomeFile.getParentFile(), FILE_CONFUSION_MATRIX));
    }

    private Properties addPredictedResults(Properties pa, String learningMode) throws Exception
    {
        File id2outcomeFile = getContext().getStorageService().locateKey(getContext().getId(),
                ID_OUTCOME_KEY);

        Map<String, String> resultMap = MetricComputationUtil.getResults(id2outcomeFile,
                learningMode);

        
        for (Entry<String, String> e : resultMap.entrySet()) {
            pa.setProperty(e.getKey(), e.getValue());
        }

		if (printResultsToSysout) {
			System.out.println("\n[" + getContext().getId() + "]");
			for (Entry<String, String> e : resultMap.entrySet()) {
				System.out.println("\t" + e.getKey() + ": " + e.getValue());
			}
			System.out.println("\n");
		} else {
			StringBuilder logMsg = new StringBuilder();
			logMsg.append("Run [" + getContext().getId() + "]: ");
			resultMap.keySet().forEach(x -> logMsg.append(x + "=" + resultMap.get(x) + " |"));
			LogFactory.getLog(getClass()).info(logMsg.toString());
        }

        return pa;
    }

    private Properties addRandomBaselineResult(Properties pa, String learningMode) throws Exception
    {
        File randomBaseline2outcomeFile = getContext().getStorageService()
                .locateKey(getContext().getId(), BASELINE_RANDOM_ID_OUTCOME_KEY);
        if (randomBaseline2outcomeFile != null && randomBaseline2outcomeFile.exists()) {
            Map<String, String> randomBaseline = MetricComputationUtil
                    .getResults(randomBaseline2outcomeFile, learningMode);
            String suffix = ".RandomClassBaseline";
            for (Entry<String, String> e : randomBaseline.entrySet()) {
                pa.setProperty(e.getKey() + suffix, e.getValue());
            }
        }
        return pa;
    }

    private Properties addMajorityBaselineResults(Properties pa, String learningMode)
        throws Exception
    {
        File baseline2outcomeFile = getContext().getStorageService().locateKey(getContext().getId(),
                BASELINE_MAJORITIY_ID_OUTCOME_KEY);
        
        if (baseline2outcomeFile != null && baseline2outcomeFile.exists()) {
            Map<String, String> baseline = MetricComputationUtil.getResults(baseline2outcomeFile,
                    learningMode);
            String suffix = ".MajorityClassBaseline";
            for (Entry<String, String> e : baseline.entrySet()) {
                pa.setProperty(e.getKey() + suffix, e.getValue());
            }
        }

        return pa;
    }

}