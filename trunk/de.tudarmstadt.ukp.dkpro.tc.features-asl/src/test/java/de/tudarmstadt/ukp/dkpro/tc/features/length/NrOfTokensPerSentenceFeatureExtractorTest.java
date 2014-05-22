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
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue.MissingValueNonNominalType;

public class NrOfTokensPerSentenceFeatureExtractorTest
{
    @Test
    public void nrOfTokensPerSentenceFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test.");
        engine.process(jcas);

        NrOfTokensPerSentenceDFE extractor = new NrOfTokensPerSentenceDFE();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(1, features.size());

        Iterator<Feature> iter = features.iterator();
        assertFeature(NrOfTokensPerSentenceDFE.FN_TOKENS_PER_SENTENCE, 5., iter.next());
    }

    @Test
    public void nrOfTokensPerSentenceExteremeFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("");
        engine.process(jcas);

        NrOfTokensPerSentenceDFE extractor = new NrOfTokensPerSentenceDFE();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(1, features.size());

        Iterator<Feature> iter = features.iterator();
        assertFeature(NrOfTokensPerSentenceDFE.FN_TOKENS_PER_SENTENCE, new MissingValue(
                MissingValueNonNominalType.NUMERIC),
                iter.next());
    }
}