package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length.DiffNrOfCharactersPairFeatureExtractor;

public class PairNgramTest
{
    @Test
    public void NgramTest()
        throws Exception
    {
		AnalysisEngineDescription desc = createAggregateDescription(
		        createPrimitiveDescription(BreakIteratorSegmenter.class)
		);
		AnalysisEngine engine = createPrimitive(desc);


		JCas jcas = engine.newJCas();
		JCas view1 = jcas.createView(AbstractPairReader.PART_ONE);
		JCas view2 = jcas.createView(AbstractPairReader.PART_TWO);
		view1.setDocumentLanguage("en");
		view1.setDocumentText("Cats eats mice.");
		view2.setDocumentLanguage("en");
		view2.setDocumentText("Birds chase cats.");
		// this part doesn't work yet
//		engine.process(view1);
//		engine.process(view2);
		
		
		
		
		LuceneNGramPairFeatureExtractor extractor = new LuceneNGramPairFeatureExtractor();
		List<Feature> features = extractor.extract(jcas, null);

		for (Feature feature : features) {
			System.out.println(feature.getName());
		}
    }
	
	

}
