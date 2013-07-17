package de.tudarmstadt.ukp.dkpro.tc.features.style;

import static de.tudarmstadt.ukp.dkpro.tc.features.syntax.SpellingErrorPOSRatioFeatureExtractor.FN_ART_ERROR_RATIO;
import static de.tudarmstadt.ukp.dkpro.tc.features.syntax.SpellingErrorPOSRatioFeatureExtractor.FN_N_ERROR_RATIO;
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

import de.tudarmstadt.ukp.dkpro.core.jazzy.SpellChecker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.features.syntax.SpellingErrorPOSRatioFeatureExtractor;

public class SpellingErrorPOSRatioFeatureExtractorTest
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
                ),
                createPrimitiveDescription(
                        SpellChecker.class,
                        SpellChecker.PARAM_MODEL_LOCATION, "src/test/resources/dictionary/en_US_dict.txt"
                )
        );
        AnalysisEngine engine = createPrimitive(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("As tthe pope leavess the Vatican for the papal residenze of Castel Gandolfo – and becomes the first pontiff to resign in 600 years – the operation to choose his successor begins.");
        engine.process(jcas);
        
        SpellingErrorPOSRatioFeatureExtractor extractor = new SpellingErrorPOSRatioFeatureExtractor();
        List<Feature> features = extractor.extract(jcas, null);

        Assert.assertEquals(11, features.size());
        
//        for (SpellingAnomaly anomaly : JCasUtil.select(jcas, SpellingAnomaly.class)) {
//            System.out.println(anomaly);
//            for (POS pos : JCasUtil.selectCovered(jcas, POS.class, anomaly)) {
//                System.out.println(pos);
//            }
//        }
        
        for (Feature feature : features) {
            if (feature.getName().equals(FN_ART_ERROR_RATIO)) {
                assertFeature(FN_ART_ERROR_RATIO, 0.1111, feature, 0.0001);
            }
            else if (feature.getName().equals(FN_N_ERROR_RATIO)) {
                assertFeature(FN_N_ERROR_RATIO, 0.3333, feature, 0.0001);
            }
        }
    }
}