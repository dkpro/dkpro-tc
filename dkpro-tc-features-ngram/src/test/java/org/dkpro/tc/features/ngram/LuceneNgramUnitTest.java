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
package org.dkpro.tc.features.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.LuceneNGramMetaCollector;
import org.dkpro.tc.features.ngram.util.EachTokenAsUnitAnnotator;
import org.dkpro.tc.fstore.simple.DenseFeatureStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class LuceneNgramUnitTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testLuceneMetaCollectorOutput()
        throws Exception
    {
        File luceneFolder = folder.newFolder();

        runMetaCollection(luceneFolder);
        evaluateMetaCollection(luceneFolder);

        File output = runFeatureExtractor(luceneFolder);
        evaluateExtractedFeatures(output);
    }

    private void evaluateExtractedFeatures(File output)
        throws Exception
    {
        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(
                FileUtils.readFileToString(new File(output, JsonDataWriter.JSON_FILE_NAME)),
                DenseFeatureStore.class);

        assertEquals(8, fs.getNumberOfInstances());
        Iterator<Instance> iterator = fs.getInstances().iterator();
        int numFeatValueOne = 0;
        int numFeatValuesZero = 0;
        while (iterator.hasNext()) {
            Instance next = iterator.next();
            List<Feature> arrayList = new ArrayList<Feature>(next.getFeatures());
            assertEquals(1, arrayList.size());

            Object value = arrayList.get(0).getValue();
            if ((double) value == 1.0) {
                numFeatValueOne++;
            }
            else if ((double) value == 0.0) {
                numFeatValuesZero++;
            }
            else {
                throw new IllegalStateException(
                        "Value should either be 1.0 or .0.0 but was [" + value + "]");
            }
        }

        assertEquals(2, numFeatValueOne);
        assertEquals(6, numFeatValuesZero);
    }

    private File runFeatureExtractor(File luceneFolder)
        throws Exception
    {
        File outputPath = folder.newFolder();

        Object[] parameters = new Object[] { LuceneNGram.PARAM_NGRAM_USE_TOP_K, 1,
                LuceneNGram.PARAM_LUCENE_DIR, luceneFolder, LuceneNGram.PARAM_NGRAM_MIN_N, 1,
                LuceneNGram.PARAM_NGRAM_MAX_N, 1, };

        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en",
                TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/text3.txt");

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription unitAnno = AnalysisEngineFactory
                .createEngineDescription(EachTokenAsUnitAnnotator.class);

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                parameterList, outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_UNIT, DenseFeatureStore.class.getName(),
                false, false, false, false, LuceneNGram.class.getName());

        SimplePipeline.runPipeline(reader, segmenter, unitAnno, featExtractorConnector);

        return outputPath;
    }

    private void evaluateMetaCollection(File luceneFolder)
        throws Exception
    {
        Set<String> tokens = getTokensFromIndex(luceneFolder);
        assertEquals(6, tokens.size());
        assertTrue(tokens.contains("."));
        assertTrue(tokens.contains("birds"));
        assertTrue(tokens.contains("cats"));
        assertTrue(tokens.contains("chase"));
        assertTrue(tokens.contains("eats"));
        assertTrue(tokens.contains("mice"));

        assertEquals(2, getTermFreq(luceneFolder, "cats"));
        assertEquals(2, getTermFreq(luceneFolder, "."));

        assertEquals(1, getTermFreq(luceneFolder, "birds"));
        assertEquals(1, getTermFreq(luceneFolder, "chase"));
        assertEquals(1, getTermFreq(luceneFolder, "eats"));
        assertEquals(1, getTermFreq(luceneFolder, "mice"));
    }

    private void runMetaCollection(File luceneFolder)
        throws Exception
    {
        Object[] parameters = new Object[] { LuceneNGram.PARAM_NGRAM_USE_TOP_K, 1,
                LuceneNGram.PARAM_LUCENE_DIR, luceneFolder, LuceneNGram.PARAM_NGRAM_MIN_N, 1,
                LuceneNGram.PARAM_NGRAM_MAX_N, 1, };
        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en",
                TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/text3.txt");

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription metaCollector = AnalysisEngineFactory
                .createEngineDescription(LuceneNGramMetaCollector.class, parameterList.toArray());

        // run meta collector
        SimplePipeline.runPipeline(reader, segmenter, metaCollector);
    }

    private int getTermFreq(File luceneFolder, String string)
        throws Exception
    {
        @SuppressWarnings("deprecation")
        IndexReader idxReader = IndexReader.open(FSDirectory.open(luceneFolder));
        Term term = new Term("ngram", string);
        return (int) idxReader.totalTermFreq(term);
    }

    private Set<String> getTokensFromIndex(File luceneFolder)
        throws Exception
    {
        Set<String> token = new HashSet<>();
        @SuppressWarnings("deprecation")
        IndexReader idxReader = IndexReader.open(FSDirectory.open(luceneFolder));
        Fields fields = MultiFields.getFields(idxReader);
        for (String field : fields) {
            if (field.equals("id")) {
                continue;
            }
            Terms terms = fields.terms(field);
            TermsEnum termsEnum = terms.iterator(null);
            BytesRef text;
            while ((text = termsEnum.next()) != null) {
                token.add(text.utf8ToString());
            }
        }
        return token;
    }
}
