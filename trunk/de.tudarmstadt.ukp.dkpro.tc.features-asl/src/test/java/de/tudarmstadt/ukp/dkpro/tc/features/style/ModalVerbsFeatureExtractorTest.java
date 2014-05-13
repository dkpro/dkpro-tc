package de.tudarmstadt.ukp.dkpro.tc.features.style;

import static de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class ModalVerbsFeatureExtractorTest
{
    @Test
    public void modalVerbsFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        "en"));
        AnalysisEngine engine = createEngine(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("I can. I could. You might. You may. I must. He should. He must. We will. They would. You shall.");
        engine.process(jcas);

        ModalVerbsFeatureExtractor extractor = new ModalVerbsFeatureExtractor();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(11, features.size());

        Iterator<Feature> iter = features.iterator();
        assertFeature(ModalVerbsFeatureExtractor.FN_CAN, 10.0, iter.next());
        assertFeature(ModalVerbsFeatureExtractor.FN_COULD, 10.0, iter.next());
        assertFeature(ModalVerbsFeatureExtractor.FN_MIGHT, 10.0, iter.next());
        assertFeature(ModalVerbsFeatureExtractor.FN_MAY, 10.0, iter.next());
        assertFeature(ModalVerbsFeatureExtractor.FN_MUST, 20.0, iter.next());
        assertFeature(ModalVerbsFeatureExtractor.FN_SHOULD, 10.0, iter.next());
        assertFeature(ModalVerbsFeatureExtractor.FN_WILL, 10.0, iter.next());
        assertFeature(ModalVerbsFeatureExtractor.FN_WOULD, 10.0, iter.next());
        assertFeature(ModalVerbsFeatureExtractor.FN_SHALL, 10.0, iter.next());
        assertFeature(ModalVerbsFeatureExtractor.FN_ALL, 100.0, iter.next()); // all verbs are modal
                                                                              // here
        assertFeature(ModalVerbsFeatureExtractor.FN_UNCERT, 70.0, iter.next()); // 70% of the verbs
                                                                                // express
                                                                                // uncertainty

    }
}