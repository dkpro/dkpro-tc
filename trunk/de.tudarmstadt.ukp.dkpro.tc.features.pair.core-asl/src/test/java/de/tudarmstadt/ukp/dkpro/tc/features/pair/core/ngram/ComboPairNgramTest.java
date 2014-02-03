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

public class ComboPairNgramTest
{
	CombinedNGramPairFeatureExtractor extractor;
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
        
        extractor = new CombinedNGramPairFeatureExtractor();
        extractor.ngramMinN1 = 1;
        extractor.ngramMinN2 = 1;
        extractor.ngramMinNAll = 1;
        extractor.ngramMinNCombo = 2;
        extractor.ngramMaxN1 = 3;
        extractor.ngramMaxN2 = 3;
        extractor.ngramMaxNAll = 3;
        extractor.ngramMaxNCombo = 4;
        extractor.useView1NgramsAsFeatures = false;
        extractor.useView2NgramsAsFeatures = false;
        extractor.useViewBlindNgramsAsFeatures = false;
        extractor.markViewBlindNgramsWithLocalView = false;
        extractor.ngramUseTopK1 = 500;
        extractor.ngramUseTopK2 = 500;
        extractor.ngramUseTopKAll = 500;
        extractor.ngramUseTopKCombo = 500;
        extractor.ngramFreqThreshold1 = 0.01f;
        extractor.ngramFreqThreshold2 = 0.01f;
        extractor.ngramFreqThresholdAll = 0.01f;
        extractor.ngramFreqThresholdCombo = 0.01f;
        extractor.ngramLowerCase = true;
        extractor.ngramMinTokenLengthThreshold = 1;
        extractor.prefix = "comboNG";
        
        extractor.stopwords = FeatureUtil.getStopwords(null, false);
        extractor.topKSetAll = makeSomeNgrams();
        extractor.topKSetView1 = makeSomeNgrams();
        extractor.topKSetView2 = makeSomeNgrams();
        extractor.topKSetCombo = makeSomeComboNgrams();
    }
    /**
     * Tests if Combo ngrams are being extracted as features.
     * 
     * @throws Exception
     */
    @Test
    public void ComboTypicalUseTest()
        throws Exception
    {
        initialize();
        extractor.ngramMinNCombo = 1;
        extractor.ngramMaxNCombo = 3;
        extractor.ngramLowerCase = true;
		List<Feature> features = extractor.extract(jcas, null);
		
		assertEquals(features.size(), 6);
		assertTrue(features.contains(new Feature("comboNG_birds_chase_cats", 0)));
		assertTrue(features.contains(new Feature("comboNG_cats_eat_birds", 1)));
        assertTrue(features.contains(new Feature("comboNG_birds_chase_birds", 0)));
        assertTrue(features.contains(new Feature("comboNG_cats_cats", 1)));
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
    	//with minN, maxN=3
        initialize();
        extractor.ngramMinNCombo = 3;
        extractor.ngramMaxNCombo = 3;
        extractor.ngramLowerCase = true;
        List<Feature> features = extractor.extract(jcas, null);

		assertTrue(features.contains(new Feature("comboNG_cats_eat_birds", 1)));
        assertTrue(features.contains(new Feature("comboNG_cats_cats", 0)));
        

    	//with minN, maxN=2
        initialize();
        extractor.ngramMinNCombo = 2;
        extractor.ngramMaxNCombo = 2;
        extractor.ngramLowerCase = true;
        features = extractor.extract(jcas, null);

		assertTrue(features.contains(new Feature("comboNG_cats_eat_birds", 0)));
        assertTrue(features.contains(new Feature("comboNG_cats_cats", 1)));
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
	private static FrequencyDistribution<String> makeSomeComboNgrams(){
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.addSample("cats_cats", 1);
        fd.addSample("cats_birds", 1);
        fd.addSample("cats_chase_cats", 1);
        fd.addSample("cats_eat_birds", 1);
        fd.addSample("birds_chase_cats", 1);
        fd.addSample("birds_chase_birds", 1);
		return fd;
	}

}