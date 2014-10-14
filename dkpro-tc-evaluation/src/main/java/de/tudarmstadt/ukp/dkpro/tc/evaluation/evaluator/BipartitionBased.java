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

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.AbstractContingencyTable;


/**
 * "The bipartitions-based evaluation measures are calculated based
 * on the comparison of the predicted relevant labels with the
 * ground truth relevant labels. This group of evaluation measures
 * is further divided into example-based and label-based."
 * Gj. Madjarov, et al., An extensive experimental comparison of methods for multi-label learning, 
 * Pattern Recognition (2012)
 * 
 * @author Andriy Nadolskyy
 * 
 */
public interface BipartitionBased {
	
	public <T> AbstractContingencyTable<T> buildConfusionMatrix();
}
