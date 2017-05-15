/*******************************************************************************
 * Copyright 2017
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;

import com.google.gson.Gson;

import de.bwaldvogel.liblinear.FeatureNode;

/**
 * Format is outcome TAB index:value TAB index:value TAB ...
 * 
 * Zeros are omitted. Indexes need to be sorted.
 * 
 * For example: 1 1:1 3:1 4:1 6:1 2 2:1 3:1 5:1 7:1 1 3:1 5:1
 */
public class LiblinearDataStreamWriter implements DataWriter {
	FeatureNodeArrayEncoder encoder;
	static final String INDEX2INSTANCEID = "index2Instanceid.txt";
	File outputDirectory;
	boolean useSparse;
	String learningMode;
	boolean applyWeighting;
	File classifierFormatOutputFile;
	BufferedWriter bw = null;
	Map<String, String> index2instanceId;

	Gson gson = new Gson();
	private int maxId = 0;
	private TreeSet<String> featureNames;

	@Override
	public void writeGenericFormat(Collection<Instance> instances) throws Exception {
		initGeneric();

		// bulk-write - in sequence mode this keeps the instances together that
		// belong to the same sequence!
		Instance[] array = instances.toArray(new Instance[0]);
		bw.write(gson.toJson(array) + System.lineSeparator());

		bw.close();
		bw = null;
	}

	private void initGeneric() throws IOException {
		if (bw != null) {
			return;
		}
		bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(outputDirectory, Constants.GENERIC_FEATURE_FILE), true), "utf-8"));
	}

	@Override
	public void transformFromGeneric() throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(outputDirectory, Constants.GENERIC_FEATURE_FILE)), "utf-8"));
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			Instance [] instance = gson.fromJson(line, Instance[].class);
			List<Instance> ins = new ArrayList<>(Arrays.asList(instance));
			writeClassifierFormat(ins, false);
		}

		reader.close();
	}

	@Override
	public void writeClassifierFormat(Collection<Instance> in, boolean compress) throws Exception {

		if (featureNames == null) {
			loadFeatureNames();
		}

		initClassifierFormat();

		List<Instance> instances = new ArrayList<>(in);

		FeatureNode[][] nodes = encoder.featueStore2FeatureNode(in, featureNames);

		for (int i = 0; i < nodes.length; i++) {
			Instance instance = instances.get(i);

			recordInstanceId(instance, maxId++, index2instanceId);

			List<String> elements = new ArrayList<String>();
			for (int j = 0; j < nodes[i].length; j++) {
				FeatureNode node = nodes[i][j];
				int index = node.getIndex();
				double value = node.getValue();

				// write sparse values, i.e. skip zero values
				if (Math.abs(value) > 0.00000000001) {
					elements.add(index + ":" + value);
				}
			}
			bw.append(instance.getOutcome());
			bw.append("\t");
			bw.append(StringUtils.join(elements, "\t"));
			bw.append("\n");
		}

		bw.close();
		bw = null;

		writeMapping(outputDirectory, INDEX2INSTANCEID, index2instanceId);
		writeFeatureName2idMapping(outputDirectory, LiblinearAdapter.getFeatureNameMappingFilename(),
				encoder.stringToInt);
	}

	private void loadFeatureNames() throws IOException {
		List<String> readLines = FileUtils.readLines(new File(outputDirectory, Constants.FILENAME_FEATURES), "utf-8");
		featureNames = new TreeSet<>();
		for (String l : readLines) {
			if (l.isEmpty()) {
				continue;
			}
			featureNames.add(l);
		}
	}

	private void writeFeatureName2idMapping(File outputDirectory2, String featurename2instanceid2,
			Map<String, Integer> stringToInt) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String k : stringToInt.keySet()) {
			sb.append(k + "\t" + stringToInt.get(k) + "\n");
		}
		FileUtils.writeStringToFile(new File(outputDirectory, featurename2instanceid2), sb.toString(), "utf-8");
	}

	private void initClassifierFormat() throws Exception {
		if (bw != null) {
			return;
		}

		bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(classifierFormatOutputFile, true), "utf-8"));
	}

	@Override
	public void init(File outputDirectory, boolean useSparse, String learningMode, boolean applyWeighting)
			throws Exception {
		this.outputDirectory = outputDirectory;
		this.useSparse = useSparse;
		this.learningMode = learningMode;
		this.applyWeighting = applyWeighting;
		encoder = new FeatureNodeArrayEncoder();
		classifierFormatOutputFile = new File(outputDirectory,
				LiblinearAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile));

		index2instanceId = new HashMap<>();

		// Caution: DKPro Lab imports (aka copies!) the data of the train task
		// as test task. We use
		// appending mode for streaming. We might append the old training file
		// with
		// testing data!
		// Force delete the old training file to make sure we start with a
		// clean, empty file
		if (classifierFormatOutputFile.exists()) {
			FileUtils.forceDelete(classifierFormatOutputFile);
		}
	}

	@Override
	public boolean canStream() {
		return true;
	}

	@Override
	public boolean classiferReadsCompressed() {
		return false;
	}

	@Override
	public String getGenericFileName() {
		return Constants.GENERIC_FEATURE_FILE;
	}

	private void writeMapping(File outputDirectory, String fileName, Map<String, String> index2instanceId)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("#Index\tDkProInstanceId\n");
		for (String k : index2instanceId.keySet()) {
			sb.append(k + "\t" + index2instanceId.get(k) + "\n");
		}
		FileUtils.writeStringToFile(new File(outputDirectory, fileName), sb.toString(), "utf-8");
	}

	// build a map between the dkpro instance id and the index in the file
	private void recordInstanceId(Instance instance, int i, Map<String, String> index2instanceId) {
		Collection<Feature> features = instance.getFeatures();
		for (Feature f : features) {
			if (!f.getName().equals(Constants.ID_FEATURE_NAME)) {
				continue;
			}
			index2instanceId.put(i + "", f.getValue() + "");
			return;
		}
	}

}