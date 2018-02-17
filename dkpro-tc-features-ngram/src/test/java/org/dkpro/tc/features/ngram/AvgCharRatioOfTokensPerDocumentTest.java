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
import java.util.Comparator;
import java.util.List;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.features.maxnormalization.AvgTokenLengthRatioPerDocument;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.maxnormalization.MaxNrOfCharsOverAllTokensMC;
import org.junit.Before;

import com.google.common.collect.Lists;

public class AvgCharRatioOfTokensPerDocumentTest extends LuceneMetaCollectionBasedFeatureTestBase {
	private static String EXTRACTOR_NAME = "56465431";

	
	@Before
	public void setup(){
		super.setup();
		
		featureClass = AvgTokenLengthRatioPerDocument.class;
		metaCollectorClass = MaxNrOfCharsOverAllTokensMC.class;
	}
	
	@Override
	protected void evaluateMetaCollection(File luceneFolder) throws Exception {
		List<String> entries = new ArrayList<String>(getEntriesFromIndex(luceneFolder));
		Collections.sort(entries);
		entries = Lists.reverse(entries);
		//
		assertEquals(35, entries.size());
		assertEquals("5", entries.get(0).split("_")[0]);
	}

	@Override
	protected void evaluateExtractedFeatures(File output) throws Exception {
		List<Instance> instances = readInstances(output);
		Collections.sort(instances, new Comparator<Instance>() {

			@Override
			public int compare(Instance o1, Instance o2) {
				String v1 = ((Double) new ArrayList<Feature>(o1.getFeatures()).get(0).getValue()).toString();
				String v2 = ((Double) new ArrayList<Feature>(o2.getFeatures()).get(0).getValue()).toString();
				return v1.compareTo(v2);
			}
		});

		assertEquals(3, instances.size());

		double r = (Double) (new ArrayList<Feature>(instances.get(0).getFeatures()).get(0).getValue());
		assertEquals(0.2, r, 0.01);

		r = (Double) (new ArrayList<Feature>(instances.get(1).getFeatures()).get(0).getValue());
		assertEquals(0.2, r, 0.01);

		r = (Double) (new ArrayList<Feature>(instances.get(2).getFeatures()).get(0).getValue());
		assertEquals(0.7, r, 0.01);
	}

	@Override
	protected CollectionReaderDescription getMetaReader() throws Exception {
		return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabel.class,
				TestReaderSingleLabel.PARAM_LANGUAGE, "en", TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
				"src/test/resources/ngrams/", TestReaderSingleLabel.PARAM_PATTERNS, "text*");
	}

	@Override
	protected CollectionReaderDescription getFeatureReader() throws Exception {
		return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabel.class,
				TestReaderSingleLabel.PARAM_LANGUAGE, "en", TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
				"src/test/resources/ngrams/", TestReaderSingleLabel.PARAM_PATTERNS, "text*",
				TestReaderSingleLabel.PARAM_SUPPRESS_DOCUMENT_ANNOTATION, false);
	}

	@Override
	protected Object[] getMetaCollectorParameters(File luceneFolder) {
		return new Object[] { AvgTokenLengthRatioPerDocument.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
				AvgTokenLengthRatioPerDocument.PARAM_NGRAM_USE_TOP_K, "1",
				AvgTokenLengthRatioPerDocument.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
				MaxNrOfCharsOverAllTokensMC.PARAM_TARGET_LOCATION, luceneFolder.toString(),
				AvgTokenLengthRatioPerDocument.PARAM_NGRAM_MIN_N, "1", AvgTokenLengthRatioPerDocument.PARAM_NGRAM_MAX_N,
				"1", };
	}

	@Override
	protected Object[] getFeatureExtractorParameters(File luceneFolder) {
		return new Object[] { AvgTokenLengthRatioPerDocument.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
				AvgTokenLengthRatioPerDocument.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
				MaxNrOfCharsOverAllTokensMC.PARAM_TARGET_LOCATION, luceneFolder.toString() };
	}
}
