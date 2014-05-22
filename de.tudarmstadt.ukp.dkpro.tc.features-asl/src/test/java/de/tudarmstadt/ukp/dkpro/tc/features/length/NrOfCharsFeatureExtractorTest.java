package de.tudarmstadt.ukp.dkpro.tc.features.length;

import static de.tudarmstadt.ukp.dkpro.tc.core.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class NrOfCharsFeatureExtractorTest
{
    @Test
    public void nrOfCharsFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test. This is a test.");
        engine.process(jcas);

        NrOfCharsDFE extractor = new NrOfCharsDFE();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(3, features.size());

        Iterator<Feature> iter = features.iterator();
        assertFeature(NrOfCharsDFE.FN_NR_OF_CHARS, 31., iter.next());
        assertFeature(NrOfCharsDFE.FN_NR_OF_CHARS_PER_SENTENCE, 15.5, iter.next());
        assertFeature(NrOfCharsDFE.FN_NR_OF_CHARS_PER_TOKEN, 3.1, iter.next());
    }
}