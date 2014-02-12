package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramPairMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.TestPairReader;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;


public class PairNgramFeatureExtractorTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void pairNgramFullPipelineTest()
            throws Exception
    {
        File lucenePath = folder.newFolder();
        File outputPath = folder.newFolder();

        Object[] parameters = new Object[] {
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW1, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW2, 1,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, "true",
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, "true",
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, "true",
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, lucenePath
        };
        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));
               
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestPairReader.class, 
                TestPairReader.PARAM_INPUT_FILE, "src/test/resources/data/textpairs.txt"
        );
        
        AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);
        
        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
                LuceneNGramPairMetaCollector.class,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, lucenePath
        );

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                parameterList,
                outputPath.getAbsolutePath(),
                WekaDataWriter.class.getName(),
                false,
                false,
                LuceneNGramPairFeatureExtractor.class.getName()
        );

        // run meta collector
        for (JCas jcas : new JCasIterable(reader, segmenter, metaCollector)) {
        }
        
        // run FE
        for (JCas jcas : new JCasIterable(reader, segmenter, featExtractorConnector)) {
        }
        
        for (File file : FileUtils.listFiles(outputPath, new String[] { "arff.gz"}, true)) {
            System.out.println(getGzippedContents(file));
        }
    }
    
    private static String getGzippedContents(File file) 
            throws IOException
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF-8"));
        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            sb.append(inputLine + "\n");
        }
        reader.close();
        return sb.toString();
    }
}