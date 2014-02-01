package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;

public class PairNgramTest
{
    LuceneNGramPairFeatureExtractor extractor;
    JCas jcas;
    
    private void initialize()
            throws Exception
    {
        AnalysisEngineDescription seg = createPrimitiveDescription(
                BreakIteratorSegmenter.class
                );
        AnalysisEngine engine = createPrimitive(seg);

        AggregateBuilder builder = new AggregateBuilder();
        builder.add(seg, AbstractPairReader.INITIAL_VIEW, AbstractPairReader.PART_ONE);
        builder.add(seg, AbstractPairReader.INITIAL_VIEW, AbstractPairReader.PART_TWO);

        jcas = engine.newJCas();
        JCas view1 = jcas.createView(AbstractPairReader.PART_ONE);
        JCas view2 = jcas.createView(AbstractPairReader.PART_TWO);
        view1.setDocumentLanguage("en");
        view2.setDocumentLanguage("en");
        view1.setDocumentText("Cats eat mice.");
        view2.setDocumentText("Birds chase cats.");
        
        SimplePipeline.runPipeline(jcas,
                builder.createAggregateDescription()
                );
        
        extractor = new LuceneNGramPairFeatureExtractor();
        extractor.ngramMinN1 = 1;
        extractor.ngramMinN2 = 1;
        extractor.ngramMinNAll = 1;
        extractor.ngramMaxN1 = 3;
        extractor.ngramMaxN2 = 3;
        extractor.ngramMaxNAll = 3;
        extractor.useView1NgramsAsFeatures = false;
        extractor.useView2NgramsAsFeatures = false;
        extractor.useViewBlindNgramsAsFeatures = false;
        extractor.markViewBlindNgramsWithLocalView = false;
        extractor.ngramUseTopK1 = 500;
        extractor.ngramUseTopK2 = 500;
        extractor.ngramUseTopKAll = 500;
        extractor.ngramFreqThreshold1 = 0.01f;
        extractor.ngramFreqThreshold2 = 0.01f;
        extractor.ngramFreqThresholdAll = 0.01f;
        extractor.ngramLowerCase = true;
        extractor.ngramMinTokenLengthThreshold = 1;
        extractor.stopwords = FeatureUtil.getStopwords(null, false);
        extractor.topKSetAll = makeSomeNgrams();
        extractor.topKSetView1 = makeSomeNgrams();
        extractor.topKSetView2 = makeSomeNgrams();
    }
    /**
     * Tests if just View1 ngrams are being extracted as features.
     * 
     * @throws Exception
     */
    @Test
    public void View1TypicalUseTest()
        throws Exception
    {
        initialize();
        extractor.ngramMinN1 = 1;
        extractor.ngramMaxN1 = 3;
        extractor.useView1NgramsAsFeatures = true;
        extractor.ngramLowerCase = true;

		List<Feature> features = extractor.extract(jcas, null);
		assertEquals(features.size(), 7);
		assertTrue(features.contains(new Feature("view1NG_birds_chase_cats", 0)));
        assertTrue(features.contains(new Feature("view1NG_cats_eat_mice", 1)));
        assertTrue(features.contains(new Feature("view1NG_cats", 1)));
    }
    /**
     * Tests if just View2 ngrams are being extracted as features
     * 
     * @throws Exception
     */
    @Test
    public void View2TypicalUseTest()
        throws Exception
    {
        initialize();
        extractor.ngramMinN2 = 1;
        extractor.ngramMaxN2 = 3;
        extractor.useView2NgramsAsFeatures = true;
        extractor.ngramLowerCase = true;
        List<Feature> features = extractor.extract(jcas, null);
        
        assertEquals(features.size(), 7);
        assertTrue(features.contains(new Feature("view2NG_birds_chase_cats", 1)));
        assertTrue(features.contains(new Feature("view2NG_cats", 1)));
    }
    /**
     * Tests if both View1 and View2 ngrams are being extracted as features.
     * 
     * @throws Exception
     */
    @Test
    public void ViewBlindTypicalUseTest()
        throws Exception
    {
        initialize();
        extractor.ngramMinN1 = 1;
        extractor.ngramMaxN1 = 3;
        extractor.useViewBlindNgramsAsFeatures = true;
        extractor.ngramLowerCase = true;

        List<Feature> features = extractor.extract(jcas, null);
        
        assertEquals(features.size(), 7);
        assertTrue(features.contains(new Feature("allNG_birds_chase_cats", 1)));
        assertTrue(features.contains(new Feature("allNG_cats_eat_mice", 1)));
        assertTrue(features.contains(new Feature("allNG_cats", 1)));
    }
    /**
     * Tests ngramLowerCase
     * 
     * @throws Exception
     */
    @Test
    public void AlphaCaseTest()
        throws Exception
    {
        initialize();
        extractor.ngramMinN2 = 1;
        extractor.ngramMaxN2 = 3;
        extractor.useView2NgramsAsFeatures = true;
        extractor.ngramLowerCase = false;
        List<Feature> features = extractor.extract(jcas, null);
        
        assertTrue(features.contains(new Feature("view2NG_birds_chase_cats", 0)));
        assertTrue(features.contains(new Feature("view2NG_Birds_chase_cats", 1)));

        initialize();
        extractor.ngramMinN2 = 1;
        extractor.ngramMaxN2 = 3;
        extractor.useView2NgramsAsFeatures = true;
        extractor.ngramLowerCase = true;
        features = extractor.extract(jcas, null);
        
        assertTrue(features.contains(new Feature("view2NG_birds_chase_cats", 1)));
        assertTrue(features.contains(new Feature("view2NG_Birds_chase_cats", 0)));
    }
    /**
     * Tests non-default minN and maxN size values
     * 
     * @throws Exception
     */
    @Test
    public void MinMaxSizeTest()
        throws Exception
    {
        initialize();
        extractor.ngramMinN1 = 2;
        extractor.ngramMaxN1 = 2;
        extractor.useView1NgramsAsFeatures = true;
        extractor.ngramLowerCase = true;
        List<Feature> features = extractor.extract(jcas, null);

        assertTrue(features.contains(new Feature("view1NG_cats_eat_mice", 0)));
        assertTrue(features.contains(new Feature("view1NG_cats_eat", 1)));
        assertTrue(features.contains(new Feature("view1NG_cats", 0)));
    }
    /**
     * Tests the stopword list to make sure stopwords are not in ngrams.
     * 
     * @throws Exception
     */
  @Test
  public void LeaveOutStopwordsTest()
      throws Exception
  {
      initialize();
      extractor.ngramMinN2 = 1;
      extractor.ngramMaxN2 = 3;
      extractor.useView2NgramsAsFeatures = true;
      extractor.ngramLowerCase = true;
      extractor.stopwords = new HashSet<String>();
      extractor.stopwords.add("cats");
      List<Feature> features = extractor.extract(jcas, null);

      assertEquals(features.size(), 7);
      assertTrue(features.contains(new Feature("view2NG_birds_chase_cats", 0)));
      assertTrue(features.contains(new Feature("view2NG_cats", 0)));
      assertTrue(features.contains(new Feature("view2NG_birds", 1)));
  }
  /**
   * Makes a FD of "filtered ngrams from whole corpus." Not really filtered
   * by params in this test suite.  Each of these will always be a final feature;
   * but have values according to whether they also occur in a set of ngrams
   * for the view/jcas in question, which <b>has</b> been filtered by params
   * in this test suite.
   * 
   * @return ngrams to represent a set of ngrams from the whole corpus
   */
    private static FrequencyDistribution<String> makeSomeNgrams(){
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.addSample("cats", 2);
        fd.addSample("birds", 1);
        fd.addSample("dogs", 4);
        fd.addSample("cats_eat", 5);
        fd.addSample("cats_eat_mice", 1);
        fd.addSample("birds_chase_cats", 2);
        fd.addSample("Birds_chase_cats", 2);
        return fd;
    }
	

}