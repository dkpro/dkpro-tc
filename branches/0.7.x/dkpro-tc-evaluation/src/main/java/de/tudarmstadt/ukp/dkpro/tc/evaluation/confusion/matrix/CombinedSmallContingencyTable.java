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

/**
 * @author Andriy Nadolskyy
 * 
 */
public class CombinedSmallContingencyTable {

	private double[][] combinedSmallContingencyTable;
	
	public CombinedSmallContingencyTable(double[][] combinedTable)
	{
		this.combinedSmallContingencyTable = combinedTable;
	}
	
	public int getSize() {
		return combinedSmallContingencyTable.length;
	}
	
	public double getTruePositives() {
		return combinedSmallContingencyTable[0][0];
	}

	public double getTrueNegatives() {
		return combinedSmallContingencyTable[1][1];
	}

	public double getFalsePositives() {
		return combinedSmallContingencyTable[1][0];		
	}

	public double getFalseNegatives() {
		return combinedSmallContingencyTable[0][1];
	}
}