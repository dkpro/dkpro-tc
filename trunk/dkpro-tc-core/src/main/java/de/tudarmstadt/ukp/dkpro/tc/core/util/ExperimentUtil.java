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
import java.util.Collection;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminable;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

public class ExperimentUtil {

    private static final String LEFTOUT_FE = "ABLATION_TEST_LEFTOUT_FEATURE_";

	        /**
     * Returns a pre-defined dimension with feature extractor sets configured for an ablation test.
     * For example, if you specify four feature extractors A, B, C, and D, you will get [A,B,C,D],
     * [A,B,C], [A,B,D], [A,C,D], [B,C,D],
     * 
     * @param featureExtractorClassNames
     *            All the feature extractors that should be tested.
     * @return a dimension with a list of feature extractor sets; named after the feature that is
     *         left out
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
        NamedArrayList<String> nameList = new NamedArrayList<String>(Arrays.asList(names));
        nameList.setName(LEFTOUT_FE + names[i]);
		nameList.remove(i);
		return nameList;
	}

    /**
     * A named list which can be used to label values which are lists (e.g. a list of feature
     * extractors) in dimensions.
     * 
     * @param <T>
     */
    public static class NamedArrayList<T> extends ArrayList<T> implements Discriminable
    {
        private String name;

        public NamedArrayList()
        {
            super();
        }

        public NamedArrayList(Collection<? extends T> c)
        {
            super(c);
        }

        public NamedArrayList(int initialCapacity)
        {
            super(initialCapacity);
        }

        public void setName(String name)
        {
            this.name = name;
        }

        @Override
        public Object getDiscriminatorValue()
        {
            return name;
        }

        @Override
        public Object getActualValue()
        {
            return this;
        }
    }

}
