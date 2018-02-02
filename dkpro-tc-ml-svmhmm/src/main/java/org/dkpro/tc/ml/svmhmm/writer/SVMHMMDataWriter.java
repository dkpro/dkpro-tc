/*
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
 */

package org.dkpro.tc.ml.svmhmm.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.ml.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.ml.svmhmm.util.OriginalTextHolderFeatureExtractor;

import com.google.gson.Gson;

/**
 * Converts features to the internal format for SVM HMM
 */
public class SVMHMMDataWriter implements DataWriter {

	// random prefix for all meta-data features
	public static final String META_DATA_FEATURE_PREFIX = "ed64ffc5d412c3d3430e0d42d6a668110d1ce8ee";

	private static final double EPS = 0.00000000001;

	static Log log = LogFactory.getLog(SVMHMMDataWriter.class);

	// a consecutive single number counter to identify a sequence over all CAS
	Map<String, Integer> uniqueId = new HashMap<String, Integer>();
	int consequtiveUniqueDocSeqId = 0;

	private File outputDirectory;

	private File classifierFormatOutputFile;

	private BufferedWriter bw;
	Gson gson = new Gson();

	private TreeSet<String> featureNames;

	private Map<String, Integer> featureNameMap;


	private Integer getUniqueSequenceId(Instance instance) {
		String key = instance.getJcasId() + "-" + instance.getSequenceId();
		Integer consecSeqId = uniqueId.get(key);
		if (consecSeqId == null) {
			consecSeqId = consequtiveUniqueDocSeqId++;
			uniqueId.put(key, consecSeqId);
		}
		return consecSeqId;
	}

	protected boolean isMetaDataFeature(String featureName) {
		return featureName.startsWith(META_DATA_FEATURE_PREFIX);
	}

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
			writeClassifierFormat(ins);
		}

		reader.close();
		FileUtils.deleteQuietly(new File(outputDirectory, Constants.GENERIC_FEATURE_FILE));
	}

	@Override
	public void writeClassifierFormat(Collection<Instance> instances) throws Exception {
		if (featureNames == null) {
			// create feature name mapping and serialize it at first
			// pass-through
			loadFeatureNames();
			initFeatureNameMap();
			writeFeatureName2idMapping(outputDirectory, SVMHMMAdapter.getFeatureNameMappingFilename(), featureNameMap);
		}

		initClassifierFormat();

		PrintWriter pw = new PrintWriter(bw);

		List<Instance> inst = new ArrayList<>(instances);
		for (int i = 0; i < inst.size(); i++) {
			Instance instance = inst.get(i);

			// placeholder for original token
			String originalToken = null;

			// other "features" - meta data features that will be stored in the
			// comment
			SortedMap<String, String> metaDataFeatures = new TreeMap<>();

			// feature values
			SortedMap<Integer, Number> featureValues = new TreeMap<>();
			for (Feature f : instance.getFeatures()) {
				String featureName = f.getName();
				Object featureValue = f.getValue();

				// we ignore null feature values
				if (featureValue == null) {
					continue;
				}

				// get original token stored in OriginalToken feature
				if (OriginalTextHolderFeatureExtractor.ORIGINAL_TEXT.equals(featureName)) {
					// if original token/text was multi line, join it to a
					// single line
					// originalToken = ((String) featureValue).replaceAll("\\n",
					// " ");
					originalToken = (String) featureValue;
					continue;
				}

				// handle other possible features as metadata?
				if (isMetaDataFeature(featureName)) {
					metaDataFeatures.put(featureName, (String) featureValue);
					continue;
				}

				// not allow other non-number features
				if (!(featureValue instanceof Number)) {
					log.debug("Only features with number values are allowed, but was " + f);
					continue;
				}

				// in case the feature store produced dense feature vector with
				// zeros for
				// non-present features, we ignore zero value features here
				Number featureValueNumber = (Number) featureValue;
				if (Math.abs(featureValueNumber.doubleValue() - 0d) < EPS) {
					continue;
				}

				// get number and int value of the feature
				Integer featureNumber = featureNameMap.get(featureName);

				featureValues.put(featureNumber, featureValueNumber);
			}

			// print formatted output: label name and sequence id
			pw.printf(Locale.ENGLISH, "%s qid:%d ", instance.getOutcome(), getUniqueSequenceId(instance));

			// print sorted features
			for (Map.Entry<Integer, Number> entry : featureValues.entrySet()) {
				if (entry.getValue() instanceof Double) {
					// format double on 8 decimal places
					pw.printf(Locale.ENGLISH, "%d:%.8f ", entry.getKey(), entry.getValue().doubleValue());
				} else {
					// format as integer
					pw.printf(Locale.ENGLISH, "%d:%d ", entry.getKey(), entry.getValue().intValue());
				}
			}

			// print original token and label as comment
			pw.printf(Locale.ENGLISH, "# %s %d %s ", instance.getOutcome(), instance.getSequenceId(),
					(originalToken != null) ? (URLEncoder.encode(originalToken, "utf-8")) : "");

			// print meta-data features at the end
			for (Map.Entry<String, String> entry : metaDataFeatures.entrySet()) {
				pw.printf(" %s:%s", URLEncoder.encode(entry.getKey(), "utf-8"),
						URLEncoder.encode(entry.getValue(), "utf-8"));
			}
			// new line at the end
			pw.println();
		}

		IOUtils.closeQuietly(pw);

	}

	private void writeFeatureName2idMapping(File outputDirectory2, String featurename2instanceid2,
			Map<String, Integer> stringToInt) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String k : stringToInt.keySet()) {
			sb.append(k + "\t" + stringToInt.get(k) + "\n");
		}
		FileUtils.writeStringToFile(new File(outputDirectory, featurename2instanceid2), sb.toString(), "utf-8");
	}

	private void initFeatureNameMap() {
		featureNameMap = new HashMap<String, Integer>();
		List<String> fm = new ArrayList<>(featureNames);
		for (int i = 0; i < fm.size(); i++) {
			featureNameMap.put(fm.get(i), i + 1);
		}
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
		classifierFormatOutputFile = new File(outputDirectory, Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);

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
	public String getGenericFileName() {
		return Constants.GENERIC_FEATURE_FILE;
	}

    @Override
    public void close()
        throws Exception
    {
        
    }
}
