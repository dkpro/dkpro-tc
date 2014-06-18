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

public class InitialCharacterUpperCaseFeatureExtractorTest
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
            unit1.setBegin(0);
            unit1.setEnd(2);

            TextClassificationUnit unit2 = new TextClassificationUnit(jcas);
            unit2.setBegin(3);
            unit2.setEnd(5);

            List<Feature> features1;
            try {
                features1 = model.extract(jcas, unit1);

                Assert.assertEquals(1, features1.size());
                for (Feature feature : features1) {
                    assertFeature(InitialCharacterUpperCaseUFE.INITIAL_CH_UPPER_CASE, true, feature);
                }

                List<Feature> features2 = model.extract(jcas, unit2);
                Assert.assertEquals(1, features2.size());
                for (Feature feature : features2) {
                    assertFeature(InitialCharacterUpperCaseUFE.INITIAL_CH_UPPER_CASE, false, feature);
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
                        createExternalResourceDescription(InitialCharacterUpperCaseUFE.class)));

        AnalysisEngine engine = createEngine(desc);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("It is a very unusual test");

        engine.process(jcas);
    }

}