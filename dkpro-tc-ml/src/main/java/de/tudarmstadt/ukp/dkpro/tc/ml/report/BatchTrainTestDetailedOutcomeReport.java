/*******************************************************************************
 * Copyright 2015
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.ContextMetaCollectorUtil;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;

/**
 * Collects the final prediction results for each Unit and
 * writes them to a new, while optionally including the
 * context. Requires the @link{ TestContextMetaInfoTask} to have been run at first.
 * 
 */
public class BatchTrainTestDetailedOutcomeReport
    extends BatchReportBase
    implements Constants
{
	/**
     * Character that is used for separating fields in the output file
     */
    public static final String SEPARATOR_CHAR = ";";		// Note: this should match the identical constant in WekaOutcomeIDReport; it is replicated here, though, to avoid a dependency on the ML-WEKA module
        
    private static final String TEST_TASK_ID = "TestTask";
    private static final String METAINFO_TASK_ID = "MetaInfoTask";
    private static final boolean extractContext = true;		// TODO MW: Externalize to parameter, if or when reports are provided as instances instead of classes
	private static final String OUTPUTFILE_HEADER = "#ID=PREDICTION;GOLDSTANDARD;PREDICTION CORRECT Y/N; TEXT OF TC UNIT (optionally with context)";

    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        
        Map<String, String> predictionsMap = null;
        Map<String, String> contextMap = null;
        
        for (TaskContextMetadata subcontext : getSubtasks()) {
            // FIXME this is a bad hack
        	String type = subcontext.getType();
            if (type.contains(TEST_TASK_ID)) {
                try {
                    predictionsMap = store.retrieveBinary(subcontext.getId(),
                    		Constants.ID_OUTCOME_KEY, new PropertiesAdapter()).getMap();
                }
                catch (Exception e) {
                    getContext().error("Error while trying to read predictions from test task file.", e);
                }
            }
            else if(type.contains(METAINFO_TASK_ID)) {
            	try {
                	File metaDirectory = store.getStorageFolder(subcontext.getId(), MetaInfoTask.META_KEY);
                	contextMap = parseContextFile(metaDirectory, extractContext);
            	}
            	catch (Exception e) {
                    getContext().error("Error while trying to read predictions from test task file.", e);
                }
            }
        }

        // output the location of the batch evaluation folder
        // otherwise it might be hard for novice users to locate this
        File dummyFolder = store.getStorageFolder(getContext().getId(), "dummy");
        // TODO can we also do this without creating and deleting the dummy folder?
        getContext().getLoggingService().message(getContextLabel(),
                "Storing detailed classification results in:\n" + dummyFolder.getParent() + "\n");
        dummyFolder.delete();
        
        // Build the output result and write to file
        Properties props = getProperties(predictionsMap, contextMap);
        getContext().storeBinary(Constants.ID_DETAILED_OUTCOME_KEY,
                new PropertiesAdapter(props, OUTPUTFILE_HEADER));
        
    }

    private Properties getProperties(Map<String, String> predictionsMap, Map<String, String> contextMap)
    {
    	Properties props = new Properties();
    	
    	for(String key : predictionsMap.keySet()) {
    		if(! contextMap.containsKey(key))		// this is required because contexts for both train and test runs are stored in the same file
    			continue;
    		
    		String value = "";
    		
    		String predictions = predictionsMap.get(key);
    		
    		value += predictions + SEPARATOR_CHAR;
    		value += predictionsMatch(predictions) ? "Y" : "N";
    		value += SEPARATOR_CHAR;
    		value += contextMap.get(key) + SEPARATOR_CHAR;
    		
    		props.put(key, value);
    	}
    	
		return props;
	}

	private boolean predictionsMatch(String predictions)
	{
		String[] s = predictions.split(SEPARATOR_CHAR);
		
		if(s.length != 2)
			return false; 	// this should not happen
		
		if(s[0].equals(s[1]))
			return true;
		else
			return false;
	}

	/**
     * Extracts the ID and text from the context file and puts the data into a map.
     * Optionally, the context strings can be removed from the text string.
     * 
     * @param metaDirectory The directory where the context file is located.
     * @param extractcontext Whether or not the context should be removed
     * @return
     */
	private Map<String, String> parseContextFile(File metaDirectory, boolean extractcontext) {
		Map<String, String> result = new HashMap<>();
		
		try {
			File contextFile = new File(metaDirectory.getPath() + File.separator + Constants.ID_CONTEXT_KEY);
			List<String> lines = FileUtils.readLines(contextFile, StandardCharsets.UTF_8.toString());
			
			for(String line : lines) {
				int firstTabPos = line.indexOf(ContextMetaCollectorUtil.ID_CONTEXT_DELIMITER);		// context file is tab delimited, but there might be leading tabs in the text string, so we don't use split() here
				
				String id = line.substring(0, firstTabPos);
				String textWithContext = line.substring(firstTabPos + 1);
				
				String text = "";
				if(! extractcontext)
					text = removeContext(textWithContext);
				else
					text = textWithContext;
				
				result.put(id, text);
			}
			
		} catch (IOException e) {
			getContext().error("Error while trying to read context file from path " + metaDirectory.getAbsolutePath(), e);
		}
		
		return result;
	}

	/**
	 * Strips the context from the text string.
	 */
	private String removeContext(String textWithContext) {
		String textWithoutContext = "";
		
		int leftSeparatorPos = textWithContext.indexOf(ContextMetaCollectorUtil.LEFT_CONTEXT_SEPARATOR);
		int rightSeparatorPos = textWithContext.indexOf(ContextMetaCollectorUtil.RIGHT_CONTEXT_SEPARATOR);
		
		textWithoutContext = textWithContext.substring(leftSeparatorPos + ContextMetaCollectorUtil.LEFT_CONTEXT_SEPARATOR.length(), rightSeparatorPos);
		
		return textWithoutContext;
	}
}