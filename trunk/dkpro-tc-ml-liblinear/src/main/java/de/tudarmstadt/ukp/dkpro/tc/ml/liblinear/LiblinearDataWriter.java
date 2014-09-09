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
package de.tudarmstadt.ukp.dkpro.tc.ml.liblinear;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import de.bwaldvogel.liblinear.FeatureNode;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter.AdapterNameEntries;

/**
 * Format is
 * outcome TAB index:value TAB index:value TAB ...
 * 
 * Zeros are omitted.
 * Indexes need to be sorted.
 * 
 * For example:
 *  1 1:1  3:1  4:1   6:1
 *  2 2:1  3:1  5:1   7:1
 *  1 3:1  5:1
 */
public class LiblinearDataWriter 
	implements DataWriter
{
	
	@Override
	public void write(File outputDirectory, FeatureStore featureStore,
			boolean useDenseInstances, String learningMode) throws Exception 
	{
		FeatureNodeArrayEncoder encoder = new FeatureNodeArrayEncoder();
		FeatureNode[][] nodes = encoder.featueStore2FeatureNode(featureStore);
	
		// liblinear only supports integer outcomes, thus we need to create a mapping
		Map<String, Integer> outcomeMapping = getOutcomeMapping(featureStore.getUniqueOutcomes());
		
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<nodes.length; i++) {
			Instance instance = featureStore.getInstance(i);
			List<String> elements = new ArrayList<String>();
			for (int j=0; j<nodes[i].length; j++) {
				FeatureNode node = nodes[i][j];
				int index = node.getIndex();
				double value = node.getValue();
				
				// write sparse values, i.e. skip zero values
				if (Math.abs(value) > 0.00000000001) {
					elements.add(index + ":" + value);
				}
			}
			sb.append(outcomeMapping.get(instance.getOutcome()));
			sb.append("\t");
			sb.append(StringUtils.join(elements, "\t"));
			sb.append("\n");
		}
		
		File outputFile = new File(outputDirectory, LiblinearAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.trainingFile));
		FileUtils.writeStringToFile(outputFile, sb.toString());
		
		File mappingFile = new File(outputDirectory, LiblinearAdapter.getOutcomeMappingFilename());
		FileUtils.writeStringToFile(mappingFile, LiblinearUtils.outcomeMap2String(outcomeMapping));
	}
	
	private Map<String, Integer> getOutcomeMapping(Set<String> outcomes) {
		Map<String, Integer> outcomeMapping = new HashMap<String, Integer>();
		int i=1;
		for (String outcome : outcomes) {
			outcomeMapping.put(outcome, i);
			i++;
		}
		return outcomeMapping;
	}
	

}