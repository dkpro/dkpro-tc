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
package org.dkpro.tc.ml.libsvm.writer;

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
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;

import com.google.gson.Gson;

/**
 * Format is outcome TAB index:value TAB index:value TAB ...
 * 
 * Zeros are omitted. Indexes need to be sorted.
 * 
 * For example: 1 1:1 3:1 4:1 6:1 2 2:1 3:1 5:1 7:1 1 3:1 5:1
 */
public class LibsvmDataWriter implements DataWriter {
	private File outputDirectory;

	private File classifierFormatOutputFile;
	private BufferedWriter bw;
	public static final String INDEX2INSTANCEID = "index2InstanceId.txt";
	Gson gson = new Gson();
	private TreeSet<String> featureNames;
	int idx = 0;
	Map<String, String> index2instanceId = new HashMap<>();
	Map<String, Integer> featureNameMap = new HashMap<>();

    private String[] outcomes;


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
			Instance[] instance = gson.fromJson(line, Instance[].class);
			List<Instance> ins = new ArrayList<>(Arrays.asList(instance));
			writeClassifierFormat(ins, false);
		}

		reader.close();
		FileUtils.deleteQuietly(new File(outputDirectory, Constants.GENERIC_FEATURE_FILE));
	}

	@Override
	public void writeClassifierFormat(Collection<Instance> instances, boolean compress) throws Exception {
		if (featureNames == null) {
			//create feature name mapping and serialize it at first pass-through
			loadFeatureNames();
			initFeatureNameMap();
			writeFeatureName2idMapping(outputDirectory, LibsvmAdapter.getFeatureNameMappingFilename(), featureNameMap);
		}

		initClassifierFormat();

		for (Instance i : instances) {
			recordInstanceId(i, idx++, index2instanceId);
			String outcome = i.getOutcome();
			bw.write(outcome);
			for (Feature f : i.getFeatures()) {
				if (!sanityCheckValue(f)) {
					continue;
				}
				bw.write("\t");
				bw.write(featureNameMap.get(f.getName()) + ":" + f.getValue());
			}
			bw.write("\n");
		}

		bw.close();
		bw = null;

		writeMapping(outputDirectory, INDEX2INSTANCEID, index2instanceId);

	}

	private void initFeatureNameMap() {
		List<String> fm = new ArrayList<>(featureNames);
		for (int i = 0; i < fm.size(); i++) {
			featureNameMap.put(fm.get(i), i + 2);
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

	@Override
	public void init(File outputDirectory, boolean useSparse, String learningMode, boolean applyWeighting, String [] outcomes)
			throws Exception {
		this.outputDirectory = outputDirectory;
        this.outcomes = outcomes;
		classifierFormatOutputFile = new File(outputDirectory,
				LibsvmAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile));

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

	private boolean sanityCheckValue(Feature f) {
		if (f.getValue() instanceof Number) {
			return true;
		}
		if (f.getName().equals(Constants.ID_FEATURE_NAME)) {
			return false;
		}

		try {
			Double.valueOf((String) f.getValue());
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Feature [" + f.getName() + "] has a non-numeric value [" + f.getValue() + "]", e);
		}
		return false;
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

    @Override
    public void close()
        throws Exception
    {
    }

}