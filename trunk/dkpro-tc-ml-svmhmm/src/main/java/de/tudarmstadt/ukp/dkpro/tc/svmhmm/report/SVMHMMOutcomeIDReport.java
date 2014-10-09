/*
 * Copyright 2014
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

package de.tudarmstadt.ukp.dkpro.tc.svmhmm.report;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.SVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.SVMHMMUtils;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class SVMHMMOutcomeIDReport
        extends ReportBase
        implements Constants
{

    protected List<String> goldLabels;

    protected List<String> predictedLabels;

    /**
     * Returns the current test file
     *
     * @return test file
     */
    protected File locateTestFile()
    {
        // test file with gold labels
        File testDataStorage = getContext().getStorageLocation(TEST_TASK_INPUT_KEY_TEST_DATA,
                StorageService.AccessMode.READONLY);
        String fileName = new SVMHMMAdapter().getFrameworkFilename(
                TCMachineLearningAdapter.AdapterNameEntries.featureVectorsFile);
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
        File predictionsFile = new File(getContext().getStorageLocation(TEST_TASK_OUTPUT_KEY,
                StorageService.AccessMode.READONLY), new SVMHMMAdapter().getFrameworkFilename(
                TCMachineLearningAdapter.AdapterNameEntries.predictionsFile));

        // test file with gold labels
        File testFile = locateTestFile();

        // load the mappings from labels to integers
        File mappingFile = new File(getContext().getStorageLocation(TEST_TASK_OUTPUT_KEY,
                StorageService.AccessMode.READWRITE),
                SVMHMMUtils.LABELS_TO_INTEGERS_MAPPING_FILE_NAME);
        BidiMap labelsToIntegersMapping = SVMHMMUtils.loadMapping(mappingFile);

        // gold label tags
        goldLabels = SVMHMMUtils.extractOutcomeLabels(testFile);

        // predicted tags
        predictedLabels = SVMHMMUtils
                .extractOutcomeLabelsFromPredictions(predictionsFile, labelsToIntegersMapping);

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
        if (goldLabels.size() != originalTokens.size() ||
                goldLabels.size() != sequenceIDs.size()) {
            throw new IllegalStateException(
                    "Gold labels, original tokens or sequenceIDs differ in size!");
        }

        File evaluationFile = new File(getContext().getStorageLocation(TEST_TASK_OUTPUT_KEY,
                StorageService.AccessMode.READWRITE), SVMHMMUtils.GOLD_PREDICTED_OUTCOMES_CSV);

        // write results into CSV
        // form: gold;predicted;token;seqID

        CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(evaluationFile), SVMHMMUtils.CSV_FORMAT);
        csvPrinter.printComment(SVMHMMUtils.CSV_COMMENT);

        for (int i = 0; i < goldLabels.size(); i++) {
            csvPrinter.printRecord(goldLabels.get(i), predictedLabels.get(i), originalTokens.get(i),
                    sequenceIDs.get(i).toString());
        }

        IOUtils.closeQuietly(csvPrinter);
    }
}
