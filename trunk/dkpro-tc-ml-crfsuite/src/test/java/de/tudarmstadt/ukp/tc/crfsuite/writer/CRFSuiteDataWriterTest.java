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
package de.tudarmstadt.ukp.tc.crfsuite.writer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.writer.CRFSuiteDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter.AdapterNameEntries;

public class CRFSuiteDataWriterTest {
	 
	@Rule
    public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void dataWriterTest() throws Exception {
		
//		FeatureStore fs = new SimpleFeatureStore();
//		
//		List<Feature> features1 = new ArrayList<>();
//		features1.add(new Feature("feature1", 1.0));
//		features1.add(new Feature("feature2", 0.0));
//		features1.add(new Feature("feature3", "Water"));
//		
//		List<Feature> features2 = new ArrayList<>();
//		features2.add(new Feature("feature2", 0.5));
//		features2.add(new Feature("feature1", 0.5));
//		features2.add(new Feature("feature3", "Fanta"));
//		
//		Instance instance1 = new Instance(features1, "1");
//		instance1.setSequenceId(0);
//		instance1.setSequencePosition(0);
//		Instance instance2 = new Instance(features2, "2");
//		instance2.setSequenceId(0);
//		instance2.setSequencePosition(1);
//		Instance instance3 = new Instance(features1, "3");
//        instance3.setSequenceId(0);
//        instance3.setSequencePosition(2);
//        
//        Instance instance4 = new Instance(features1, "4");
//        instance4.setSequenceId(1);
//        instance4.setSequencePosition(0);
//        Instance instance5 = new Instance(features2, "4");
//        instance5.setSequenceId(1);
//        instance5.setSequencePosition(1);
//
//
//		fs.addInstance(instance1);
//		fs.addInstance(instance2);
//		fs.addInstance(instance3);
//		fs.addInstance(instance4);
//		fs.addInstance(instance5);
//		
//		File outputDirectory = folder.newFolder();
//		File outputFile = new File(outputDirectory, CRFSuiteAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.trainingFile));
//		CRFSuiteDataWriter writer = new CRFSuiteDataWriter();
//		writer.write(outputDirectory, fs, false, Constants.LM_SINGLE_LABEL);
		
	}
}
