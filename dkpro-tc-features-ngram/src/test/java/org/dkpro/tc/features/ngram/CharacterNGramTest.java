/*******************************************************************************
 * Copyright 2019
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
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabelDocumentReader;
import org.dkpro.tc.features.ngram.meta.CharacterNGramMC;
import org.junit.Before;

public class CharacterNGramTest
    extends LuceneMetaCollectionBasedFeatureTestBase
{

    static String FEATURE_NAME = "23423432434";

    @Before
    public void setup()
    {
        super.setup();
        featureClass = CharacterNGram.class;
        metaCollectorClass = CharacterNGramMC.class;
    }

    @Override
    protected void evaluateMetaCollection(File luceneFolder) throws Exception
    {
        Set<String> entriesFromIndex = getEntriesFromIndex(luceneFolder);

        assertEquals(29, entriesFromIndex.size());
    }

    @Override
    protected void evaluateExtractedFeatures(File output) throws Exception
    {
        List<Instance> instances = readInstances(output);
        assertEquals(1, instances.size());

        // ly + y*
        assertEquals(2, instances.get(0).getFeatures().size());

        List<Feature> features = new ArrayList<Feature>(instances.get(0).getFeatures());
        assertEquals(CharacterNGram.FEATURE_PREFIX + "_ly", features.get(0).getName());
        assertEquals(CharacterNGram.FEATURE_PREFIX + "_u163u32", features.get(1).getName());
    }

    @Override
    protected CollectionReaderDescription getMetaReader() throws Exception
    {
        return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabelDocumentReader.class,
                TestReaderSingleLabelDocumentReader.PARAM_LANGUAGE, "en",
                TestReaderSingleLabelDocumentReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/charngram/meta.txt");
    }

    @Override
    protected Object[] getMetaCollectorParameters(File luceneFolder)
    {
        return new Object[] { CharacterNGramMC.PARAM_UNIQUE_EXTRACTOR_NAME, FEATURE_NAME,
                CharacterNGram.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
                CharacterNGram.PARAM_NGRAM_USE_TOP_K, 5, CharacterNGram.PARAM_NGRAM_MIN_N, 2,
                CharacterNGram.PARAM_NGRAM_MAX_N, 2, CharacterNGramMC.PARAM_TARGET_LOCATION,
                luceneFolder.toString() };
    }

    @Override
    protected Object[] getFeatureExtractorParameters(File luceneFolder)
    {
        return new Object[] { CharacterNGram.PARAM_UNIQUE_EXTRACTOR_NAME, FEATURE_NAME,
                CharacterNGram.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
                CharacterNGram.PARAM_NGRAM_USE_TOP_K, "2", CharacterNGram.PARAM_NGRAM_MIN_N, "2",
                CharacterNGram.PARAM_NGRAM_MAX_N, "2", CharacterNGramMC.PARAM_TARGET_LOCATION,
                luceneFolder.toString() };
    }

    @Override
    protected CollectionReaderDescription getFeatureReader() throws Exception
    {
        return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabelDocumentReader.class,
                TestReaderSingleLabelDocumentReader.PARAM_LANGUAGE, "en",
                TestReaderSingleLabelDocumentReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/charngram/feature.txt");
    }

}
