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
package org.dkpro.tc.ml.report.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dkpro.tc.core.Constants;

public class ID2OutcomeCombiner<T> {

	List<String> names;
	List<Double> thresholds;

	List<List<T>> gold;
	List<List<T>> prediction;
	private String mode;

	Set<String> uniqueNames = new HashSet<>();
	Map<String,String> multilabelIndexMapping;

	public ID2OutcomeCombiner(String mode) {
		this.mode = mode;
		names = new ArrayList<>();
		thresholds = new ArrayList<>();
		gold = new ArrayList<>();
		prediction = new ArrayList<>();

		uniqueNames = new HashSet<>();
		multilabelIndexMapping = new HashMap<>();
	}

	public void add(File id2OutcomeFile, String mode) throws Exception {
		if (!this.mode.equals(mode)) {
			throw new IllegalArgumentException("This aggregator was initilized to process values created during ["
					+ this.mode + "] but received now an id2outcome file created during [" + mode + "] ");
		}

		switch (mode) {
		case Constants.LM_SINGLE_LABEL:
			processSingleLabel(id2OutcomeFile);
			break;
		case Constants.LM_REGRESSION:
			processRegression(id2OutcomeFile);			
			break;
		case Constants.LM_MULTI_LABEL:
			processMultilabel(id2OutcomeFile);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown learning mode ["+mode+"]");
		}

	}

	@SuppressWarnings("unchecked")
	private void processRegression(File id2OutcomeFile) throws Exception {

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(id2OutcomeFile), "utf-8"));
		reader.readLine(); // pop first line

		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			String[] split = line.split("=");
			String docName = split[0];
			String values = split[1];

			String[] valSplit = values.split(";");
			String p = valSplit[0];
			String g = valSplit[1];
			String threshold = valSplit[2];
			
			uniqueNames.add(p);
			uniqueNames.add(g);
			
			names.add(docName);
			thresholds.add(Double.valueOf(threshold));

			List<T> pl = new ArrayList<>();
			pl.add((T) p);
			prediction.add(pl);

			List<T> gl = new ArrayList<>();
			gl.add((T) g);
			gold.add(gl);

		}
		reader.close();
	}

	@SuppressWarnings("unchecked")
	private void processSingleLabel(File id2OutcomeFile) throws Exception {

		// the order of labels might be different depending on the fold, this
		// map contains a mapping to harmonize the numbering of the label
		// (order) in the single fold
		// e.g. label id 3 might be C in the first fold but become 2 in the
		// second one because there is one label less

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(id2OutcomeFile), "utf-8"));
		reader.readLine(); // pop first line
		Map<String, String> map = buildMappingFromHeader(reader.readLine());
		

		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			String[] split = line.split("=");
			String docName = split[0];
			String values = split[1];

			String[] valSplit = values.split(";");
			String p = map.get(valSplit[0]);
			String g = map.get(valSplit[1]);
			String threshold = valSplit[2];
			
			uniqueNames.add(p);
			uniqueNames.add(g);
			
			names.add(docName);
			thresholds.add(Double.valueOf(threshold));

			List<T> pl = new ArrayList<>();
			pl.add((T) p);
			prediction.add(pl);

			List<T> gl = new ArrayList<>();
			gl.add((T) g);
			gold.add(gl);

		}
		reader.close();
		
	}
	
	@SuppressWarnings("unchecked")
	private void processMultilabel(File id2OutcomeFile) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(id2OutcomeFile), "utf-8"));
		reader.readLine(); // pop first line
		
		Map<String, String> map = buildMappingFromHeader(reader.readLine());
		multilabelIndexMapping.putAll(map);
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			String[] split = line.split("=");
			String docName = split[0];
			String values = split[1];

			String[] valSplit = values.split(";");
			String [] p = valSplit[0].split(",");
			String [] g = valSplit[1].split(",");
			String threshold = valSplit[2];
			
			names.add(docName);
			thresholds.add(Double.valueOf(threshold));

			List<T> pl = new ArrayList<>();
			for(String x : p){
				pl.add((T)x);
			}
			prediction.add(pl);

			List<T> gl = new ArrayList<>();
			for(String x : g){
				gl.add((T)x);
			}
			gold.add(gl);

		}
		
		reader.close();
	}

	private static Map<String, String> buildMappingFromHeader(String header) {

		header = header.replaceAll("#labels", "").trim();

		Map<String, String> map = new HashMap<>();

		String[] split = header.split(" ");
		for (String entry : split) {
			int indexOf = entry.indexOf("=");
			String key = entry.substring(0, indexOf).trim();
			String val = entry.substring(indexOf + 1).trim();
			map.put(key, val);
		}

		return map;
	}

	public String generateId2OutcomeFile() {
		
		Map<String, Integer> map = new HashMap<>();
		int id=0;
		
		for(String l : uniqueNames){
			map.put(l, id++);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD\n");
		
		switch(mode){
		
		case Constants.LM_SINGLE_LABEL:
			String header = buildHeader(map);
			sb.append(header+"\n");
			
			for(int i=0; i < names.size(); i++){
				sb.append(names.get(i) + "=" + map.get(prediction.get(i).get(0)) + ";" + map.get(gold.get(i).get(0))
						+ ";" + thresholds.get(i) + "\n");
			}
			break;
			
		case Constants.LM_REGRESSION:
			sb.append("#labels\n");
			
			for (int i = 0; i < names.size(); i++) {
				sb.append(names.get(i) + "=" + prediction.get(i).get(0) + ";" + gold.get(i).get(0) + ";"
						+ thresholds.get(i) + "\n");
			}
			break;
			
		case Constants.LM_MULTI_LABEL:
			sb.append("#labels");
			List<String> keySet = new ArrayList<String>(multilabelIndexMapping.keySet());
			for(int i=0; i < keySet.size(); i++){
				String s = keySet.get(i);
				sb.append(s+"=" + multilabelIndexMapping.get(s));
				if(i+1 < keySet.size()){
					sb.append(" ");
				}
			}
			sb.append("\n");
			
			for (int i = 0; i < names.size(); i++) {
				sb.append(names.get(i) + "=");
				
				for(int j=0; j < prediction.get(i).size(); j++){
					T t = prediction.get(i).get(j);
					sb.append(t);
					if(j+1  < prediction.get(i).size()){
						sb.append(",");
					}
				}
				sb.append(";");
				
				for(int j=0; j < gold.get(i).size(); j++){
					T t = gold.get(i).get(j);
					sb.append(t);
					if(j+1  < gold.get(i).size()){
						sb.append(",");
					}
				}
				sb.append(";");

				sb.append(thresholds.get(i));
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	private String buildHeader(Map<String, Integer> map) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("#labels ");
		for(String key : map.keySet()){
			sb.append(map.get(key) + "=" + key + " ");
		}
		
		return sb.toString().trim();
	}

}
