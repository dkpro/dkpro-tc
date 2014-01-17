package de.tudarmstadt.ukp.dkpro.tc.features.length;

import static de.tudarmstadt.ukp.dkpro.tc.features.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;

public class NrOfCharsFeatureExtractorTest
{
    @Test
    public void nrOfCharsFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class)
                );
        AnalysisEngine engine = createPrimitive(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test. This is a test.");
        engine.process(jcas);

        NrOfCharsFeatureExtractor extractor = new NrOfCharsFeatureExtractor();
        List<IFeature> features = extractor.extract(jcas);

        Assert.assertEquals(3, features.size());

        Iterator<IFeature> iter = features.iterator();
        assertFeature(NrOfCharsFeatureExtractor.FN_NR_OF_CHARS, 31, iter.next());
        assertFeature(NrOfCharsFeatureExtractor.FN_NR_OF_CHARS_PER_SENTENCE, 15.5, iter.next());
        assertFeature(NrOfCharsFeatureExtractor.FN_NR_OF_CHARS_PER_TOKEN, 3.1, iter.next());
    }
}