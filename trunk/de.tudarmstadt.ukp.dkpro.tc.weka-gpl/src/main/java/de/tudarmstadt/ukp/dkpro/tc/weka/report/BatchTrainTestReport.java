package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.reporting.FlexTable;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.Task;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.ReportUtils;

/**
 * Collects the final evaluation results in a train/test setting.
 * 
 * @author zesch
 * 
 */
public class BatchTrainTestReport
    extends BatchReportBase
    implements Constants
{

    private static final List<String> discriminatorsToExclude = Arrays.asList(new String[] {
            "files_validation", "files_training" });

    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        FlexTable<String> table = FlexTable.forClass(String.class);

        Map<String, List<Double>> key2resultValues = new HashMap<String, List<Double>>();
        Map<List<String>, Double> confMatrixMap = new HashMap<List<String>, Double>();

        Properties outcomeIdProps = new Properties();

        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().startsWith(TestTask.class.getName())) {

                try {
                    outcomeIdProps.putAll(store.retrieveBinary(subcontext.getId(),
                            OutcomeIDReport.ID_OUTCOME_KEY, new PropertiesAdapter()).getMap());
                }
                catch (Exception e) {
                    // silently ignore if this file was not generated
                }

                Map<String, String> discriminatorsMap = store.retrieveBinary(subcontext.getId(),
                        Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();
                Map<String, String> resultMap = store.retrieveBinary(subcontext.getId(),
                        TestTask.RESULTS_FILENAME, new PropertiesAdapter()).getMap();

                File confMatrix = store.getStorageFolder(subcontext.getId(), CONFUSIONMATRIX_KEY);

                if (confMatrix.isFile()) {
                    confMatrixMap = ReportUtils.updateAggregateMatrix(confMatrixMap, confMatrix);
                }

                String key = getKey(discriminatorsMap);

                List<Double> results;
                if (key2resultValues.get(key) == null) {
                    results = new ArrayList<Double>();
                }
                else {
                    results = key2resultValues.get(key);
                }
                key2resultValues.put(key, results);

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
        getContext()
                .storeBinary(EVAL_FILE_NAME + "_compact" + SUFFIX_EXCEL, table.getExcelWriter());
        getContext().storeBinary(EVAL_FILE_NAME + "_compact" + SUFFIX_CSV, table.getCsvWriter());
        table.setCompact(false);
        getContext().storeBinary(EVAL_FILE_NAME + SUFFIX_EXCEL, table.getExcelWriter());
        getContext().storeBinary(EVAL_FILE_NAME + SUFFIX_CSV, table.getCsvWriter());

        // this report is reused in CV, and we only want to aggregate confusion matrices from folds
        // in CV, and an aggregated OutcomeIdReport
        if (getContext().getId().startsWith(BatchTaskCrossValidation.class.getSimpleName())) {
            FlexTable<String> confMatrix = ReportUtils.createOverallConfusionMatrix(confMatrixMap);
            getContext().storeBinary(CONFUSIONMATRIX_KEY, confMatrix.getCsvWriter());
            if (outcomeIdProps.size() > 0)
                getContext().storeBinary(OutcomeIDReport.ID_OUTCOME_KEY,
                        new PropertiesAdapter(outcomeIdProps));
        }

        // output the location of the batch evaluation folder
        // otherwise it might be hard for novice users to locate this
        File dummyFolder = store.getStorageFolder(getContext().getId(), "dummy");
        // TODO can we also do this without creating and deleting the dummy folder?
        getContext().getLoggingService().message(getContextLabel(),
                "Storing detailed results in:\n" + dummyFolder.getParent() + "\n");
        dummyFolder.delete();
    }

    private String getKey(Map<String, String> discriminatorsMap)
    {
        Set<String> sortedDiscriminators = new TreeSet<String>(discriminatorsMap.keySet());

        List<String> values = new ArrayList<String>();
        for (String discriminator : sortedDiscriminators) {
            values.add(discriminatorsMap.get(discriminator));
        }
        return StringUtils.join(values, "_");
    }
}