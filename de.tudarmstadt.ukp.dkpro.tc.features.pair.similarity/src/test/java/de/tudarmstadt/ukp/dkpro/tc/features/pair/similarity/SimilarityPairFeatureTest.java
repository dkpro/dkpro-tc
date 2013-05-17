package de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.cleartk.classifier.Feature;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.component.initialize.ExternalResourceInitializer;
import org.uimafit.factory.ExternalResourceFactory;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.similarity.dkpro.resource.lexical.string.GreedyStringTilingMeasureResource;

public class SimilarityPairFeatureTest
{
    @Ignore
    @Test
    public void similarityPairFeatureTest()
        throws Exception
    {
        ExternalResourceDescription gstResource = ExternalResourceFactory.createExternalResourceDescription(
                GreedyStringTilingMeasureResource.class,
                GreedyStringTilingMeasureResource.PARAM_MIN_MATCH_LENGTH, "3"
        );
        
        AnalysisEngineDescription desc = createAggregateDescription(
                createPrimitiveDescription(
                        BreakIteratorSegmenter.class,
                        SimilarityPairFeatureExtractor.PARAM_SEGMENT_FEATURE_PATH, Token.class.getName(),
                        SimilarityPairFeatureExtractor.PARAM_TEXT_SIMILARITY_RESOURCE, gstResource
                )
        );
        AnalysisEngine engine = createPrimitive(desc);

        JCas jcas1 = engine.newJCas();
        jcas1.setDocumentLanguage("en");
        jcas1.setDocumentText("This is a test.");
        engine.process(jcas1);
        
        JCas jcas2 = engine.newJCas();
        jcas2.setDocumentLanguage("en");
        jcas2.setDocumentText("Test is this.");
        engine.process(jcas2);

        SimilarityPairFeatureExtractor extractor = new SimilarityPairFeatureExtractor();
        ConfigurationParameterInitializer.initialize(extractor, engine.getUimaContext());
        ExternalResourceInitializer.initialize(engine.getUimaContext(), extractor);

        List<Feature> features = extractor.extract(jcas1, jcas2);

        Assert.assertEquals(1, features.size());
        
        Iterator<Feature> iter = features.iterator();
        System.out.println(iter.next());
    }
}