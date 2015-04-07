/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.CORRELATION;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.MEAN_ABSOLUTE_ERROR;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.RELATIVE_ABSOLUTE_ERROR;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.ROOT_MEAN_SQUARED_ERROR;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.ROOT_RELATIVE_SQUARED_ERROR;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaTestTask;

/**
 * Simple report for regression problems
 * 
 * @author Oliver Ferschke
 * @author daxenberger
 * 
 */
public class WekaRegressionReport
    extends ReportBase
{

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(WekaTestTask.TEST_TASK_OUTPUT_KEY, AccessMode.READONLY);
        Properties props = new Properties();
        File evaluationFile = new File(storage.getAbsolutePath() + "/"
                + WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.evaluationFile));

        weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
                .read(evaluationFile.getAbsolutePath());
        HashMap<String, Double> m = new HashMap<String, Double>();

        m.put(CORRELATION, eval.correlationCoefficient());
        m.put(MEAN_ABSOLUTE_ERROR, eval.meanAbsoluteError());
        m.put(RELATIVE_ABSOLUTE_ERROR, eval.relativeAbsoluteError());
        m.put(ROOT_MEAN_SQUARED_ERROR, eval.rootMeanSquaredError());
        m.put(ROOT_RELATIVE_SQUARED_ERROR, eval.rootRelativeSquaredError());

        for (String s : m.keySet()) {
            props.setProperty(s, m.get(s).toString());
        }

        // Write out properties
        getContext().storeBinary(WekaTestTask.RESULTS_FILENAME, new PropertiesAdapter(props));
    }
}