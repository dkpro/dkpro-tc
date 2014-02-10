package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;


public class ComboPairNgramTest
{
//	CombinedNGramPairFeatureExtractor extractor;
//    JCas jcas;
//    
//    private void initialize()
//            throws Exception
//    {
//        AnalysisEngineDescription seg = createEngineDescription(
//                BreakIteratorSegmenter.class
//        );
//        AnalysisEngine engine = createEngine(seg);
//
//        AggregateBuilder builder = new AggregateBuilder();
//        builder.add(seg, AbstractPairReader.INITIAL_VIEW, AbstractPairReader.PART_ONE);
//        builder.add(seg, AbstractPairReader.INITIAL_VIEW, AbstractPairReader.PART_TWO);
//
//        jcas = engine.newJCas();
//        JCas view1 = jcas.createView(AbstractPairReader.PART_ONE);
//        JCas view2 = jcas.createView(AbstractPairReader.PART_TWO);
//        view1.setDocumentLanguage("en");
//        view2.setDocumentLanguage("en");
//        view1.setDocumentText("Cats eat mice.");
//        view2.setDocumentText("Birds chase cats.");
//        
//        SimplePipeline.runPipeline(jcas,
//                builder.createAggregateDescription()
//                );
//        
//        extractor = new CombinedNGramPairFeatureExtractor();
//        extractor.ngramMinN1 = 1;
//        extractor.ngramMinN2 = 1;
//        extractor.ngramMinNAll = 1;
//        extractor.ngramMinNCombo = 2;
//        extractor.ngramMaxN1 = 3;
//        extractor.ngramMaxN2 = 3;
//        extractor.ngramMaxNAll = 3;
//        extractor.ngramMaxNCombo = 4;
//        extractor.useView1NgramsAsFeatures = false;
//        extractor.useView2NgramsAsFeatures = false;
//        extractor.useViewBlindNgramsAsFeatures = false;
//        extractor.markViewBlindNgramsWithLocalView = false;
//        extractor.ngramUseTopK1 = 500;
//        extractor.ngramUseTopK2 = 500;
//        extractor.ngramUseTopKAll = 500;
//        extractor.ngramUseTopKCombo = 500;
//        extractor.ngramFreqThreshold1 = 0.01f;
//        extractor.ngramFreqThreshold2 = 0.01f;
//        extractor.ngramFreqThresholdAll = 0.01f;
//        extractor.ngramFreqThresholdCombo = 0.01f;
//        extractor.ngramLowerCase = true;
//        extractor.ngramMinTokenLengthThreshold = 1;
//        extractor.prefix = "comboNG";
//        
//        extractor.stopwords = FeatureUtil.getStopwords(null, false);
//        extractor.topKSetAll = makeSomeNgrams();
//        extractor.topKSetView1 = makeSomeNgrams();
//        extractor.topKSetView2 = makeSomeNgrams();
//        extractor.topKSetCombo = makeSomeComboNgrams();
//    }
//    /**
//     * Tests if Combo ngrams are being extracted as features.
//     * 
//     * @throws Exception
//     */
//    @Test
//    public void ComboTypicalUseTest()
//        throws Exception
//    {
//        initialize();
//		List<Feature> features = extractor.extract(jcas, null);
//		
//		assertEquals(features.size(), 7);
//		assertTrue(features.contains(new Feature("comboNG_birds_chase_cats", 0)));
//		assertTrue(features.contains(new Feature("comboNG_cats_eat_birds", 1)));
//        assertTrue(features.contains(new Feature("comboNG_birds_chase_birds", 0)));
//        assertTrue(features.contains(new Feature("comboNG_cats_cats", 1)));
//    }
//    /**
//     * Tests non-default {@link CombinedNGramPairFeatureExtractor#ngramMinNCombo} and 
//     * {@link CombinedNGramPairFeatureExtractor#ngramMaxNCombo} values.
//     * 
//     * @throws Exception
//     */
//    @Test
//    public void MinMaxSizeTest()
//        throws Exception
//    {
//    	//with minN, maxN=3
//        initialize();
//        extractor.ngramMinNCombo = 3;
//        extractor.ngramMaxNCombo = 3;
//        List<Feature> features = extractor.extract(jcas, null);
//
//		assertTrue(features.contains(new Feature("comboNG_cats_eat_birds", 1)));
//        assertTrue(features.contains(new Feature("comboNG_cats_cats", 0)));
//        
//
//    	//with minN, maxN=2
//        initialize();
//        extractor.ngramMinNCombo = 2;
//        extractor.ngramMaxNCombo = 2;
//        features = extractor.extract(jcas, null);
//
//		assertTrue(features.contains(new Feature("comboNG_cats_eat_birds", 0)));
//        assertTrue(features.contains(new Feature("comboNG_cats_cats", 1)));
//    }
//    /**
//     * Tests the parameter {@link CombinedNGramPairFeatureExtractor#ngramUseSymmetricalCombos}.
//     * @throws Exception
//     */
//    @Test
//    public void SymmetryTest() throws Exception{
//    	//without symmetry
//        initialize();
//        extractor.ngramUseSymmetricalCombos = false;
//        List<Feature> features = extractor.extract(jcas, null);
//
//		assertTrue(features.contains(new Feature("comboNG_birds_cats", 0)));
//        assertTrue(features.contains(new Feature("comboNG_cats_birds", 1)));
//        
//
//    	//with symmetry
//        initialize();
//        extractor.ngramUseSymmetricalCombos = true;
//        features = extractor.extract(jcas, null);
//
//		assertTrue(features.contains(new Feature("comboNG_birds_cats", 1)));
//        assertTrue(features.contains(new Feature("comboNG_cats_birds", 1)));
//    }
//  /**
//   * Makes a FD of "filtered ngrams from whole corpus." Not really filtered
//   * by params in this test suite.  Each of these will always be a final feature;
//   * but have values according to whether they also occur in a set of ngrams
//   * for the view/jcas in question, which <b>has</b> been filtered by params
//   * in this test suite.
//   * 
//   * @return ngrams to represent a set of ngrams from the whole corpus
//   */
//    private static FrequencyDistribution<String> makeSomeNgrams(){
//        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
//        fd.addSample("cats", 2);
//        fd.addSample("birds", 1);
//        fd.addSample("dogs", 4);
//        fd.addSample("cats_eat", 5);
//        fd.addSample("cats_eat_mice", 1);
//        fd.addSample("birds_chase_cats", 2);
//        fd.addSample("Birds_chase_cats", 2);
//        return fd;
//    }
//    /**
//     * Makes a FD of "filtered combo ngrams from whole corpus." Not really filtered
//   * by params in this test suite.  Each of these will always be a final feature;
//   * but have values according to whether they also occur in a set of ngrams
//   * for the view/jcas in question, which <b>has</b> been filtered by params
//   * in this test suite.
//     * @return combo ngrams to represent a set of combo ngrams from the whole corpus
//     */
//	private static FrequencyDistribution<String> makeSomeComboNgrams(){
//        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
//        fd.addSample("cats_cats", 1);
//        fd.addSample("cats_birds", 1);
//        fd.addSample("birds_cats", 1);
//        fd.addSample("cats_chase_cats", 1);
//        fd.addSample("cats_eat_birds", 1);
//        fd.addSample("birds_chase_cats", 1);
//        fd.addSample("birds_chase_birds", 1);
//		return fd;
//	}

}