package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.TestReader;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneNGramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;

public class LuceneNGramFeatureExtractorTest
{
    
    @Rule 
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void luceneNGramFeatureExtractorTest()
        throws Exception
    {
        
        File luceneFolder = folder.newFolder();
        File outputPath = folder.newFolder();
        
        Object[] parameters = new Object[] {
                LuceneNGramFeatureExtractor.PARAM_NGRAM_USE_TOP_K, 3,
                LuceneNGramFeatureExtractor.PARAM_LUCENE_DIR, luceneFolder
        };
        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReader.class, 
                TestReader.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/*.txt"
        );
        
        AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);
        
        
        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
                LuceneNGramMetaCollector.class,
                parameterList.toArray()
        );

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                parameterList,
                outputPath.getAbsolutePath(),
                JsonDataWriter.class.getName(),
                false,
                false,
                LuceneNGramFeatureExtractor.class.getName()
        );

        // run meta collector
        SimplePipeline.runPipeline(reader, segmenter, metaCollector);

        // run FE(s)
        SimplePipeline.runPipeline(reader, segmenter, featExtractorConnector);

        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)), SimpleFeatureStore.class);
        assertEquals(2, fs.getNumberOfInstances());
        assertEquals(1, fs.getUniqueOutcomes().size());
        
        Set<String> featureNames = new HashSet<String>(fs.getFeatureNames());
        assertEquals(3, featureNames.size());
        assertTrue(featureNames.contains("ngram_4"));
        assertTrue(featureNames.contains("ngram_5"));
        assertTrue(featureNames.contains("ngram_5_5"));

        System.out.println(FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)));
        
    }
}
