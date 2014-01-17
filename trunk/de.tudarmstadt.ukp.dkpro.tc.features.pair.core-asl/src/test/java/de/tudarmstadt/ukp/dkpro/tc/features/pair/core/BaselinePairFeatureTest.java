package de.tudarmstadt.ukp.dkpro.tc.features.pair.core;

import static de.tudarmstadt.ukp.dkpro.tc.features.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;

public class BaselinePairFeatureTest
    extends PairFeatureTestBase
{

    @Test
    public void extractTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class)

                );
        AnalysisEngine engine = createPrimitive(desc);

        PairFeatureExtractor extractor = new AlwaysZeroPairFeatureExtractor();
        List<IFeature> features = runExtractor(engine, extractor);

        assertEquals(1, features.size());

        for (IFeature feature : features) {
            assertFeature("BaselineFeature", 0, feature);
        }
    }
}