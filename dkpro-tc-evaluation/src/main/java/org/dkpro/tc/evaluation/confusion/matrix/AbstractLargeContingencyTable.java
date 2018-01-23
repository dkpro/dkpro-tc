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
package org.dkpro.tc.evaluation.confusion.matrix;

import java.util.List;

public abstract class AbstractLargeContingencyTable<T> {
	protected T largeContingencyTable;
	protected List<String> labels;

	public AbstractLargeContingencyTable(T matrix, List<String> labels){
		this.largeContingencyTable = matrix;
		this.labels = labels;
	}
	
	public abstract SmallContingencyTables decomposeLargeContingencyTable();
	
	public List<String> getLabels() {
		return labels;
	}
	
	
}
