/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.mallet.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.tudarmstadt.ukp.dkpro.lab.reporting.FlexTable;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.mallet.task.TestTask;

/**
 * Report that computes evaluation results given the classification results.
 */
public class ClassificationReport
	extends ReportBase
{

	List<String> actualLabelsList = new ArrayList<String>();
	List<String> predictedLabelsList = new ArrayList<String>();
	// in ML mode, holds a map for building the Label Power Set over all label actuals/predictions
	HashMap<String, Map<String, Integer>> tempM = new HashMap<String, Map<String, Integer>>();
	// holds overall CV results
	Map<String, Double> results = new HashMap<String, Double>();
	// holds PR curve data
	List<double[][]> prcData = new ArrayList<double[][]>();

	@Override
	public void execute() throws Exception
	{
		File storage = getContext().getStorageLocation(TestTask.OUTPUT_KEY, AccessMode.READONLY);

		Properties props = new Properties();
		// table to hold CM results
		FlexTable<String> cMTable = FlexTable.forClass(String.class);
		cMTable.setSortRows(false);

		File evaluationFile = new File(storage.getAbsolutePath() + "/" + TestTask.EVALUATION_DATA_KEY);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(evaluationFile), "UTF-8"));
		String line = null;
		boolean header = false;
		while ((line = br.readLine()) != null) {
			if (!header) {
				header = true;
				continue;
			}
			String[] fields = line.split(",");
			results.put(fields[0], Double.parseDouble(fields[1]));
		}
		br.close();

		for (String s : results.keySet()) {
			getContext().getLoggingService().message(getContextLabel(),
					s + " - " + results.get(s));
			props.setProperty(s, results.get(s).toString());
		}

		// Write out properties
		getContext().storeBinary(TestTask.RESULTS_KEY, new PropertiesAdapter(props));
//		getContext().storeBinary(ClassificationReport.CONFUSIONMATRIX_KEY, cMTable.getCsvWriter());

	}
}