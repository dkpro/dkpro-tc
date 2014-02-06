package de.tudarmstadt.ukp.dkpro.tc.features.style;

import static de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureTestUtil.assertFeature;
import static de.tudarmstadt.ukp.dkpro.tc.features.syntax.POSRatioFeatureExtractor.FN_N_RATIO;
import static de.tudarmstadt.ukp.dkpro.tc.features.syntax.POSRatioFeatureExtractor.FN_PUNC_RATIO;
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
import de.tudarmstadt.ukp.dkpro.tc.features.syntax.POSRatioFeatureExtractor;

public class POSRatioFeatureExtractorTest
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
        jcas.setDocumentText("As the emeritus pope leaves the Vatican for the papal residence of Castel Gandolfo – and becomes the first pontiff to resign in 600 years – the operation to choose his successor begins. With the throne of St Peter declared empty and the interregnum formally begun, as many of the 208 cardinals who can make the journey will be expected to travel to the Vatican to help run the church in the absence of a pope.");
        engine.process(jcas);

        POSRatioFeatureExtractor extractor = new POSRatioFeatureExtractor();
        List<Feature> features = extractor.extract(jcas);

        Assert.assertEquals(11, features.size());

        for (Feature feature : features) {
            if (feature.getName().equals(FN_N_RATIO)) {
                assertFeature(FN_N_RATIO, 0.2658, feature, 0.0001);
            }
            else if (feature.getName().equals(FN_PUNC_RATIO)) {
                assertFeature(FN_PUNC_RATIO, 0.0380, feature, 0.0001);
            }
        }
    }
}