/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneNGramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;

public class NGramFeatureExtractorTest
{

    FeatureStore fsLucene;
    FeatureStore fsFrequenceDist;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // @Before
    // public void setupLogging()
    // {
    // System.setProperty("org.apache.uima.logger.class",
    // "org.apache.uima.util.impl.Log4jLogger_impl");
    // }

    private void initialize(int ngramNMin, int ngramNMax)
        throws Exception
    {

        File luceneFolder = folder.newFolder();
        File outputPathLucene = folder.newFolder();
        File frequencyDistFile = folder.newFile();
        File outputPathFrequencyDist = folder.newFolder();

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
                "src/test/resources/ngrams/text3.txt");
        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        ArrayList<Object> parametersLucene = new ArrayList<Object>(Arrays.asList(new Object[] {
                LuceneNGramDFE.PARAM_NGRAM_MIN_N, ngramNMin, LuceneNGramDFE.PARAM_NGRAM_MAX_N,
                ngramNMax, LuceneNGramDFE.PARAM_LUCENE_DIR, luceneFolder }));

        ArrayList<Object> parametersFrequencyDist = new ArrayList<Object>(
                Arrays.asList(new Object[] { FrequencyDistributionNGramDFE.PARAM_NGRAM_MIN_N,
                        ngramNMin, FrequencyDistributionNGramDFE.PARAM_NGRAM_MAX_N, ngramNMax,
                        FrequencyDistributionNGramDFE.PARAM_NGRAM_FD_FILE, frequencyDistFile }));

        AnalysisEngineDescription metaCollectorLucene = AnalysisEngineFactory
                .createEngineDescription(LuceneNGramMetaCollector.class, parametersLucene.toArray());

        AnalysisEngineDescription metaCollectorFrequencyDist = AnalysisEngineFactory
                .createEngineDescription(NGramMetaCollector.class,
                        parametersFrequencyDist.toArray());

        AnalysisEngineDescription featExtractorConnectorLucene = TaskUtils
                .getFeatureExtractorConnector(parametersLucene, outputPathLucene.getAbsolutePath(),
                        JsonDataWriter.class.getName(), Constants.LM_SINGLE_LABEL,
                        Constants.FM_DOCUMENT, SimpleFeatureStore.class.getName(), false, false, LuceneNGramDFE.class.getName());

        AnalysisEngineDescription featExtractorConnectorFrequencyDist = TaskUtils
                .getFeatureExtractorConnector(parametersFrequencyDist,
                        outputPathFrequencyDist.getAbsolutePath(), JsonDataWriter.class.getName(),
                        Constants.LM_SINGLE_LABEL, Constants.FM_DOCUMENT,
                        SimpleFeatureStore.class.getName(), false, false,
                        FrequencyDistributionNGramDFE.class.getName());

        // run meta collectors
        SimplePipeline.runPipeline(reader, segmenter, metaCollectorLucene);
        SimplePipeline.runPipeline(reader, segmenter, metaCollectorFrequencyDist);

        // run FE(s)
        SimplePipeline.runPipeline(reader, segmenter, featExtractorConnectorLucene);
        SimplePipeline.runPipeline(reader, segmenter, featExtractorConnectorFrequencyDist);

        Gson gson = new Gson();
        fsLucene = gson.fromJson(FileUtils.readFileToString(new File(outputPathLucene,
                JsonDataWriter.JSON_FILE_NAME)), SimpleFeatureStore.class);
        fsFrequenceDist = gson.fromJson(FileUtils.readFileToString(new File(
                outputPathFrequencyDist, JsonDataWriter.JSON_FILE_NAME)), SimpleFeatureStore.class);

        assertEquals(1, fsLucene.getNumberOfInstances());
        assertEquals(1, fsFrequenceDist.getNumberOfInstances());
    }

    @Test
    public void CompareOldAndNewPairFETest()
        throws Exception
    {
        initialize(1, 3);
        TreeSet<String> luceneFeatures = fsLucene.getFeatureNames();
        TreeSet<String> frequencyDistFeatures = fsFrequenceDist.getFeatureNames();

        assertEquals(luceneFeatures, frequencyDistFeatures);

    }

    @Test
    public void nonDefaultMinNMaxNTest()
        throws Exception
    {
        initialize(1, 1);

        assertTrue(fsLucene.getFeatureNames().contains("ngram_cats"));
        assertFalse(fsLucene.getFeatureNames().contains("ngram_cats_eat"));
        assertFalse(fsLucene.getFeatureNames().contains("ngram_birds_chase_cats"));

        initialize(3, 3);

        assertFalse(fsLucene.getFeatureNames().contains("ngram_cats"));
        assertFalse(fsLucene.getFeatureNames().contains("ngram_cats_eat"));
        assertTrue(fsLucene.getFeatureNames().contains("ngram_birds_chase_cats"));
    }
}
