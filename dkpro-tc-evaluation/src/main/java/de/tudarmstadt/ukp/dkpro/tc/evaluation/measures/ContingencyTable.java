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

package de.tudarmstadt.ukp.dkpro.tc.evaluation.measures;

public class ContingencyTable {

	private double[][][] table;
	
	public ContingencyTable(int numberOfClasses) {
		this.table = new double[numberOfClasses][2][2];
	}
	
	public int getSize() {
		return table.length;
	}
	
	public void addTruePositives(int classId, double count) {
		table[classId][0][0] += count;		
	}

	public void addTrueNegatives(int classId, double count) {
		table[classId][1][1] += count;
	}

	public void addFalsePositives(int classId, double count) {
		table[classId][0][1] += count;
	}

	public void addFalseNegatives(int classId, double count) {
		table[classId][1][0] += count;		
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
}