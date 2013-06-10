package de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.ExternalResourceFactory.createExternalResourceDescription;

import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.similarity.dkpro.resource.lexical.string.GreedyStringTilingMeasureResource;

public class SimilarityPairFeatureTest
{
    
    private static final String VIEW1 = "view1";
    private static final String VIEW2 = "view2";
    
    public static class Annotator extends JCasAnnotator_ImplBase {
        final static String MODEL_KEY = "PairFeatureExtractorResource";
        @ExternalResource(key = MODEL_KEY)
        private PairFeatureExtractor model;

        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            System.out.println(model.getClass().getName());
            
            List<Feature> features;
            try {
                features = model.extract(aJCas.getView(VIEW1), aJCas.getView(VIEW2));
            }
            catch (CASException e) {
                throw new AnalysisEngineProcessException(e);
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
            Assert.assertEquals(1, features.size());
            
            Iterator<Feature> iter = features.iterator();
            System.out.println(iter.next());
            
        }
    }

    @Test
    public void configureAggregatedExample() throws Exception {
        ExternalResourceDescription gstResource = ExternalResourceFactory.createExternalResourceDescription(
                GreedyStringTilingMeasureResource.class,
                GreedyStringTilingMeasureResource.PARAM_MIN_MATCH_LENGTH, "3"
        );
        
        AnalysisEngineDescription desc = createPrimitiveDescription(
                      Annotator.class,
                      Annotator.MODEL_KEY, createExternalResourceDescription(
                              SimilarityPairFeatureExtractor.class,
                              SimilarityPairFeatureExtractor.PARAM_SEGMENT_FEATURE_PATH, Token.class.getName(),
                              SimilarityPairFeatureExtractor.PARAM_TEXT_SIMILARITY_RESOURCE, gstResource
                      )
       );
        
      AnalysisEngine engine = createPrimitive(desc);
      JCas jcas = engine.newJCas();
      TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);

      JCas view1 = jcas.createView(VIEW1);
      view1.setDocumentLanguage("en");
      tb.buildTokens(view1, "This is a test .");
      
      JCas view2 = jcas.createView(VIEW2);
      view2.setDocumentLanguage("en");
      tb.buildTokens(view2, "Test is this .");

      engine.process(jcas);
    }
}