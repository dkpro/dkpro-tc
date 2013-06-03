package de.tudarmstadt.ukp.dkpro.tc.features.pair.core;

import static de.tudarmstadt.ukp.dkpro.tc.features.util.FeatureTestUtil.assertFeature;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class BaselinePairFeatureExtractorTest extends PairFeatureTestBase
{
	@Ignore
    @Test
    public void baselinePairFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class)
                
        );
        AnalysisEngine engine = createPrimitive(desc);

        BaselinePairFeatureExtractor extractor = new BaselinePairFeatureExtractor();
        List<Feature> features = runExtractor(engine, extractor);

        Assert.assertEquals(1, features.size());
        
        for (Feature feature : features) {
            assertFeature("Baseline", 0, feature);
        }
    }
}