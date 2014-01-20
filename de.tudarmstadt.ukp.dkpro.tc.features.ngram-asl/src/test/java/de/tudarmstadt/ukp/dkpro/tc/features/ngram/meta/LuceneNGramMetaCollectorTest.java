package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.store.FSDirectory;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramFeatureExtractor;

public class LuceneNGramMetaCollectorTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void luceneNgramMetaCollectorTest()
        throws Exception
    {
        File tmpDir = new File("target/" + LuceneBasedMetaCollector.LUCENE_DIR);
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TextReader.class, 
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/data/",
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_PATTERNS, "*.txt"
        );
        
        AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);
        
        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
                LuceneNGramMetaCollector.class,
                LuceneNGramFeatureExtractor.PARAM_LUCENE_DIR, tmpDir
        );

        for (JCas jcas : new JCasIterable(reader, segmenter, metaCollector)) {
            System.out.println(jcas.getDocumentText().length());
        }
        
        int i = 0;
        IndexReader index;
        try {
            index = DirectoryReader.open(FSDirectory.open(tmpDir));
            for (TermStats termStat : HighFreqTerms.getHighFreqTerms(index, 500, LuceneNGramFeatureExtractor.LUCENE_NGRAM_FIELD)) {
                System.out.println(termStat.termtext.utf8ToString());
                i++;
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        
       assertEquals(35, i);    
    }
}
