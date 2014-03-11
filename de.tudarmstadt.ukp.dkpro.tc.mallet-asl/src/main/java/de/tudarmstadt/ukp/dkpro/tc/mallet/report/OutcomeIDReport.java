package de.tudarmstadt.ukp.dkpro.tc.mallet.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.mallet.task.TestTask;

/**
 * Writes a instanceId / outcome pair for each classification instance.
 * 
 * @author zesch
 * 
 */
public class OutcomeIDReport
    extends ReportBase
{
    public static final String ID_OUTCOME_KEY = "id2outcome.txt";

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(TestTask.OUTPUT_KEY, AccessMode.READONLY);
        File filePredictions = new File(storage.getAbsolutePath() + "/" + TestTask.PREDICTIONS_KEY);
        File fileId2Outcome = new File(getContext().getStorageLocation(TestTask.OUTPUT_KEY, AccessMode.READWRITE)
                .getPath() + "/" + ID_OUTCOME_KEY);
        BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePredictions))));
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileId2Outcome));
        String line = null;
        boolean header = false;
        int outcomeIndex = -1;
        int predictedOutcomeIndex = -1;
        int instanceIdIndex = -1;
        while ((line = br.readLine()) != null) {
        	if (!header) {
        		header = true;
        		String featureNames[] = line.split(" ");
        		for (int i = 0; i < featureNames.length; i++) {
        			if (featureNames[i].equals(TestTask.OUTCOME_CLASS_LABEL_NAME)) {
        				outcomeIndex = i;
        			}
        			else if (featureNames[i].equals(TestTask.PREDICTION_CLASS_LABEL_NAME)) {
        				predictedOutcomeIndex = i;
        			}
        			else if (featureNames[i].equals(AddIdFeatureExtractor.ID_FEATURE_NAME)) {
        				instanceIdIndex = i;
        			}
        		}
        		continue;
        	}
        	if (!line.isEmpty()) {
        		String featureValues[] = line.split(" ");
        		bw.write(featureValues[instanceIdIndex] + "=" + featureValues[predictedOutcomeIndex]
        				+ " (is " + featureValues[outcomeIndex] + ")\n");
        		bw.flush();
        	}
        }
        br.close();
        bw.close();
    }
}