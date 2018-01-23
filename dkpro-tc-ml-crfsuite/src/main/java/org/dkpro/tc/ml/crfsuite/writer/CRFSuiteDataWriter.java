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

package org.dkpro.tc.ml.crfsuite.writer;

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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;

import com.google.gson.Gson;

public class CRFSuiteDataWriter implements DataWriter {
	CRFSuiteFeatureStoreSequenceIterator iterator;
	File outputDirectory;
	boolean useSparse;
	String learningMode;
	boolean applyWeigthing;
	private BufferedWriter bw = null;
	private Gson gson = new Gson();
	private File classifierFormatOutputFile;

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

		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(classifierFormatOutputFile), "utf-8"));

		String line = null;
		while ((line = reader.readLine()) != null) {
			Instance [] instance = gson.fromJson(line, Instance[].class);
			List<Instance> ins = new ArrayList<>(Arrays.asList(instance));

			Iterator<StringBuilder> sequenceIterator = new CRFSuiteFeatureStoreSequenceIterator(ins);

			while (sequenceIterator.hasNext()) {
				String features = sequenceIterator.next().toString();
				writer.write(features);
				writer.write("\n");
			}

		}

		reader.close();
		writer.close();
	}

	@Override
	public void writeClassifierFormat(Collection<Instance> instances, boolean compress) throws Exception {
		initClassifierFormat();

		Iterator<StringBuilder> sequenceIterator = new CRFSuiteFeatureStoreSequenceIterator(
				new ArrayList<Instance>(instances));

		while (sequenceIterator.hasNext()) {
			String features = sequenceIterator.next().toString();
			bw.write(features);
			bw.write("\n");
		}

		bw.close();
		bw = null;
	}

	private void initClassifierFormat() throws Exception {
		if (bw != null) {
			return;
		}

		bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(classifierFormatOutputFile, true), "utf-8"));

	}

	@Override
	public void init(File outputDirectory, boolean useSparse, String learningMode, boolean applyWeighting, String [] outcomes)
			throws Exception {
		this.outputDirectory = outputDirectory;
		this.useSparse = useSparse;
		this.learningMode = learningMode;
		this.applyWeigthing = applyWeighting;

		classifierFormatOutputFile = new File(outputDirectory,
				CRFSuiteAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile));

		// Caution: DKPro Lab imports (aka copies!) the data of the train task
		// as test task. We use
		// appending mode for streaming. We might errornously append the old
		// training file with
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

    @Override
    public void close()
        throws Exception
    {
        
    }

}
