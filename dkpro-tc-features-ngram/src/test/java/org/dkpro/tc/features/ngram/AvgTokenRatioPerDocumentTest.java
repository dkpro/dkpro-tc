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
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.MaxNrOfTokensOverAllDocumentsMC;
import org.junit.Before;

import com.google.common.collect.Lists;

public class AvgTokenRatioPerDocumentTest extends LuceneMetaCollectionBasedFeatureTestBase {
	private static String EXTRACTOR_NAME = "5646534431";
	
	@Before
	public void setup() {
		super.setup();

		featureClass = AvgTokenRatioPerDocument.class;
		metaCollectorClass = MaxNrOfTokensOverAllDocumentsMC.class;
	}

	@Override
	protected void evaluateMetaCollection(File luceneFolder) throws Exception {
		List<String> entries = new ArrayList<String>(getEntriesFromIndex(luceneFolder));
		Collections.sort(entries, new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				return Integer.valueOf(o1.split("_")[0]).compareTo(Integer.valueOf(o2.split("_")[0]));
			}}
		);
		entries = Lists.reverse(entries);
		
		assertEquals(3, entries.size());
		assertEquals("15", entries.get(0).split("_")[0]);		
	}

	@Override
	protected void evaluateExtractedFeatures(File output) throws Exception {
		List<Instance> instances = readInstances(output);
		Collections.sort(instances, new Comparator<Instance>() {

			@Override
			public int compare(Instance o1, Instance o2) {
				Double v1 = (Double) new ArrayList<Feature>(o1.getFeatures()).get(0).getValue();
				Double v2 = (Double) new ArrayList<Feature>(o2.getFeatures()).get(0).getValue();
				return v1.compareTo(v2);
			}
		});
		
		instances = Lists.reverse(instances);
		
		
		assertEquals(3, instances.size());
		
		double r = (Double)(new ArrayList<Feature>(instances.get(0).getFeatures()).get(0).getValue());
		assertEquals(1.0, r, 0.01);
		
		r = (Double)(new ArrayList<Feature>(instances.get(1).getFeatures()).get(0).getValue());
		assertEquals(0.8, r, 0.01);
		
		r = (Double)(new ArrayList<Feature>(instances.get(2).getFeatures()).get(0).getValue());
		assertEquals(0.53, r, 0.01);		
	}

	@Override
	protected CollectionReaderDescription getMetaReader() throws Exception {
		return  CollectionReaderFactory.createReaderDescription(
				TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en",
				TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/",
				TestReaderSingleLabel.PARAM_PATTERNS, "text*"
				);
	}

	@Override
	protected CollectionReaderDescription getFeatureReader() throws Exception {
		return getMetaReader();
	}

	@Override
	protected Object[] getMetaCollectorParameters(File luceneFolder) {
		return new Object[] { AvgTokenRatioPerDocument.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
				AvgTokenRatioPerDocument.PARAM_NGRAM_USE_TOP_K, "1", AvgTokenRatioPerDocument.PARAM_SOURCE_LOCATION,
				luceneFolder.toString(), MaxNrOfTokensOverAllDocumentsMC.PARAM_TARGET_LOCATION,
				luceneFolder.toString(), AvgTokenRatioPerDocument.PARAM_NGRAM_MIN_N, "1",
				AvgTokenRatioPerDocument.PARAM_NGRAM_MAX_N, "1", };
	}

	@Override
	protected Object[] getFeatureExtractorParameters(File luceneFolder) {
		return new Object[] { AvgTokenRatioPerDocument.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
				AvgTokenRatioPerDocument.PARAM_SOURCE_LOCATION,
				luceneFolder.toString(), MaxNrOfTokensOverAllDocumentsMC.PARAM_TARGET_LOCATION,
				luceneFolder.toString()};
	}
}
