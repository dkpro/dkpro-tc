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
package org.dkpro.tc.features.ngram.meta;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.uima.DocumentModeAnnotator;
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class LuceneNGramMetaCollectorTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public static String UNIQUE_FEATURE_NAME = "123";
    
    @Test
    public void luceneNgramMetaCollectorTest()
        throws Exception
    {
        File tmpDir = folder.newFolder();

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TextReader.class, 
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/data/",
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_PATTERNS, "text*.txt"
        );
        
        AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);
        
        AnalysisEngineDescription doc = AnalysisEngineFactory.createEngineDescription(
                DocumentModeAnnotator.class,
                DocumentModeAnnotator.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT);
        
        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
                LuceneNGramMetaCollector.class,
                LuceneNGramMetaCollector.PARAM_TARGET_LOCATION, tmpDir,
                LuceneNGramMetaCollector.PARAM_UNIQUE_EXTRACTOR_NAME, UNIQUE_FEATURE_NAME
        );

        for (JCas jcas : new JCasIterable(reader, segmenter,doc, metaCollector)) {
            System.out.println(jcas.getDocumentText().length());
        }
        
        int i = 0;
        IndexReader index;
        try {
            index = DirectoryReader.open(FSDirectory.open(tmpDir));
            Fields fields = MultiFields.getFields(index);
            if (fields != null) {
                Terms terms = fields.terms(LuceneNGram.LUCENE_NGRAM_FIELD+UNIQUE_FEATURE_NAME);
                if (terms != null) {
                    TermsEnum termsEnum = terms.iterator(null);
                    BytesRef text = null;
                    while ((text = termsEnum.next()) != null) {
                        
                        if (text.utf8ToString().equals("this")) {
                            assertEquals(2, termsEnum.docFreq());
                            assertEquals(3, termsEnum.totalTermFreq());
                        }
                        
                        i++;
                    }
                }
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        
       assertEquals(35, i);    
    }
    
    @SuppressWarnings("unused")
    @Test
    public void emptyDocumentTest()
        throws Exception
    {
        File tmpDir = folder.newFolder();

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TextReader.class, 
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/empty/",
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_PATTERNS, "empty*.txt"
        );
        
        AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);
        
        AnalysisEngineDescription doc = AnalysisEngineFactory
                .createEngineDescription(DocumentModeAnnotator.class, DocumentModeAnnotator.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT);
        
        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
                LuceneNGramMetaCollector.class,
                LuceneNGram.PARAM_SOURCE_LOCATION, tmpDir
        );

        for (JCas jcas : new JCasIterable(reader, segmenter, doc, metaCollector)) {
//            System.out.println(jcas.getDocumentText().length());
        }
    }
}
