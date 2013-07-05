package de.tudarmstadt.ukp.dkpro.tc.features.style;

import static de.tudarmstadt.ukp.dkpro.tc.features.style.TokenRatioFeatureExtractor.FN_TOKEN_RATIO;
import static de.tudarmstadt.ukp.dkpro.tc.features.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class TokenRatioFeatureExtractorTest
{
    @Test
    public void posContextFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class)
                
        );
        AnalysisEngine engine = createPrimitive(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("He is no tester. I am a Tester.");
        engine.process(jcas);
        
        String token = "tester";
        TokenRatioFeatureExtractor extractor = new TokenRatioFeatureExtractor(token);
        List<Feature> features = extractor.extract(jcas, null);

        Assert.assertEquals(1, features.size());
        
        for (Feature feature : features) {
            assertFeature(FN_TOKEN_RATIO + "_" + token, 0.2, feature);
        }
    }
}