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
import java.util.Iterator;
import java.util.List;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.MaxSentLenOverAllDocumentsMC;
import org.junit.Before;

public class AvgSentenceLengthPerDocumentTest extends LuceneMetaCollectionBasedFeatureTestBase {

	private static String EXTRACTOR_NAME = "13233";

	@Before
	public void setup(){
		super.setup();
		
		featureClass = AvgSentenceLengthRatioPerDocument.class;
		metaCollectorClass = MaxSentLenOverAllDocumentsMC.class;
	}

	protected void evaluateMetaCollection(File luceneFolder) throws Exception {
		List<String> entries = new ArrayList<String>(getEntriesFromIndex(luceneFolder));
		Collections.sort(entries);

		assertEquals("4", entries.get(0).split("_")[0]);
		assertEquals("6", entries.get(1).split("_")[0]);		
	}

	@Override
	protected void evaluateExtractedFeatures(File output) throws Exception {
		List<Instance> instances = readInstances(output);
		assertEquals(1, instances.size());
		Iterator<Instance> iterator = instances.iterator();
		double val=-1;
		while (iterator.hasNext()) {
			Instance next = iterator.next();
			List<Feature> arrayList = new ArrayList<Feature>(next.getFeatures());
			assertEquals(1, arrayList.size());

			Object value = arrayList.get(0).getValue();
			
			val = Double.valueOf(value.toString());
		}

		double expected = (4.0/6 + 6/6)/2;
		
		assertEquals(expected, val, 0.01);
	}

	@Override
	protected CollectionReaderDescription getMetaReader() throws Exception {
		return CollectionReaderFactory.createReaderDescription(
				TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en",
				TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/sentAvg/text4.txt");
	}

	@Override
	protected CollectionReaderDescription getFeatureReader() throws Exception {
		return  getMetaReader();
	}

	@Override
	protected Object[] getMetaCollectorParameters(File luceneFolder) {
		return new Object[] { AvgSentenceLengthRatioPerDocument.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
				AvgSentenceLengthRatioPerDocument.PARAM_NGRAM_USE_TOP_K, 1, AvgSentenceLengthRatioPerDocument.PARAM_SOURCE_LOCATION,
				luceneFolder.toString(), MaxSentLenOverAllDocumentsMC.PARAM_TARGET_LOCATION,
				luceneFolder.toString(), AvgSentenceLengthRatioPerDocument.PARAM_NGRAM_MIN_N, 1,
				AvgSentenceLengthRatioPerDocument.PARAM_NGRAM_MAX_N, 1 };
	}

	@Override
	protected Object[] getFeatureExtractorParameters(File luceneFolder) {
		return new Object[] { AvgSentenceLengthRatioPerDocument.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
				AvgSentenceLengthRatioPerDocument.PARAM_NGRAM_USE_TOP_K, "1", AvgSentenceLengthRatioPerDocument.PARAM_SOURCE_LOCATION,
				luceneFolder.toString(), MaxSentLenOverAllDocumentsMC.PARAM_TARGET_LOCATION,
				luceneFolder.toString(), AvgSentenceLengthRatioPerDocument.PARAM_NGRAM_MIN_N, "1",
				AvgSentenceLengthRatioPerDocument.PARAM_NGRAM_MAX_N, "1", };
	}
}
