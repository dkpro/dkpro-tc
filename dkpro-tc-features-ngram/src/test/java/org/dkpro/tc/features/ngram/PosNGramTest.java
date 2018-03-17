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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.PosNGramMC;
import org.junit.Before;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class PosNGramTest
    extends LuceneMetaCollectionBasedFeatureTestBase
{

    @Before
    public void setupLogging()
    {
        super.setup();
        featureClass = PosNGram.class;
        metaCollectorClass = PosNGramMC.class;
    }

    private Collection<? extends String> getUniqueFeatureNames(List<Instance> instances)
    {
        Set<String> s = new HashSet<>();

        for (Instance i : instances) {
            for (Feature f : i.getFeatures()) {
                s.add(f.getName());
            }
        }

        return s;
    }

    private int getUniqueOutcomes(List<Instance> instances)
    {
        Set<String> outcomes = new HashSet<String>();
        instances.forEach(x -> outcomes.addAll(x.getOutcomes()));
        return outcomes.size();
    }

    @Override
    protected void evaluateMetaCollection(File luceneFolder) throws Exception
    {
        Set<String> entries = getEntriesFromIndex(luceneFolder);
        assertEquals(40, entries.size());
    }

    @Override
    protected void evaluateExtractedFeatures(File output) throws Exception
    {
        List<Instance> instances = readInstances(output);
        assertEquals(4, instances.size());
        assertEquals(1, getUniqueOutcomes(instances));

        Set<String> featureNames = new HashSet<String>(getUniqueFeatureNames(instances));
        assertEquals(5, featureNames.size());
        assertTrue(featureNames.contains("PosNGram_POS_NUM"));
        assertTrue(featureNames.contains("PosNGram_POS_NOUN"));
        assertTrue(featureNames.contains("PosNGram_POS_NUM_POS_NUM"));
    }

    @Override
    protected void runMetaCollection(File luceneFolder, AnalysisEngineDescription metaCollector)
        throws Exception
    {

        CollectionReaderDescription reader = getMetaReader();

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription posTagger = AnalysisEngineFactory.createEngineDescription(
                OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, "en");

        SimplePipeline.runPipeline(reader, segmenter, posTagger, metaCollector);
    }

    @Override
    protected CollectionReaderDescription getMetaReader() throws Exception
    {
        return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabel.class,
                TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/*.txt");
    }

    @Override
    protected CollectionReaderDescription getFeatureReader() throws Exception
    {
        return getMetaReader();
    }

    @Override
    protected Object[] getMetaCollectorParameters(File luceneFolder)
    {
        return new Object[] { PosNGram.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                PosNGram.PARAM_NGRAM_USE_TOP_K, "5", PosNGram.PARAM_SOURCE_LOCATION,
                luceneFolder.toString(), PosNGramMC.PARAM_TARGET_LOCATION,
                luceneFolder.toString() };
    }

    @Override
    protected Object[] getFeatureExtractorParameters(File luceneFolder)
    {
        return getMetaCollectorParameters(luceneFolder);
    }

    protected void runFeatureExtractor(File luceneFolder,
            AnalysisEngineDescription featureExtractor)
        throws Exception
    {

        CollectionReaderDescription reader = getFeatureReader();

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription posTagger = AnalysisEngineFactory.createEngineDescription(
                OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, "en");

        SimplePipeline.runPipeline(reader, segmenter, posTagger, featureExtractor);
    }
}
