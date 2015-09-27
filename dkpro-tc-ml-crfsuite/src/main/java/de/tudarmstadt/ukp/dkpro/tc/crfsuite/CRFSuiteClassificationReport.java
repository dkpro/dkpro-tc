/**
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;

public class CRFSuiteClassificationReport
    extends ReportBase
    implements Constants
{

    @Override
    public void execute()
        throws Exception
    {
        // only a mock for now - this needs to be rewritten anyway once the evaluation module is
        // ready
    	File evalFolder = getContext().getFolder(TEST_TASK_OUTPUT_KEY,
                AccessMode.READWRITE);
    	String evalFileName = CRFSuiteAdapter.getInstance().getFrameworkFilename(
                AdapterNameEntries.evaluationFile);
        File evalFile = new File(evalFolder,evalFileName);

        Properties props = new Properties();
        for (String line : FileUtils.readLines(evalFile)) {
            String[] parts = line.split("=");
            props.setProperty(parts[0], parts[1]);
        }

        // Write out properties
        getContext().storeBinary(Constants.RESULTS_FILENAME, new PropertiesAdapter(props));

    }
}
