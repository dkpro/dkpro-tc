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
package de.tudarmstadt.ukp.dkpro.tc.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

public class ExperimentUtil {

	/**
	 * Returns a pre-defined dimension with feature extractor sets configured for an ablation test.
	 * For example, if you specify four feature extractors A, B, C, and D, you will get
	 * [A,B,C,D], [A,B,C], [A,B,D], [A,C,D], [B,C,D],
	 * 
	 * @param featureExtractorClassNames All the feature extractors that should be tested.
	 * @return
	 */
	public static Dimension<List<String>> getAblationTestFeatures(String ... featureExtractorClassNames) {
		@SuppressWarnings("unchecked")
		List<String>[] featureSets = (ArrayList<String>[]) new ArrayList[featureExtractorClassNames.length + 1];

		for (int i=0; i<featureExtractorClassNames.length; i++) {
			List<String> featureNamesMinusOne = getFeatureNamesMinusOne(featureExtractorClassNames, i);
			featureSets[i] = featureNamesMinusOne;
		}
		// also add all features extractors
		featureSets[featureExtractorClassNames.length] = new ArrayList<String>(Arrays.asList(featureExtractorClassNames));
		
        Dimension<List<String>> dimFeatureSets = Dimension.create(
        		Constants.DIM_FEATURE_SET, featureSets
        );
        
        return dimFeatureSets;
	}
	
	private static List<String> getFeatureNamesMinusOne(String[] names, int i) {
		List<String> nameList = new ArrayList<String>(Arrays.asList(names));
		nameList.remove(i);
		return nameList;
	}
}
