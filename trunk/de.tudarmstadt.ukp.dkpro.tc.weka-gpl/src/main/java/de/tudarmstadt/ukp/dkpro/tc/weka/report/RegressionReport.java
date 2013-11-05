package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;

/**
 * Simple report for regression problems
 * 
 * @author Oliver Ferschke
 * @author daxenberger
 * 
 */
public class RegressionReport
    extends ReportBase
{

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(TestTask.OUTPUT_KEY, AccessMode.READONLY);
        Properties props = new Properties();
        File evaluationFile = new File(storage.getAbsolutePath() + "/"
                + TestTask.EVALUATION_DATA_KEY);

        weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
                .read(evaluationFile.getAbsolutePath());
        HashMap<String, Double> m = new HashMap<String, Double>();

        m.put("correlation coefficient", eval.correlationCoefficient());
        m.put("mean absolute error", eval.meanAbsoluteError());
        m.put("root mean squared error", eval.rootMeanSquaredError());
        m.put("root relative squared error", eval.rootRelativeSquaredError());
        m.put("relative absolute error", eval.relativeAbsoluteError());

        for (String s : m.keySet()) {
            props.setProperty(s, m.get(s).toString());
        }

        // Write out properties
        getContext().storeBinary(TestTask.RESULTS_KEY, new PropertiesAdapter(props));
    }
}