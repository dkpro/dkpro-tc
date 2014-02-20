package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;

public class NGramMetaCollectorTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void ngramMetaCollectorTest()
        throws Exception
    {
        File tmpFdFile = folder.newFile(NGramMetaCollector.NGRAM_FD_KEY);
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TextReader.class, 
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/data/",
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_PATTERNS, "text*.txt"
        );
        
        AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);
        
        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
                NGramMetaCollector.class,
                NGramFeatureExtractor.PARAM_NGRAM_FD_FILE, tmpFdFile
        );

        for (JCas jcas : new JCasIterable(reader, segmenter, metaCollector)) {
            System.out.println(jcas.getDocumentText().length());
        }
        
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.load(tmpFdFile);
        System.out.println(fd);
        
        assertEquals(35, fd.getB());
        assertEquals(51, fd.getN());
        
        assertEquals(2, fd.getCount("small_example_."));
    }
}
