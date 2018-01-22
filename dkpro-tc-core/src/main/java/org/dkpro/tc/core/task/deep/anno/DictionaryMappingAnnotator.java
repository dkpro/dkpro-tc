/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.core.task.deep.anno;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.DeepLearningConstants;

public class DictionaryMappingAnnotator
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_TARGET_DIRECTORY = "targetDirectory";
    @ConfigurationParameter(name = PARAM_TARGET_DIRECTORY, mandatory = true)
    protected File targetFolder;
    
    /**
	 * Path to the dictionaries that shall be loaded. The expected format is
	 * word TAB VAL, the word is substituted with the id from the mapping step.
	 * If it did not occur, a new entry is added
	 */
    public static final String PARAM_DICTIONARY_PATHS = "dictionaryPath";
    @ConfigurationParameter(name = PARAM_DICTIONARY_PATHS, mandatory = true)
    protected String [] dictionaryPath;

    Map<String,Integer> map = new HashMap<>();
    
    int nextId=0;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        try {
			List<String> instanceMappings = FileUtils.readLines(new File(targetFolder, DeepLearningConstants.FILENAME_INSTANCE_MAPPING), "utf-8");
			for(String e : instanceMappings){
				String[] split = e.split("\t");
				
				String val = split[0];
				Integer key = Integer.valueOf(split[1]);
				map.put(val, key);
				
				if(key > nextId){
					nextId = key;
				}
			}
			
			nextId+=1; //next free id
			
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}

    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
    	//do nothing here
    }

    @Override
    public void collectionProcessComplete()
    {
    	for(String dict : dictionaryPath){
    		List<String> mappedDict = processDictionary(dict);
    		writeMappedDictionary(dict, mappedDict);
    	}
    	
    	writeUpdatedInstanceMapping();
    	
    }

	private void writeUpdatedInstanceMapping() {
		
		List<String> mapping = new ArrayList<>();

		List<String> keySet = new ArrayList<>(map.keySet());
		Collections.sort(keySet);

		for (String key : keySet) {
			mapping.add(key + "\t" + map.get(key) + "\n");
		}
		
		try {
			FileUtils.writeLines(new File(targetFolder, DeepLearningConstants.FILENAME_INSTANCE_MAPPING), "utf-8", mapping);
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
		
	}

	private void writeMappedDictionary(String sourceDict, List<String> dict) {
		File file = new File(sourceDict);
		try {
			FileUtils.writeLines(new File(targetFolder, file.getName()), "utf-8", dict);
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}		
	}

	private List<String> processDictionary(String dict) {
		List<String> mappedDict = new ArrayList<>();
		try {
			List<String> readLines = FileUtils.readLines(new File(dict), "utf-8");
			for(String e : readLines){
				int indexOf = e.indexOf("\t");
				String word = e.substring(0, indexOf);
				String rest = e.substring(indexOf+1);
				
				Integer integer = map.get(word);
				if(integer == null){
					integer = nextId++;
				}
				mappedDict.add(integer.toString()+"\t"+rest+"\n");
			}
			
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
		return mappedDict;
	}
}
