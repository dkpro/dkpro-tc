package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramPairMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.TestPairReader;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;


public class PairNgramFeatureExtractorTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void giveMeBetterName()
            throws Exception
    {
        File lucenePath = folder.newFolder();
        File outputPath = folder.newFolder();

        Object[] parameters = new Object[] {
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW1, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW2, 1,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW1, 3,
                LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW2, 3,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW1_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEW2_NGRAMS_AS_FEATURES, true,
                LuceneNGramPairFeatureExtractor.PARAM_USE_VIEWBLIND_NGRAMS_AS_FEATURES, true,
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
                JsonDataWriter.class.getName(),
                false,
                false,
                LuceneNGramPairFeatureExtractor.class.getName()
        );

        // run meta collector
        SimplePipeline.runPipeline(reader, segmenter, metaCollector);

        // run FE(s)
        SimplePipeline.runPipeline(reader, segmenter, featExtractorConnector);

        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)), SimpleFeatureStore.class);
        assertEquals(1, fs.getNumberOfInstances());
        assertEquals(1, fs.getUniqueOutcomes().size());

        System.out.println(FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)));
    }
}