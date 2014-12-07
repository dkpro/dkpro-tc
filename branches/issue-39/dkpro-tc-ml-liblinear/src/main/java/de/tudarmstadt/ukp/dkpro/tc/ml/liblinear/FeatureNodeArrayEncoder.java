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
/*
 * Copyright (c) 2013, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package de.tudarmstadt.ukp.dkpro.tc.ml.liblinear;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import de.bwaldvogel.liblinear.FeatureNode;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;

public class FeatureNodeArrayEncoder {

	private static final String BIAS_NAME = FeatureNodeArrayEncoder.class.getName() + ".BIAS";

	private Map<String, Integer> stringToInt;

	private int biasIndex;

	public FeatureNodeArrayEncoder() {
		this.stringToInt = new HashMap<String, Integer>();
		this.biasIndex = 1;
		this.stringToInt.put(BIAS_NAME, biasIndex);
	}

	public FeatureNode[][] featueStore2FeatureNode(FeatureStore store) {
		// map feature indexes to feature nodes, sorting by index
		Map<Integer, FeatureNode> featureNodes = new TreeMap<Integer, FeatureNode>();

		// add a "bias" feature node; otherwise LIBLINEAR is unable to predict
		// the majority class for
		// instances consisting entirely of features never seen during training
		featureNodes.put(this.biasIndex, new FeatureNode(this.biasIndex, 1));

		// convert the name String to an index
		for (String featureName : store.getFeatureNames()) {
			if (!this.stringToInt.containsKey(featureName)) {
				this.stringToInt.put(featureName, this.stringToInt.size() + 1);
			}
		}
		
		FeatureNode[][] xValues = new FeatureNode[store.getNumberOfInstances()][];
		
		int instanceOffset = 0;
		for (Instance instance : store.getInstances()) {
			for (Feature feature : instance.getFeatures()) {	
				String name = feature.getName();
				
				double value;
				if (feature.getValue() instanceof Number) {
					value = ((Number) feature.getValue()).doubleValue();
				} else {
					value = 1.0;
				}			

				int index = this.stringToInt.get(name);

				// create a feature node for the given index
				// NOTE: if there are duplicate features, only the last will be kept
				featureNodes.put(index, new FeatureNode(index, value));
			}
			
			// put the feature nodes into an array, sorted by feature index
			FeatureNode[] featureNodeArray = new FeatureNode[featureNodes.size()];
			int i = 0;
			for (Integer index : featureNodes.keySet()) {
				featureNodeArray[i] = featureNodes.get(index);
				++i;
			}
			xValues[instanceOffset] = featureNodeArray;
			instanceOffset++;
		}

		return xValues;
	}
}
