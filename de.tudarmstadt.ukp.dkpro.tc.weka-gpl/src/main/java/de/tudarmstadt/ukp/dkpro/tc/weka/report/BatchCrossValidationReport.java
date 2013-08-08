package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.reporting.FlexTable;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.Task;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;

public class BatchCrossValidationReport
    extends BatchReportBase
{
    private static final String EVALUATION_FILE_XLS = "eval.xls";
    private static final String EVALUATION_FILE_CSV = "eval.csv";

    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        FlexTable<String> table = FlexTable.forClass(String.class);

        Map<String, List<Double>> key2resultValues = new HashMap<String, List<Double>>();

        for (TaskContextMetadata subcontext : getSubtasks()) {
            String name = BatchTask.class.getSimpleName() + "CrossValidation";
            if (subcontext.getLabel().startsWith(name
                    )) {
                Map<String, String> discriminatorsMap = store.retrieveBinary(subcontext.getId(),
                        Task.DISCRIMINATORS_KEY, new PropertiesAdapter()).getMap();

                File eval = store.getStorageFolder(subcontext.getId(),
                        BatchTrainTestReport.EVALUATION_FILE_CSV);
                Map<String, String> resultMap = new HashMap<String, String>();

                String[][] evalMatrix = null;

                int i = 0;
                for (String line : FileUtils.readLines(eval)) {
                    String[] tokenizedLine = StrTokenizer.getCSVInstance(line).getTokenArray();
                    if (evalMatrix == null) {
                        evalMatrix = new String[FileUtils.readLines(eval)
                                .size()][tokenizedLine.length];
                    }
                    evalMatrix[i] = tokenizedLine;
                    i++;
                }

                // columns
                for (int j = 0; j < evalMatrix[0].length; j++) {
                    String header = evalMatrix[0][j];
                    double[] vals = new double[evalMatrix.length - 1];
                    // rows
                    for (int k = 1; k < evalMatrix.length; k++) {
                        try {
                            vals[k - 1] = Double.parseDouble(evalMatrix[k][j]);
                        }
                        catch (NumberFormatException e) {
                            // skip non-numeric lines
                            vals = null;
                            break;
                        }
                    }
                    Mean mean = new Mean();
                    StandardDeviation std = new StandardDeviation();
                    if (vals != null) {
                        resultMap.put(
                                header,
                                String.valueOf(mean.evaluate(vals) + "\u00B1"
                                        + String.valueOf(std.evaluate(vals))));
                    }
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
                values.putAll(discriminatorsMap);
                values.putAll(resultMap);

                table.addRow(subcontext.getLabel(), values);
            }
        }

        table.setCompact(false);
        getContext().storeBinary(EVALUATION_FILE_XLS, table.getExcelWriter());
        getContext().storeBinary(EVALUATION_FILE_CSV, table.getCsvWriter());
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
