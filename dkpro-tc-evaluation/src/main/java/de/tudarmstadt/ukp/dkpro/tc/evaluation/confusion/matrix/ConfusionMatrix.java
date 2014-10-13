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

import java.util.Map;


/**
 * @author Andriy Nadolskyy
 * 
 */
public abstract class ConfusionMatrix<T> {
	protected T matrix;
	protected Map<String, Integer> class2number;
	
	public ConfusionMatrix(T matrix, Map<String, Integer> class2number){
		this.matrix = matrix;
		this.class2number = class2number;
	}
	
	public abstract ContingencyTable decomposeConfusionMatrix();
}
