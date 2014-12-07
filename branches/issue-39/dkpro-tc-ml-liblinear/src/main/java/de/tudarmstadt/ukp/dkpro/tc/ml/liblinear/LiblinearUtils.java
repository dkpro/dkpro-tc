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
package de.tudarmstadt.ukp.dkpro.tc.ml.liblinear;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

public class LiblinearUtils {

	public static String outcomeMap2String(Map<String, Integer> map) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> entry : map.entrySet()) {
			sb.append(entry.getKey());
			sb.append("\t");
			sb.append(entry.getValue());
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	public static Map<String, Integer> string2outcomeMap(String s) {
		Map<String, Integer> outcomeMap = new HashMap<String, Integer>();
		for (String line : s.split("\n")) {
			String[] parts = line.split("\t");
			outcomeMap.put(parts[0], Integer.parseInt(parts[1]));
		}
		
		return outcomeMap;
	}
	
	public static void savePredictions(File outputFile, List<Double> predictions) 
			throws IOException
	{
		StringBuilder sb = new StringBuilder();
		for (Double prediction : predictions) {
			sb.append(prediction);
			sb.append("\n");
		}
		FileUtils.writeStringToFile(outputFile, sb.toString());
	}
}