package de.tudarmstadt.ukp.dkpro.tc.features.pair.core;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import static de.tudarmstadt.ukp.dkpro.tc.features.pair.core.FeatureTestUtil.assertFeature;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class BaselinePairFeatureTest extends PairFeatureTestBase
{
	
    @Test
    public void extractTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class)
                
        );
        AnalysisEngine engine = createPrimitive(desc);

        BaselinePairFeatureExtractor extractor = new BaselinePairFeatureExtractor();
        List<Feature> features = runExtractor(engine, extractor);

        assertEquals(1, features.size());
        
        for (Feature feature : features) {
            assertFeature("BaselineFeature", 0, feature);
        }
    }
}