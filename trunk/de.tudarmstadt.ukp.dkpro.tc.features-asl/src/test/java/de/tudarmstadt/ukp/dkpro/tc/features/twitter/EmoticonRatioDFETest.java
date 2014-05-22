package de.tudarmstadt.ukp.dkpro.tc.features.twitter;

import static de.tudarmstadt.ukp.dkpro.tc.core.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTagger;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class EmoticonRatioDFETest
{
    @Test
    public void emoticonRatioFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createEngineDescription(
                ArktweetTagger.class, ArktweetTagger.PARAM_LANGUAGE, "en",
                ArktweetTagger.PARAM_VARIANT, "default");
        AnalysisEngine engine = createEngine(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a very emotional tweet ;-)");
        engine.process(jcas);

        EmoticonRatioDFE extractor = new EmoticonRatioDFE();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(1, features.size());

        for (Feature feature : features) {
            assertFeature(EmoticonRatioDFE.class.getSimpleName(), 0.14, feature, 0.01);
        }
    }
}
