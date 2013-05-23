package de.tudarmstadt.ukp.dkpro.tc.features.style;

import static de.tudarmstadt.ukp.dkpro.tc.features.style.ContextualityMeasureFeatureExtractor.CONTEXTUALITY_MEASURE_FN;
import static de.tudarmstadt.ukp.dkpro.tc.features.util.FeatureTestUtil.assertFeature;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class ContextualityFeatureExtractorTest
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
        jcas.setDocumentText("This is a test.");
        engine.process(jcas);
        
        ContextualityMeasureFeatureExtractor extractor = new ContextualityMeasureFeatureExtractor();
        List<Feature> features = extractor.extract(jcas, null);

        Assert.assertEquals(9, features.size());
        
        for (Feature feature : features) {
            if (feature.getName().equals(CONTEXTUALITY_MEASURE_FN)) {
                // TODO not sure how to interpret this number
                assertFeature(CONTEXTUALITY_MEASURE_FN, 50.2, feature);
            }
        }
    }
}