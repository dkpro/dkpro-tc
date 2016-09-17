/**
 * Copyright 2016
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
package org.dkpro.tc.ml.weka.report;

import static org.dkpro.tc.core.util.ReportConstants.CORRELATION;
import static org.dkpro.tc.core.util.ReportConstants.MEAN_ABSOLUTE_ERROR;
import static org.dkpro.tc.core.util.ReportConstants.RELATIVE_ABSOLUTE_ERROR;
import static org.dkpro.tc.core.util.ReportConstants.ROOT_MEAN_SQUARED_ERROR;
import static org.dkpro.tc.core.util.ReportConstants.ROOT_RELATIVE_SQUARED_ERROR;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.tc.ml.weka.task.WekaTestTask;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import weka.core.SerializationHelper;

/**
 * Simple report for regression problems
 */
@Deprecated
public class WekaRegressionReport
    extends ReportBase
{

    @Override
    public void execute()
        throws Exception
    {
        File evaluationFile = WekaUtils.getFile(getContext(), "", WekaTestTask.evaluationBin, AccessMode.READONLY);
        
        weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
                .read(evaluationFile.getAbsolutePath());
        HashMap<String, Double> m = new HashMap<String, Double>();

        m.put(CORRELATION, eval.correlationCoefficient());
        m.put(MEAN_ABSOLUTE_ERROR, eval.meanAbsoluteError());
        m.put(RELATIVE_ABSOLUTE_ERROR, eval.relativeAbsoluteError());
        m.put(ROOT_MEAN_SQUARED_ERROR, eval.rootMeanSquaredError());
        m.put(ROOT_RELATIVE_SQUARED_ERROR, eval.rootRelativeSquaredError());

        Properties props = new Properties();
        for (String s : m.keySet()) {
            props.setProperty(s, m.get(s).toString());
        }

        // Write out properties
        getContext().storeBinary("regression.txt", new PropertiesAdapter(props));
    }
}