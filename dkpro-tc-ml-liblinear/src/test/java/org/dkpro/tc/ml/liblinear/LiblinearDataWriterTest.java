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
package org.dkpro.tc.ml.liblinear;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.liblinear.writer.LiblinearDataWriter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.bwaldvogel.liblinear.Problem;

public class LiblinearDataWriterTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void dataWriterTest() throws Exception {

		List<Instance> fs = new ArrayList<Instance>();

		List<Feature> features1 = new ArrayList<>();
		features1.add(new Feature("feature1", 1.0));
		features1.add(new Feature("feature2", 0.0));

		List<Feature> features2 = new ArrayList<>();
		features2.add(new Feature("feature2", 0.5));
		features2.add(new Feature("feature1", 0.5));

		Instance instance1 = new Instance(features1, "0");
		Instance instance2 = new Instance(features2, "1");

		fs.add(instance1);
		fs.add(instance2);

		File outputDirectory = folder.newFolder();
		StringBuilder sb = new StringBuilder();
		sb.append("feature1\n");
		sb.append("feature2\n");
		FileUtils.writeStringToFile(new File(outputDirectory, Constants.FILENAME_FEATURES), sb.toString(), "utf-8");
		File outputFile = new File(outputDirectory, Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);
		LiblinearDataWriter writer = new LiblinearDataWriter();
		writer.init(outputDirectory, false, Constants.LM_SINGLE_LABEL, false, new String[]{"0", "1"});
		writer.writeClassifierFormat(fs);

		Problem problem = Problem.readFromFile(outputFile, 1.0);
		assertEquals(2, problem.l);
		assertEquals(4, problem.n);
		assertEquals(0.0, problem.y[0], 0.00001);
		assertEquals(1.0, problem.y[1], 0.00001);
	}
}
