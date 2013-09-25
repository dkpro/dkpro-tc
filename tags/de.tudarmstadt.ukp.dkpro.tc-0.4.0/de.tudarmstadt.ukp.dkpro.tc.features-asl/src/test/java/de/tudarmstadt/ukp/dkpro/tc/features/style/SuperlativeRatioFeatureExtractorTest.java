package de.tudarmstadt.ukp.dkpro.tc.features.style;

import static de.tudarmstadt.ukp.dkpro.tc.features.syntax.SuperlativeRatioFeatureExtractor.FN_SUPERLATIVE_RATIO_ADJ;
import static de.tudarmstadt.ukp.dkpro.tc.features.syntax.SuperlativeRatioFeatureExtractor.FN_SUPERLATIVE_RATIO_ADV;
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

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.features.syntax.SuperlativeRatioFeatureExtractor;

public class SuperlativeRatioFeatureExtractorTest
{
    @Test
    public void posContextFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class),
                createPrimitiveDescription(
                        OpenNlpPosTagger.class,
                        OpenNlpPosTagger.PARAM_LANGUAGE, "en"
                )

                );
        AnalysisEngine engine = createPrimitive(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a normal test. This is the best, biggest, and greatest test ever.");
        engine.process(jcas);

        SuperlativeRatioFeatureExtractor extractor = new SuperlativeRatioFeatureExtractor();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(2, features.size());

        for (Feature feature : features) {
            if (feature.getName().equals(FN_SUPERLATIVE_RATIO_ADJ)) {
                assertFeature(FN_SUPERLATIVE_RATIO_ADJ, 0.75, feature);
            }
            else if (feature.getName().equals(FN_SUPERLATIVE_RATIO_ADV)) {
                assertFeature(FN_SUPERLATIVE_RATIO_ADV, 0.0, feature);
            }
        }
    }
}