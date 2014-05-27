package de.tudarmstadt.ukp.dkpro.tc.features.style;

import static de.tudarmstadt.ukp.dkpro.tc.core.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;

import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class IsSurroundedByCharsFeatureExtractorTest
{

    public static class Annotator
        extends JCasAnnotator_ImplBase
    {
        final static String MODEL_KEY = "UnitFeatureExtractorResource";
        @ExternalResource(key = MODEL_KEY)
        private ClassificationUnitFeatureExtractor model;

        @Override
        public void process(JCas jcas)
            throws AnalysisEngineProcessException
        {

            TextClassificationUnit unit1 = new TextClassificationUnit(jcas);
            unit1.setBegin(10);
            unit1.setEnd(11);

            TextClassificationUnit unit2 = new TextClassificationUnit(jcas);
            unit2.setBegin(32);
            unit2.setEnd(35);

            List<Feature> features1;
            try {
                features1 = model.extract(jcas, unit1);

                Assert.assertEquals(1, features1.size());
                for (Feature feature : features1) {
                    assertFeature(IsSurroundedByCharsUFE.SURROUNDED_BY_CHARS, false, feature);
                }

                List<Feature> features2 = model.extract(jcas, unit2);
                Assert.assertEquals(1, features2.size());

                for (Feature feature : features2) {
                    assertFeature(IsSurroundedByCharsUFE.SURROUNDED_BY_CHARS, true, feature);
                }

            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    @Test
    public void configureAggregatedExample()
        throws Exception
    {

        AnalysisEngineDescription desc = createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(
                        Annotator.class,
                        Annotator.MODEL_KEY,
                        createExternalResourceDescription(IsSurroundedByCharsUFE.class,
                                IsSurroundedByCharsUFE.PARAM_SURROUNDING_CHARS, "\"\"")));

        AnalysisEngine engine = createEngine(desc);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("He said: \"I am a Tester.\" That \"was\" all.");

        engine.process(jcas);
    }

}