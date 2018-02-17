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
import java.util.List;
import java.util.Set;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.SkipWordNGramMC;
import org.junit.Before;

public class SkipWordNGramTest extends LuceneMetaCollectionBasedFeatureTestBase
{
	
	final String FEATURE_NAME = "23424322";
	
    @Before
    public void setup()
    {
    	super.setup();
    	featureClass = SkipWordNGram.class;
    	metaCollectorClass = SkipWordNGramMC.class;
    }
    
    @Override
	protected void evaluateMetaCollection(File luceneFolder) throws Exception {
    	Set<String> entriesFromIndex = getEntriesFromIndex(luceneFolder);
    	assertEquals(17, entriesFromIndex.size());
	}

	@Override
	protected void evaluateExtractedFeatures(File output) throws Exception {
		List<Instance> instances = readInstances(output);
        assertEquals(1, instances.size());
        
        List<Feature> features = new ArrayList<Feature>(instances.get(0).getFeatures());
        assertEquals(1, features.size());

        assertEquals("SkipWordNGram_a_mice", features.get(0).getName());
	}

	@Override
	protected CollectionReaderDescription getMetaReader() throws Exception {
		return CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
                "src/test/resources/skipngram/text1.txt");
	}

	@Override
	protected CollectionReaderDescription getFeatureReader() throws Exception {
		return getMetaReader();
	}

	@Override
	protected Object[] getMetaCollectorParameters(File luceneFolder) {
		return new Object[] { 
				SkipWordNGram.PARAM_UNIQUE_EXTRACTOR_NAME, FEATURE_NAME,
				SkipWordNGram.PARAM_NGRAM_MIN_N, 2,
				SkipWordNGram.PARAM_NGRAM_MAX_N, 2,
				SkipWordNGram.PARAM_SKIP_SIZE, 1,
				SkipWordNGram.PARAM_SOURCE_LOCATION, luceneFolder.toString(), 
				SkipWordNGramMC.PARAM_TARGET_LOCATION, luceneFolder.toString()
				};
	}

	@Override
	protected Object[] getFeatureExtractorParameters(File luceneFolder) {
		return new Object[] { SkipWordNGram.PARAM_UNIQUE_EXTRACTOR_NAME, FEATURE_NAME,
				SkipWordNGram.PARAM_NGRAM_USE_TOP_K, "1",
				SkipWordNGram.PARAM_NGRAM_MIN_N, "2",
				SkipWordNGram.PARAM_NGRAM_MAX_N, "2",
				SkipWordNGram.PARAM_SKIP_SIZE, "1",
				SkipWordNGram.PARAM_SOURCE_LOCATION, luceneFolder.toString(), 
                SkipWordNGramMC.PARAM_TARGET_LOCATION, luceneFolder.toString()
                };
	}
	
}
