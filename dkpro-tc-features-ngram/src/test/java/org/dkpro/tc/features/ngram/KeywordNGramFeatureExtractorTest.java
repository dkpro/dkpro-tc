/*******************************************************************************
 * Copyright 2017
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ExternalResourceDescription;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.KeywordNGramMetaCollector;
import org.dkpro.tc.features.ngram.util.KeywordNGramUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class KeywordNGramFeatureExtractorTest
{

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
    }

    private List<Instance> initialize(boolean includeComma, boolean markSentenceLocation)
        throws Exception
    {

        File luceneFolder = folder.newFolder();
        File outputPath = folder.newFolder();

        Object[] parameters = new Object[] { KeywordNGram.PARAM_UNIQUE_EXTRACTOR_NAME, "123",
                KeywordNGram.PARAM_NGRAM_KEYWORDS_FILE, "src/test/resources/data/keywordlist.txt",
                KeywordNGram.PARAM_SOURCE_LOCATION, luceneFolder,
                KeywordNGramMetaCollector.PARAM_TARGET_LOCATION, luceneFolder,
                KeywordNGram.PARAM_KEYWORD_NGRAM_MARK_SENTENCE_LOCATION, markSentenceLocation,
                KeywordNGram.PARAM_KEYWORD_NGRAM_INCLUDE_COMMAS, includeComma };

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
                "src/test/resources/ngrams/trees.txt");

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription metaCollector = AnalysisEngineFactory
                .createEngineDescription(KeywordNGramMetaCollector.class, parameters);

        ExternalResourceDescription featureExtractor = ExternalResourceFactory
                .createExternalResourceDescription(KeywordNGram.class, toString(parameters));
        List<ExternalResourceDescription> fes = new ArrayList<>();
        fes.add(featureExtractor);

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_DOCUMENT, false, false, false, false, false,
                Collections.emptyList(), fes, new String[]{});

        // run meta collector
        SimplePipeline.runPipeline(reader, segmenter, metaCollector);

        // run FE(s)
        SimplePipeline.runPipeline(reader, segmenter, featExtractorConnector);

        Gson gson = new Gson();
        List<String> lines = FileUtils
                .readLines(new File(outputPath, JsonDataWriter.JSON_FILE_NAME),"utf-8");
        List<Instance> instances = new ArrayList<>();
        for (String l : lines) {
            instances.add(gson.fromJson(l, Instance.class));
        }

        assertEquals(1, instances.size());

        return instances;
    }

    private Object[] toString(Object[] parameters)
    {
        List<Object> out = new ArrayList<>();
        for (Object o : parameters) {
            out.add(o.toString());
        }

        return out.toArray();
    }

    @Test
    public void extractKeywordsTest()
        throws Exception
    {
        List<Instance> inst = initialize(false, false);

        assertTrue(containsFeatureName(inst, "keyNG_cherry"));
        assertTrue(containsFeatureName(inst, "keyNG_apricot_peach"));
        assertTrue(containsFeatureName(inst, "keyNG_peach_nectarine_SB"));
        assertTrue(containsFeatureName(inst,
                "keyNG_cherry" + KeywordNGramUtils.MIDNGRAMGLUE + "trees"));

        assertFalse(containsFeatureName(inst, "keyNG_guava"));
        assertFalse(containsFeatureName(inst, "keyNG_peach_CA"));
        assertFalse(containsFeatureName(inst, "keyNG_nectarine_SBBEG"));
    }

    private boolean containsFeatureName(List<Instance> ins, String name)
    {
        for (Instance i : ins) {
            for (Feature f : i.getFeatures()) {
                if (f.getName().equals(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Test
    public void commasTest()
        throws Exception
    {
        List<Instance> ins = initialize(true, false);

        assertTrue(containsFeatureName(ins, "keyNG_cherry"));
        assertFalse(containsFeatureName(ins, "keyNG_apricot_peach"));
        assertFalse(containsFeatureName(ins, "keyNG_peach_nectarine_SB"));
        assertTrue(containsFeatureName(ins,
                "keyNG_cherry" + KeywordNGramUtils.MIDNGRAMGLUE + "trees"));

        assertFalse(containsFeatureName(ins, "keyNG_guava"));
        assertTrue(containsFeatureName(ins, "keyNG_peach_CA"));
        assertFalse(containsFeatureName(ins, "keyNG_nectarine_SBBEG"));

    }

    @Test
    public void sentenceLocationTest()
        throws Exception
    {
        List<Instance> ins = initialize(false, true);

        assertTrue(containsFeatureName(ins,"keyNG_cherry"));
        assertTrue(containsFeatureName(ins,"keyNG_apricot_peach"));
        assertFalse(containsFeatureName(ins,"keyNG_peach_nectarine_SB"));
        assertTrue(containsFeatureName(ins,"keyNG_cherry" + KeywordNGramUtils.MIDNGRAMGLUE + "trees"));

        assertFalse(containsFeatureName(ins,"keyNG_guava"));
        assertFalse(containsFeatureName(ins,"keyNG_peach_CA"));
        assertTrue(containsFeatureName(ins,"keyNG_nectarine_SBBEG"));
    }

}
