package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;

public class KeywordNGramFeatureExtractorTest
{
    KeywordNGramDFE keywordExtractor;
    JCas jcas;

    private void initialize()
        throws Exception
    {

        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("Cherry trees are hardier than apricot, "
                + "peach, and nectarine trees.  " + "Crabapple trees are the toughest.");
        engine.process(jcas);

        keywordExtractor = new KeywordNGramDFE();

        keywordExtractor.prefix = "keyNG";

        keywordExtractor.stopwords = FeatureUtil.getStopwords(null, false);
        keywordExtractor.keywords = FeatureUtil.getStopwords(
                "src/test/resources/data/keywordlist.txt", true);
        keywordExtractor.topKSet = makeSomeNgrams();
    }

    @Test
    public void extractKeywordsTest()
        throws Exception
    {
        initialize();
        keywordExtractor.keywordMinN = 1;
        keywordExtractor.keywordMaxN = 3;

        List<Feature> luceneFeatures = keywordExtractor.extract(jcas);

        for (Feature f : luceneFeatures) {
            if (f.getName().equals("keyNG_cherry")) {
                assertEquals(f.getValue(), 1);
            }
            else if (f.getName().equals("keyNG_guava")) {
                assertEquals(f.getValue(), 0);
            }
            else if (f.getName().equals("keyNG_cherry_apricot")) {
                assertEquals(f.getValue(), 1);
            }
            else if (f.getName().equals("keyNG_peach_CA")) {
                assertEquals(f.getValue(), 0);
            }
            else if (f.getName().equals("keyNG_peach_nectarine_SB")) {
                assertEquals(f.getValue(), 1);
            }
            else if (f.getName().equals("keyNG_nectarine_SBBEG")) {
                assertEquals(f.getValue(), 0);
            }
            else {
                throw new Exception();
            }
            // System.out.println(f.getName() + "  " + f.getValue());
        }

    }

    @Test
    public void commasTest()
        throws Exception
    {
        initialize();
        keywordExtractor.keywordMinN = 1;
        keywordExtractor.keywordMaxN = 3;
        keywordExtractor.includeCommas = true;

        List<Feature> luceneFeatures = keywordExtractor.extract(jcas);

        for (Feature f : luceneFeatures) {
            if (f.getName().equals("keyNG_cherry")) {
                assertEquals(f.getValue(), 1);
            }
            else if (f.getName().equals("keyNG_guava")) {
                assertEquals(f.getValue(), 0);
            }
            else if (f.getName().equals("keyNG_cherry_apricot")) {
                assertEquals(f.getValue(), 1);
            }
            else if (f.getName().equals("keyNG_peach_CA")) {
                assertEquals(f.getValue(), 1);
            }
            else if (f.getName().equals("keyNG_peach_nectarine_SB")) {
                assertEquals(f.getValue(), 0);
            }
            else if (f.getName().equals("keyNG_nectarine_SBBEG")) {
                assertEquals(f.getValue(), 0);
            }
            else {
                throw new Exception();
            }
            // System.out.println(f.getName() + "  " + f.getValue());
        }

    }

    @Test
    public void sentenceLocationTest()
        throws Exception
    {
        initialize();
        keywordExtractor.keywordMinN = 1;
        keywordExtractor.keywordMaxN = 3;
        keywordExtractor.markSentenceLocation = true;

        List<Feature> luceneFeatures = keywordExtractor.extract(jcas);

        for (Feature f : luceneFeatures) {
            if (f.getName().equals("keyNG_cherry")) {
                assertEquals(f.getValue(), 1);
            }
            else if (f.getName().equals("keyNG_guava")) {
                assertEquals(f.getValue(), 0);
            }
            else if (f.getName().equals("keyNG_cherry_apricot")) {
                assertEquals(f.getValue(), 1);
            }
            else if (f.getName().equals("keyNG_peach_CA")) {
                assertEquals(f.getValue(), 0);
            }
            else if (f.getName().equals("keyNG_peach_nectarine_SB")) {
                assertEquals(f.getValue(), 0);
            }
            else if (f.getName().equals("keyNG_nectarine_SBBEG")) {
                assertEquals(f.getValue(), 1);
            }
            else {
                throw new Exception();
            }
            // System.out.println(f.getName() + "  " + f.getValue());
        }

    }

    @Test
    public void getKeywordsFromFileTest()
        throws Exception
    {
        initialize();
        keywordExtractor.keywordMinN = 1;
        keywordExtractor.keywordMaxN = 3;

        List<Feature> luceneFeatures = keywordExtractor.extract(jcas);

        assertTrue(keywordExtractor.keywords.contains("peach"));

    }

    private static FrequencyDistribution<String> makeSomeNgrams()
    {
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.addSample("cherry", 2);
        fd.addSample("guava", 4);
        fd.addSample("cherry_apricot", 5);
        fd.addSample("peach_CA", 1);
        fd.addSample("peach_nectarine_SB", 1);
        fd.addSample("nectarine_SBBEG", 1);
        return fd;
    }

}
