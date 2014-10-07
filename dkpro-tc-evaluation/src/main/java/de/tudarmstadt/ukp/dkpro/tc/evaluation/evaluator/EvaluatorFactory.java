/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit�t Darmstadt
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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.multi.MultiEvaluator;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.regression.RegressionEvaluator;
import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.single.SingleEvaluator;


/**
 * @author Andriy Nadolskyy
 * 
 */
public class EvaluatorFactory {
	
	private File file;
	private EvaluationMode mode;
	protected HashMap<String, Integer> class2number;
	protected LinkedList<String> readData;
	
	
	public enum EvaluationMode{
		SINGLE, MULTI, REGRESSION
	}
	
	/***
	 * 
	 * @param file - file containing data are to be evaluated
	 * @param mode 
	 */
	public EvaluatorFactory(File file, EvaluationMode mode){
		this.file = file;
		this.mode = mode;
	}
	
	public EvaluatorBase makeEvaluator(){
		EvaluatorBase evaluator = null;
		switch (mode) {
			case SINGLE:
				evaluator = new SingleEvaluator(class2number, readData);
				break;
			case MULTI:
				evaluator = new MultiEvaluator(class2number, readData);
				break;
			case REGRESSION:
				evaluator = new RegressionEvaluator(class2number, readData);
				break;
		}		
		return evaluator;
	}
	
	/**
	 * read evaluation relevant data from file
	 * 
	 * @throws IOException
	 */
	public void readDataFile() throws IOException{
		class2number = new HashMap<String, Integer>();
		readData = new LinkedList<String>();
		
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		String line = ""; 
		while ((line = br.readLine()) != null){
			if (line.startsWith("#labels")){
				String[] classes = line.split(" ");
				
				// filter #labels out and collect labels
				for (int i = 1; i < classes.length; i++) {
					class2number.put(classes[i], i-1);
				}
			}	
			else if (! line.startsWith("#")){
				String evaluationData = line.split("=")[1];
				readData.add(evaluationData);
			}
		}
		br.close();
	}

}
