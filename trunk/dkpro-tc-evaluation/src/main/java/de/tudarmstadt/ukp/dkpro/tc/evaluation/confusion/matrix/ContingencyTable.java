/*
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
 */

package de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContingencyTable {

	private Map<String, Integer> class2Number;
	private double[][][] table;
	
	public ContingencyTable(String ... classNames)
	{
		if (classNames.length == 0) {
			throw new IllegalArgumentException("Must at least specify on class name.");
		}
		this.class2Number = classNamesToMapping(Arrays.asList(classNames));
		this.table = new double[class2Number.size()][2][2];
	}
	
	public ContingencyTable(Map<String, Integer> class2Number) {
		this.class2Number = class2Number;
		this.table = new double[class2Number.size()][2][2];
	}
	
	public int getSize() {
		return table.length;
	}
	
	public void addTruePositives(String className, double count) {
		table[class2Number.get(className)][0][0] += count;		
	}

	public void addTrueNegatives(String className, double count) {
		table[class2Number.get(className)][1][1] += count;
	}

	public void addFalsePositives(String className, double count) {
		table[class2Number.get(className)][1][0] += count;
	}

	public void addFalseNegatives(String className, double count) {
		table[class2Number.get(className)][0][1] += count;		
	}
	
	public double getTruePositives(String className) {
		return table[class2Number.get(className)][0][0];
	}

	public double getTrueNegatives(String className) {
		return table[class2Number.get(className)][1][1];
	}

	public double getFalsePositives(String className) {
		return table[class2Number.get(className)][1][0];		
	}

	public double getFalseNegatives(String className) {
		return table[class2Number.get(className)][0][1];
	}
	
	public void addTruePositives(int classId, double count) {
		table[classId][0][0] += count;		
	}

	public void addTrueNegatives(int classId, double count) {
		table[classId][1][1] += count;
	}

	public void addFalsePositives(int classId, double count) {
		table[classId][1][0] += count;
	}

	public void addFalseNegatives(int classId, double count) {
		table[classId][0][1] += count;		
	}
	
	public double getTruePositives(int classId) {
		return table[classId][0][0];
	}

	public double getTrueNegatives(int classId) {
		return table[classId][1][1];
	}

	public double getFalsePositives(int classId) {
		return table[classId][1][0];		
	}

	public double getFalseNegatives(int classId) {
		return table[classId][0][1];
	}
	
	public static Map<String, Integer> classNamesToMapping(Collection<String> collection)
	{
		List<String> classNames = new ArrayList<>(collection);
		Collections.sort(classNames);
		
		Map<String, Integer> mapping = new HashMap<String, Integer>();
		for (int i=0; i<classNames.size(); i++) {
			mapping.put(classNames.get(i), i);
		}
		
		return mapping;
	}
	
	/**
	 * combine contingency tables of all labels into one table
	 * 
	 * @return
	 */
	public CombinedContingencyTable buildCombinedMatrix() {
		double[][] combinedMatrix = new double[2][2]; 
		for (int x = 0; x < table.length; x++){
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					combinedMatrix[y][z] += table[x][y][z];
				}
			}
		}
		return new CombinedContingencyTable(combinedMatrix);
	}
}