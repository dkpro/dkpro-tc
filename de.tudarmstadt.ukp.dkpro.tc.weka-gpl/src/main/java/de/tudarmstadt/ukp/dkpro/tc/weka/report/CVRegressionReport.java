package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.CrossValidationTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.ReportUtils;

/**
 * Simple report for regression problems
 *
 * @author Oliver Ferschke
 *
 */
@Deprecated
public class CVRegressionReport
    extends ReportBase
{

    // holds overall CV results
    Map<String, List<Double>> results = new HashMap<String, List<Double>>();

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(CrossValidationTask.OUTPUT_KEY,
                AccessMode.READONLY);

        Properties props = new Properties();
        for (int n = 0; n < CrossValidationTask.FOLDS; n++) {

            File evaluationFile = new File(storage.getAbsolutePath()
                    + "/"
                    + StringUtils.replace(CrossValidationTask.EVALUATION_DATA_KEY, "#",
                            String.valueOf(n)));

            if (CrossValidationTask.MULTILABEL) {
                // ============= multi-label setup ======================
            }

            // ============= single-label setup ======================
            else {
                weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
                        .read(evaluationFile.getAbsolutePath());
                HashMap<String, Double> m = new HashMap<String, Double>();

                m.put("correlation coefficient", eval.correlationCoefficient());
                m.put("mean absolute error", eval.meanAbsoluteError());
                m.put("root mean squared error", eval.rootMeanSquaredError());
                m.put("root relative squared error", eval.rootRelativeSquaredError());
                m.put("relative absolute error", eval.relativeAbsoluteError());

                ReportUtils.addToResults(m, results);

            }
            // ================================================
        }

        for (String s : results.keySet()) {
            DescriptiveStatistics stats = new DescriptiveStatistics(ArrayUtils.toPrimitive(results
                    .get(s).toArray(new Double[] {})));
            double mean = stats.getMean();
            props.setProperty(s, Double.toString(mean));
        }

        // Write out properties
        getContext().storeBinary(CrossValidationTask.RESULTS_KEY, new PropertiesAdapter(props));
    }
}