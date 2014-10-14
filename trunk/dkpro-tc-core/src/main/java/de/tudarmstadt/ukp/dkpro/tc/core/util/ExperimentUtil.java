package de.tudarmstadt.ukp.dkpro.tc.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

public class ExperimentUtil {

	public static Dimension<List<String>> getAblationTestFeatures(String ... featureExtractorClassNames) {
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
