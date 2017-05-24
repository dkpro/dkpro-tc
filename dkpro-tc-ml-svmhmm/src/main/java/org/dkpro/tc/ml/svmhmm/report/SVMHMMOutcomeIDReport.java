/*
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
 */

package org.dkpro.tc.ml.svmhmm.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections.BidiMap;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.ml.report.util.SortedKeyProperties;
import org.dkpro.tc.ml.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.ml.svmhmm.util.SVMHMMUtils;

public class SVMHMMOutcomeIDReport
    extends ReportBase
    implements Constants
{

    protected List<String> goldLabels;

    protected List<String> predictedLabels;

    public static final String SEPARATOR_CHAR = ";";

    /*
     * Dummy value as threshold which is expected by the evaluation module but not created/needed by
     * SvmHmm
     */
    private static final String THRESHOLD_DUMMY_CONSTANT = "-1";
    
    public SVMHMMOutcomeIDReport(){
        //required by groovy
    }

    /**
     * Returns the current test file
     *
     * @return test file
     */
    protected File locateTestFile()
    {
        // test file with gold labels
        File testDataStorage = getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA,
                StorageService.AccessMode.READONLY);
        String fileName = new SVMHMMAdapter().getFrameworkFilename(
                TcShallowLearningAdapter.AdapterNameEntries.featureVectorsFile);
        return new File(testDataStorage, fileName);
    }

    /**
     * Loads gold labels and predicted labels
     *
     * @throws IOException
     */
    protected void loadGoldAndPredictedLabels()
        throws IOException
    {
        // predictions
        String predictionFileName = new SVMHMMAdapter()
                .getFrameworkFilename(TcShallowLearningAdapter.AdapterNameEntries.predictionsFile);
        
        File predictionsFile = getContext().getFile(predictionFileName, AccessMode.READONLY);

        // test file with gold labels
        File testFile = locateTestFile();

        // load the mappings from labels to integers
        File mappingFile = getContext().getFile(SVMHMMUtils.LABELS_TO_INTEGERS_MAPPING_FILE_NAME, AccessMode.READONLY);
        BidiMap labelsToIntegersMapping = SVMHMMUtils.loadMapping(mappingFile);

        // gold label tags
        goldLabels = SVMHMMUtils.extractOutcomeLabels(testFile);

        // predicted tags
        predictedLabels = SVMHMMUtils.extractOutcomeLabelsFromPredictions(predictionsFile,
                labelsToIntegersMapping);

        // sanity check
        if (goldLabels.size() != predictedLabels.size()) {
            throw new IllegalStateException("Gold labels and predicted labels differ in size!");
        }
    }

    @Override
    public void execute()
        throws Exception
    {
        // load gold and predicted labels
        loadGoldAndPredictedLabels();

        File testFile = locateTestFile();

        // original tokens
        List<String> originalTokens = SVMHMMUtils.extractOriginalTokens(testFile);

        // sequence IDs
        List<Integer> sequenceIDs = SVMHMMUtils.extractOriginalSequenceIDs(testFile);

        // sanity check
        if (goldLabels.size() != originalTokens.size() || goldLabels.size() != sequenceIDs.size()) {
            throw new IllegalStateException(
                    "Gold labels, original tokens or sequenceIDs differ in size!");
        }

        File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
        File evaluationFile = new File(evaluationFolder, ID_OUTCOME_KEY);

        File mappingFile = getContext().getFile(SVMHMMUtils.LABELS_TO_INTEGERS_MAPPING_FILE_NAME,
                AccessMode.READONLY);
        BidiMap id2label = SVMHMMUtils.loadMapping(mappingFile);

        String header = buildHeader(id2label);

        Properties prop = new SortedKeyProperties();
        BidiMap label2id = id2label.inverseBidiMap();

        for (int idx = 0; idx < goldLabels.size(); idx++) {
            String gold = goldLabels.get(idx);
            String pred = predictedLabels.get(idx);
            int g = (int) label2id.getKey(gold);
            int p = (int) label2id.getKey(pred);

            // we decrement all gold/pred labels by one because the evaluation modules seems to
            // expect that the numbering starts with 0 which is seemingly a problem for SVMHMM -
            // thus we decrement all labels and shifting the entire outcome numbering by one
            g--;
            p--;

            prop.setProperty("" + String.format("%05d",idx),
                    p + SEPARATOR_CHAR + g + SEPARATOR_CHAR + THRESHOLD_DUMMY_CONSTANT);
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(evaluationFile), "utf-8");
        prop.store(osw, header);
        osw.close();
    }

    private String buildHeader(BidiMap label2id)
        throws UnsupportedEncodingException
    {
        StringBuilder sb = new StringBuilder();

        sb.append("ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD" + SEPARATOR_CHAR + "THRESHOLD"
                + "\n" + "labels" + " ");

        @SuppressWarnings("unchecked")
        List<String> keySet = new ArrayList<>(label2id.keySet());
        for (int i = 0; i < keySet.size(); i++) {
            String key = keySet.get(i);
            Integer id = (Integer) label2id.get(key);
            id--; // SvmHmm starts label numbering at 1 - we need a label numbering starting with
                  // zero i.e. expected by the evaluation module
            sb.append(id + "=" + URLEncoder.encode(key, "UTF-8"));
            if (i + 1 < keySet.size()) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

}
