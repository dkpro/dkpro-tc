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
import java.util.List;
import java.util.Set;

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
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.LuceneNGramMetaCollector;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class LuceneNgramUFETest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void testLuceneMetaCollectorOutput() throws Exception
    {
        File luceneFolder = folder.newFolder();

        Object[] parameters = new Object[] { LuceneNGramUFE.PARAM_NGRAM_USE_TOP_K, 1,
                LuceneNGramUFE.PARAM_LUCENE_DIR, luceneFolder,
                LuceneNGramUFE.PARAM_NGRAM_MIN_N, 1,
                LuceneNGramUFE.PARAM_NGRAM_MAX_N, 1,
        };
        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en", TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
                "src/test/resources/ngrams/text3.txt");

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
                LuceneNGramMetaCollector.class, parameterList.toArray());

        // run meta collector
        SimplePipeline.runPipeline(reader, segmenter, metaCollector);

        Set<String> tokens = getTokensFromIndex(luceneFolder);
        tokens.forEach(x -> System.out.println(x));
        assertEquals(6, tokens.size());
        assertTrue(tokens.contains("."));
        assertTrue(tokens.contains("birds"));
        assertTrue(tokens.contains("cats"));
        assertTrue(tokens.contains("chase"));
        assertTrue(tokens.contains("eats"));
        assertTrue(tokens.contains("mice"));
        
        assertEquals(2, getTermFreq(luceneFolder, "cats"));
    }

    private int getTermFreq(File luceneFolder, String string) throws Exception
    {
        @SuppressWarnings("deprecation")
        IndexReader idxReader = IndexReader.open(FSDirectory.open(luceneFolder));
        Term term = new Term("ngram", string);
        return (int) idxReader.totalTermFreq(term);
    }

    private Set<String> getTokensFromIndex(File luceneFolder) throws Exception
    {
        Set<String> token = new HashSet<>();
        @SuppressWarnings("deprecation")
        IndexReader idxReader = IndexReader.open(FSDirectory.open(luceneFolder));
        Fields fields = MultiFields.getFields(idxReader);
        for(String field : fields) {
            if(field.equals("id")){
                continue;
            }
            Terms terms = fields.terms(field);
            TermsEnum termsEnum = terms.iterator(null);
            BytesRef text;
            while((text = termsEnum.next()) != null) {
              token.add(text.utf8ToString());
          }
        }
        return token;
    }
}
