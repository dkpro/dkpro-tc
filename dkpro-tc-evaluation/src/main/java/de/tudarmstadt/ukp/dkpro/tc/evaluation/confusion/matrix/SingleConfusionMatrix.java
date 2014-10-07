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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author Andriy Nadolskyy
 * 
 */
public class SingleConfusionMatrix extends ConfusionMatrix<ArrayList<ArrayList<Double>>>{

	public SingleConfusionMatrix(ArrayList<ArrayList<Double>> matrix,
			HashMap<String, Integer> class2number) {
		super(matrix, class2number);
	}

	@Override
	public double[][][] decomposeConfusionMatrix() {
		int numberOfClasses = matrix.size(); 
		double[][][] decomposedConfusionMatrix = new double[numberOfClasses][2][2];

		for (int x = 0; x < numberOfClasses; x++){
			for (int y = 0; y < numberOfClasses; y++){
				// true positives
				if(x == y){
					decomposedConfusionMatrix[x][0][0] = matrix.get(x).get(x);
				} 
				// false negatives
				else{
					decomposedConfusionMatrix[x][0][1] += matrix.get(x).get(y);
				}
				
				if(y != x ){
					// false positives
					decomposedConfusionMatrix[x][1][0] += matrix.get(y).get(x);
					
					for (int z = 0; z < numberOfClasses; z++){
						// true negatives
						if (z != x){
							decomposedConfusionMatrix[x][1][1] += matrix.get(y).get(z);
						}
					}
				}
			}
		}
		return decomposedConfusionMatrix;
	}

}
