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
package org.dkpro.tc.features.ngram;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.PhoneticNGramMC;
import org.junit.Before;

public class PhoneticNGramFeatureExtractorTest extends LuceneMetaCollectorTestBase{

	String FEATURE_NAME = "23423";
	
	
	
	@Before
	public void setup() {
		super.setup();
		featureClass = PhoneticNGram.class;
		metaCollectorClass = PhoneticNGramMC.class;
	}
	
	@Override
	protected Object[] getMetaCollectorParameters(File luceneFolder) {
		return new Object[] { PhoneticNGram.PARAM_UNIQUE_EXTRACTOR_NAME, FEATURE_NAME,
				PhoneticNGram.PARAM_NGRAM_USE_TOP_K, "10", PhoneticNGram.PARAM_SOURCE_LOCATION,
				luceneFolder.toString(), PhoneticNGramMC.PARAM_TARGET_LOCATION,
				luceneFolder.toString(), };
	}

	private int getUniqueOutcomes(List<Instance> instances) {
		Set<String> outcomes = new HashSet<String>();
		instances.forEach(x -> outcomes.addAll(x.getOutcomes()));
		return outcomes.size();
	}

	@Override
	protected void evaluateExtractedFeatures(File output) throws Exception {
		List<Instance> instances = readInstances(output);

		assertEquals(2, instances.size());
		assertEquals(1, getUniqueOutcomes(instances));		
	}

	@Override
	protected CollectionReaderDescription getMetaReader() throws Exception {
		return CollectionReaderFactory.createReaderDescription(
				TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en",
				TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/data/text*.txt");
	}

	@Override
	protected void evaluateMetaCollection(File luceneFolder) throws Exception {
		//FIXME: Missing meta evaluation
	}

	@Override
	protected Object[] getFeatureExtractorParameters(File luceneFolder) {
		return getMetaCollectorParameters(luceneFolder);
	}

	@Override
	protected CollectionReaderDescription getFeatureReader() throws Exception {
		return getMetaReader();
	}

}
