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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabelDocumentReader;
import org.dkpro.tc.features.ngram.meta.WordNGramMC;
import org.junit.Before;

public class WordNGramTest
    extends LuceneMetaCollectionBasedFeatureTestBase
{
    @Before
    public void setup()
    {
        super.setup();
        featureClass = WordNGram.class;
        metaCollectorClass = WordNGramMC.class;
    }

    @Override
    protected void evaluateMetaCollection(File luceneFolder) throws Exception
    {
        Set<String> entriesFromIndex = getEntriesFromIndex(luceneFolder);
        assertEquals(86, entriesFromIndex.size());
    }

    @Override
    protected void evaluateExtractedFeatures(File output) throws Exception
    {
        List<Instance> instances = readInstances(output);
        assertEquals(4, instances.size());
        assertEquals(1, getUniqueOutcomes(instances));

        Set<String> featureNames = new HashSet<String>();
        for (Instance i : instances) {
            for (Feature f : i.getFeatures()) {
                featureNames.add(f.getName());
            }
        }
        assertEquals(3, featureNames.size());
        assertTrue(featureNames.contains(WordNGram.FEATURE_PREFIX+"_4"));
        assertTrue(featureNames.contains(WordNGram.FEATURE_PREFIX+"_5"));
        assertTrue(featureNames.contains(WordNGram.FEATURE_PREFIX+"_5_5"));
    }

    @Override
    protected CollectionReaderDescription getMetaReader() throws Exception
    {
        return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabelDocumentReader.class,
                TestReaderSingleLabelDocumentReader.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/*.txt");
    }

    @Override
    protected CollectionReaderDescription getFeatureReader() throws Exception
    {
        return getMetaReader();
    }

    @Override
    protected Object[] getMetaCollectorParameters(File luceneFolder)
    {
        return new Object[] { WordNGram.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                WordNGram.PARAM_NGRAM_USE_TOP_K, "3", WordNGram.PARAM_SOURCE_LOCATION,
                luceneFolder.toString(), WordNGramMC.PARAM_TARGET_LOCATION,
                luceneFolder.toString() };
    }

    @Override
    protected Object[] getFeatureExtractorParameters(File luceneFolder)
    {
        return getMetaCollectorParameters(luceneFolder);
    }


    private int getUniqueOutcomes(List<Instance> instances)
    {
        Set<String> outcomes = new HashSet<String>();
        instances.forEach(x -> outcomes.addAll(x.getOutcomes()));
        return outcomes.size();
    }

}
