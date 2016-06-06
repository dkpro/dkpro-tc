/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.features.pair.core.ngram;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.task.uima.DocumentTextClassificationUnitAnnotator;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.features.pair.core.ngram.meta.FrequencyDistributionNGramPMetaCollector;
import org.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramPMetaCollector;
import org.dkpro.tc.fstore.simple.DenseFeatureStore;
import org.dkpro.tc.testing.TestPairReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class LuceneAndFDEquivalenceNGramPTest
{
    FeatureStore fsFrequenceDist;
    FeatureStore fsLucene;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private void initialize()
        throws Exception
    {
        AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);
        
        AnalysisEngineDescription doc = AnalysisEngineFactory.createEngineDescription(
                DocumentTextClassificationUnitAnnotator.class,
                DocumentTextClassificationUnitAnnotator.PARAM_FEATURE_MODE, Constants.FM_PAIR);

        AggregateBuilder builder = new AggregateBuilder();
        builder.add(segmenter, Constants.INITIAL_VIEW, Constants.PART_ONE);
        builder.add(doc, Constants.INITIAL_VIEW, Constants.PART_ONE);
        builder.add(segmenter, Constants.INITIAL_VIEW, Constants.PART_TWO);
        builder.add(doc, Constants.INITIAL_VIEW, Constants.PART_TWO);
        
        File frequencyDistFile = folder.newFile();
        File luceneFile = folder.newFolder();
        File outputPathFrequencyDist = folder.newFolder();
        File outputPathLucene = folder.newFolder();
        File dfStoreFile = folder.newFile();        
        
 CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
         TestPairReader.class, 
         TestPairReader.PARAM_INPUT_FILE,
                "src/test/resources/data/textpairs.txt");
        
        ArrayList<Object> parametersLucene = new ArrayList<Object>(
                Arrays.asList(new Object[] { 
                		LuceneNGramPFE.PARAM_NGRAM_MIN_N_VIEW1, 1, 
                		LuceneNGramPFE.PARAM_NGRAM_MIN_N_VIEW2, 1,
                		LuceneNGramPFE.PARAM_NGRAM_MAX_N_VIEW1, 3,
                		LuceneNGramPFE.PARAM_NGRAM_MAX_N_VIEW2, 3,
                		LuceneNGramPFE.PARAM_NGRAM_USE_TOP_K_VIEW1, 500,
                		LuceneNGramPFE.PARAM_NGRAM_USE_TOP_K_VIEW2, 500,
                		LuceneNGramPFE.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                		LuceneNGramPFE.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, true,
                		LuceneNGramPFE.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, false,
                		LuceneNGramPFE.PARAM_MARK_VIEWBLIND_NGRAMS_WITH_LOCAL_VIEW, false,
                		LuceneNGramPFE.PARAM_NGRAM_LOWER_CASE, true,        		
                        LuceneNGramPFE.PARAM_LUCENE_DIR, luceneFile,
                        LuceneNGramPFE.PARAM_TF_IDF_CALCULATION, false}));
        
        ArrayList<Object> parametersFrequencyDist = new ArrayList<Object>(
                Arrays.asList(new Object[] { 
                		FrequencyDistributionNGramPFE.PARAM_NGRAM_MIN_N, 1, 
                		FrequencyDistributionNGramPFE.PARAM_NGRAM_MAX_N, 3,
                		FrequencyDistributionNGramPFE.PARAM_NGRAM_USE_TOP_K, 500,
                		FrequencyDistributionNGramPFE.PARAM_DFSTORE_FILE, dfStoreFile,
                		FrequencyDistributionNGramPFE.PARAM_NGRAM_FD_FILE, frequencyDistFile,
                		FrequencyDistributionNGramPFE.PARAM_NGRAM_LOWER_CASE, true,
                		FrequencyDistributionNGramPFE.PARAM_TF_IDF_CALCULATION, false
                		}));
        
        AnalysisEngineDescription metaCollectorFrequencyDist = AnalysisEngineFactory
                .createEngineDescription(FrequencyDistributionNGramPMetaCollector.class,
                        parametersFrequencyDist.toArray());
        
        AnalysisEngineDescription metaCollectorLucene = AnalysisEngineFactory
                .createEngineDescription(LuceneNGramPMetaCollector.class,
                        parametersLucene.toArray());

        AnalysisEngineDescription featExtractorConnectorFrequencyDist = TaskUtils
                .getFeatureExtractorConnector(parametersFrequencyDist,
                        outputPathFrequencyDist.getAbsolutePath(), JsonDataWriter.class.getName(),
                        Constants.LM_SINGLE_LABEL, Constants.FM_PAIR,
                        DenseFeatureStore.class.getName(), false, false, false, false,
                        FrequencyDistributionNGramPFE.class.getName());
        
        AnalysisEngineDescription featExtractorConnectorLucene = TaskUtils
                .getFeatureExtractorConnector(parametersLucene,
                        outputPathLucene.getAbsolutePath(), JsonDataWriter.class.getName(),
                        Constants.LM_SINGLE_LABEL, Constants.FM_PAIR,
                        DenseFeatureStore.class.getName(), false, false, false, false,
                        LuceneNGramPFE.class.getName());
        
        // run meta collector
        SimplePipeline.runPipeline(reader, builder.createAggregateDescription(), metaCollectorFrequencyDist);
        // run FE
        SimplePipeline.runPipeline(reader, builder.createAggregateDescription(), featExtractorConnectorFrequencyDist);        
        Gson gson = new Gson();
        fsFrequenceDist = gson.fromJson(FileUtils.readFileToString(new File(
        		outputPathFrequencyDist, JsonDataWriter.JSON_FILE_NAME)), DenseFeatureStore.class);

        // run meta collector
        SimplePipeline.runPipeline(reader, builder.createAggregateDescription(), metaCollectorLucene);
        // run FE
        SimplePipeline.runPipeline(reader, builder.createAggregateDescription(), featExtractorConnectorLucene);
        fsLucene = gson.fromJson(FileUtils.readFileToString(new File(
                outputPathLucene, JsonDataWriter.JSON_FILE_NAME)), DenseFeatureStore.class);
    }

    @Test
    public void CompareOldAndNewPairFETest()
        throws Exception
    {
        initialize();
        FrequencyDistribution<String> view1features = new FrequencyDistribution<String>();
        FrequencyDistribution<String> view2features = new FrequencyDistribution<String>();

        for (Instance instance : fsLucene.getInstances()) {
        	for (Feature feature : instance.getFeatures()) {
                if (feature.getName().startsWith("view1NG_") && ((double)feature.getValue()) > 0) {
                    view1features.addSample(feature.getName().replace("view1NG_", ""), 1);
                }
                else if (feature.getName().startsWith("view2NG_") && ((double)feature.getValue()) > 0) {
                    view2features.addSample(feature.getName().replace("view2NG_", ""), 1);
                }
        	}
        }
        
        for (Instance instance : fsFrequenceDist.getInstances()) {
        	for (Feature feature : instance.getFeatures()) {
                if (feature.getName().startsWith("ngrams_PART_ONE_") && ((double)feature.getValue()) > 0) {
                    view1features.addSample(feature.getName().replace("ngrams_PART_ONE_", ""), 1);
                }
                else if (feature.getName().startsWith("ngrams_PART_TWO_") && ((double)feature.getValue()) > 0) {
                    view2features.addSample(feature.getName().replace("ngrams_PART_TWO_", ""), 1);
                }
        	}
        }

        assertEquals(9, view1features.getKeys().size());
        for (String sample : view1features.getKeys()) {
            assertEquals(2, view1features.getCount(sample));
        }
        assertEquals(9, view2features.getKeys().size());
        for (String sample : view2features.getKeys()) {
            assertEquals(2, view2features.getCount(sample));
        }
    }
}
