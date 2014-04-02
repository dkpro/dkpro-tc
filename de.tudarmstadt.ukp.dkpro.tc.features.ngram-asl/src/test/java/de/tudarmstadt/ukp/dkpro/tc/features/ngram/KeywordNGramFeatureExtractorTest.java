package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.KeywordNGramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.KeywordNGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;

public class KeywordNGramFeatureExtractorTest
{

    FeatureStore fs;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
    }

    private void initialize(boolean includeComma, boolean markSentenceLocation)
        throws Exception
    {

        File luceneFolder = folder.newFolder();
        File outputPath = folder.newFolder();

        Object[] parameters = new Object[] { KeywordNGramDFE.PARAM_NGRAM_KEYWORDS_FILE,
                "src/test/resources/data/keywordlist.txt", KeywordNGramDFE.PARAM_LUCENE_DIR,
                luceneFolder, KeywordNGramDFE.PARAM_KEYWORD_NGRAM_MARK_SENTENCE_LOCATION,
                markSentenceLocation, KeywordNGramDFE.PARAM_KEYWORD_NGRAM_INCLUDE_COMMAS,
                includeComma };
        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
                "src/test/resources/ngrams/trees.txt");

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
                KeywordNGramMetaCollector.class, parameterList.toArray());

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                parameterList, outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_DOCUMENT, false, false,
                KeywordNGramDFE.class.getName());

        // run meta collector
        SimplePipeline.runPipeline(reader, segmenter, metaCollector);

        // run FE(s)
        SimplePipeline.runPipeline(reader, segmenter, featExtractorConnector);

        Gson gson = new Gson();
        fs = gson.fromJson(
                FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)),
                SimpleFeatureStore.class);
        assertEquals(1, fs.getNumberOfInstances());
    }

    @Test
    public void extractKeywordsTest()
        throws Exception
    {
        initialize(false, false);

        assertTrue(fs.getFeatureNames().contains("keyNG_cherry"));
        assertTrue(fs.getFeatureNames().contains("keyNG_apricot_peach"));
        assertTrue(fs.getFeatureNames().contains("keyNG_peach_nectarine_SB"));
        assertTrue(fs.getFeatureNames().contains(
                "keyNG_cherry" + KeywordNGramUtils.MIDNGRAMGLUE + "trees"));

        assertFalse(fs.getFeatureNames().contains("keyNG_guava"));
        assertFalse(fs.getFeatureNames().contains("keyNG_peach_CA"));
        assertFalse(fs.getFeatureNames().contains("keyNG_nectarine_SBBEG"));
    }

    @Test
    public void commasTest()
        throws Exception
    {
        initialize(true, false);

        assertTrue(fs.getFeatureNames().contains("keyNG_cherry"));
        assertFalse(fs.getFeatureNames().contains("keyNG_apricot_peach"));
        assertFalse(fs.getFeatureNames().contains("keyNG_peach_nectarine_SB"));
        assertTrue(fs.getFeatureNames().contains(
                "keyNG_cherry" + KeywordNGramUtils.MIDNGRAMGLUE + "trees"));

        assertFalse(fs.getFeatureNames().contains("keyNG_guava"));
        assertTrue(fs.getFeatureNames().contains("keyNG_peach_CA"));
        assertFalse(fs.getFeatureNames().contains("keyNG_nectarine_SBBEG"));

    }

    @Test
    public void sentenceLocationTest()
        throws Exception
    {
        initialize(false, true);

        assertTrue(fs.getFeatureNames().contains("keyNG_cherry"));
        assertTrue(fs.getFeatureNames().contains("keyNG_apricot_peach"));
        assertFalse(fs.getFeatureNames().contains("keyNG_peach_nectarine_SB"));
        assertTrue(fs.getFeatureNames().contains(
                "keyNG_cherry" + KeywordNGramUtils.MIDNGRAMGLUE + "trees"));

        assertFalse(fs.getFeatureNames().contains("keyNG_guava"));
        assertFalse(fs.getFeatureNames().contains("keyNG_peach_CA"));
        assertTrue(fs.getFeatureNames().contains("keyNG_nectarine_SBBEG"));
    }

}
