package de.tudarmstadt.ukp.dkpro.tc.features.length;

import static de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class NrOfTokensFeatureExtractorTest
{
    @Test
    public void nrOfTokensFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test.");
        engine.process(jcas);
        
        NrOfTokensFeatureExtractor extractor = new NrOfTokensFeatureExtractor();
        List<Feature> features = extractor.extract(jcas, null);

        Assert.assertEquals(2, features.size());
        
        Iterator<Feature> iter = features.iterator();
        assertFeature(NrOfTokensFeatureExtractor.FN_NR_OF_TOKENS, 5, iter.next());
        assertFeature(NrOfTokensFeatureExtractor.FN_TOKENS_PER_SENTENCE, 5.0, iter.next());
    }
}