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
package de.tudarmstadt.ukp.dkpro.tc.ml.report;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.StringAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;


/**
 * 
 * Collects statistical evaluation results from CV BatchTasks. Needs to be run on top level of CV setups.
 *
 * @author Johannes Daxenberger
 *
 */
public class BatchStatisticsCVReport
    extends BatchReportBase
    implements Constants
{

    @Override
    public void execute()
        throws Exception
    {
        StringWriter sWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(sWriter, ';');
        
        boolean experimentHasBaseline = false;
        HashSet<String> variableClassifier = new HashSet<String>();
        HashSet<String> variableFeature = new HashSet<String>();

        for (TaskContextMetadata subcontext : getSubtasks()) {
        	// FIXME this is a really bad hack
            if (subcontext.getType().contains("$1")) {
            	String csvText = getContext().getStorageService().retrieveBinary(subcontext.getId(), 
            			STATISTICS_REPORT_FILENAME, new StringAdapter()).getString();
                CSVReader csvReader = new CSVReader(new StringReader(csvText), ';');
            	for (String[] line : csvReader.readAll()) {
                    if (!experimentHasBaseline) {
                        experimentHasBaseline = Integer.parseInt(line[6]) == 1;
                    }
                    variableClassifier.add(line[2]);
                    variableFeature.add(line[3]);
            		csvWriter.writeNext(line);
				}
                csvReader.close();
            }
        }

        String s = sWriter.toString();
        csvWriter.close();
        if (variableClassifier.size() > 1 && variableFeature.size() > 1 && experimentHasBaseline) {
            throw new TextClassificationException("If you configure a baseline, you may test either only one classifier (arguments) or one feature set (arguments).");
        }
        getContext().storeBinary(STATISTICS_REPORT_FILENAME, new StringAdapter(s));
    }
}