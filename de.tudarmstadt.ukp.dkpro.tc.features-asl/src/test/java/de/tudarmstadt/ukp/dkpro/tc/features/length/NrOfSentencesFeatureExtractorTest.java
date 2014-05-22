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

public class NrOfSentencesFeatureExtractorTest
{
    @Test
    public void nrOfSentencesFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test! Does it test sentences? Oh yes, it does!");
        engine.process(jcas);

        NrOfSentencesDFE extractor = new NrOfSentencesDFE();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(1, features.size());

        Iterator<Feature> iter = features.iterator();
        assertFeature(NrOfSentencesUFE.FN_NR_OF_SENTENCES, 3, iter.next());
    }
}