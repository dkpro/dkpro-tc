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

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.ConfusionMatrix;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.SVMHMMUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class SVMHMMBatchCrossValidationReport
        extends BatchReportBase
{
    static Log log = LogFactory.getLog(SVMHMMBatchCrossValidationReport.class);

    @Override public void execute()
            throws Exception
    {

        StorageService storageService = getContext().getStorageService();

        // aggregate rows from all CSVs from all folds
        List<List<String>> allOutcomes = new ArrayList<>();

        // iterate over all sub tasks
        for (TaskContextMetadata subContext : getSubtasks()) {
            // but only test tasks are important
            if (subContext.getLabel().startsWith(SVMHMMTestTask.class.getSimpleName())) {

                // locate CSV file with outcomes (gold, predicted, token, etc.)
                File csvFile = storageService.getStorageFolder(subContext.getId(),
                        Constants.TEST_TASK_OUTPUT_KEY + File.separator
                                + SVMHMMUtils.GOLD_PREDICTED_OUTCOMES_CSV);

                // load the CSV
                CSVParser csvParser = new CSVParser(new FileReader(csvFile),
                        CSVFormat.DEFAULT.withCommentMarker('#'));

                // and add the all rows
                for (CSVRecord csvRecord : csvParser) {
                    // row for particular instance
                    List<String> row = new ArrayList<>();
                    for (String value : csvRecord) {
                        row.add(value);
                    }
                    allOutcomes.add(row);
                }

                IOUtils.closeQuietly(csvParser);
            }
        }

        // store aggregated outcomes again to CSV
        File evaluationFile = new File(
                getContext().getStorageLocation(Constants.TEST_TASK_OUTPUT_KEY,
                        StorageService.AccessMode.READWRITE),
                SVMHMMUtils.GOLD_PREDICTED_OUTCOMES_CSV);
        log.debug("Evaluation file: " + evaluationFile.getAbsolutePath());

        CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(evaluationFile),
                SVMHMMUtils.CSV_FORMAT);
        csvPrinter.printComment(SVMHMMUtils.CSV_COMMENT);
        csvPrinter.printRecords(allOutcomes);
        IOUtils.closeQuietly(csvPrinter);

        // compute confusion matrix
        ConfusionMatrix cm = new ConfusionMatrix();

        for (List<String> singleInstanceOutcomeRow : allOutcomes) {
            // first item is the gold label
            String gold = singleInstanceOutcomeRow.get(0);
            // second item is the predicted label
            String predicted = singleInstanceOutcomeRow.get(1);

            cm.increaseValue(gold, predicted);
        }

        // and write all reports
        SVMHMMUtils.writeOutputResults(getContext(), cm);

        // and print detailed results
        log.info(cm.printNiceResults());
        log.info(cm.printLabelPrecRecFm());
    }
}
