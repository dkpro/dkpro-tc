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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.MaxNrOfSentencesOverAllDocumentsMC;
import org.dkpro.tc.features.ngram.meta.MaxSentLenOverAllDocumentsMC;
import org.junit.Before;

public class AvgSentenceRatioPerDocumentTest extends LuceneMetaCollectionBasedFeatureTestBase {

	private static String EXTRACTOR_NAME = "13233";

	@Before
	public void setup() {
		super.setup();

		featureClass = AvgSentenceRatioPerDocument.class;
		metaCollectorClass = MaxNrOfSentencesOverAllDocumentsMC.class;
	}

	protected void evaluateMetaCollection(File luceneFolder) throws Exception {
		List<String> entries = new ArrayList<String>(getEntriesFromIndex(luceneFolder));
		Collections.sort(entries);

		assertEquals(1, entries.size());
		assertEquals("3", entries.get(0).split("_")[0]);
	}

	@Override
	protected void evaluateExtractedFeatures(File output) throws Exception {
		List<Instance> instances = readInstances(output);
		assertEquals(1, instances.size());
		assertEquals(1, instances.get(0).getFeatures().size());
		Double actual = (Double) new ArrayList<Feature>(instances.get(0).getFeatures()).get(0).getValue();
		assertEquals(0.666, actual, 0.01);
	}

	@Override
	protected CollectionReaderDescription getMetaReader() throws Exception {
		return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabel.class,
				TestReaderSingleLabel.PARAM_LANGUAGE, "en", TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
				"src/test/resources/sentAvg/text5.txt");
	}

	@Override
	protected CollectionReaderDescription getFeatureReader() throws Exception {
		return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabel.class,
				TestReaderSingleLabel.PARAM_LANGUAGE, "en", TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
				"src/test/resources/sentAvg/text4.txt");
	}

	@Override
	protected Object[] getMetaCollectorParameters(File luceneFolder) {
		return new Object[] { AvgSentenceLengthRatioPerDocument.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
				AvgSentenceLengthRatioPerDocument.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
				MaxSentLenOverAllDocumentsMC.PARAM_TARGET_LOCATION, luceneFolder.toString() };
	}

	@Override
	protected Object[] getFeatureExtractorParameters(File luceneFolder) {
		return new Object[] { AvgSentenceLengthRatioPerDocument.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
				AvgSentenceLengthRatioPerDocument.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
				MaxSentLenOverAllDocumentsMC.PARAM_TARGET_LOCATION, luceneFolder.toString() };
	}
}
