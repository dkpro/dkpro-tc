package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;

public class NGramFeatureExtractorTest
{
    LuceneNGramFeatureExtractor luceneExtractor;
    NGramFeatureExtractor nGramExtractor;
    JCas jcas;
    
    private void initialize() throws Exception{

        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);

        jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("Cats eats mice. Birds chase cats.");
        engine.process(jcas); // or engine.process(jcas)
        
        luceneExtractor = new LuceneNGramFeatureExtractor();
        nGramExtractor = new NGramFeatureExtractor();

        luceneExtractor.prefix = "ngram";
        nGramExtractor.prefix = "ngram";

        luceneExtractor.stopwords = FeatureUtil.getStopwords(null, false);
        nGramExtractor.stopwords = FeatureUtil.getStopwords(null, false);
        luceneExtractor.topKSet = makeSomeNgrams();
        nGramExtractor.topKSet = makeSomeNgrams();
    }
    
    
    @Test
    public void CompareOldAndNewPairFETest()
        throws Exception
    {
        initialize();
        luceneExtractor.ngramMinN = 1;
        luceneExtractor.ngramMaxN = 3;
        luceneExtractor.ngramLowerCase = true;
        
        List<Feature> luceneFeatures = luceneExtractor.extract(jcas, null);
          
        FrequencyDistribution<String> features = new FrequencyDistribution<String>();
          
        for(Feature f: luceneFeatures){
            features.addSample(f.getName(), 1);
        }
          
        
        nGramExtractor.ngramMinN = 1;
        nGramExtractor.ngramMaxN = 3;
        nGramExtractor.ngramLowerCase = true;
          
        List<Feature> ngramFeatures = nGramExtractor.extract(jcas, null);
        
        for(Feature f: ngramFeatures){
            features.addSample(f.getName(), 1);
        }
        assertEquals(features.getKeys().size(), 7);
        for(String sample: features.getKeys()){
            assertTrue(features.getCount(sample) == 2);
        }
    }
    @Test
    public void nonDefaultMinNMaxNTest()
        throws Exception
    {
        initialize();
        luceneExtractor.ngramMinN = 1;
        luceneExtractor.ngramMaxN = 1;
        luceneExtractor.ngramLowerCase = true;
        
        List<Feature> luceneFeatures = luceneExtractor.extract(jcas, null);

        assertTrue(luceneFeatures.contains(new Feature("ngram_cats", 1)));
        assertTrue(luceneFeatures.contains(new Feature("ngram_cats_eat", 0)));
        assertTrue(luceneFeatures.contains(new Feature("ngram_birds_chase_cats", 0)));
        
        initialize();
        luceneExtractor.ngramMinN = 3;
        luceneExtractor.ngramMaxN = 3;
        luceneExtractor.ngramLowerCase = true;
        
        luceneFeatures = luceneExtractor.extract(jcas, null);

        assertTrue(luceneFeatures.contains(new Feature("ngram_cats", 0)));
        assertTrue(luceneFeatures.contains(new Feature("ngram_cats_eat", 0)));
        assertTrue(luceneFeatures.contains(new Feature("ngram_birds_chase_cats", 1)));
    }
          
    
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
