package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * Pair-wise feature extractor
 * Returns all combinations of qualified ngrams from each of the two views.
 * An ngram-pair's value is 1 if each of the ngrams appeared in their 
 * respective text, and 0 otherwise.
 * @author Emily Jamison
 *
 */
public class CombinedNGramPairFeatureExtractor
    extends LuceneNGramPairFeatureExtractor
{    
    public static final String PARAM_NGRAM_MIN_N_COMBO = "ngramMinNCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N_COMBO, mandatory = false, defaultValue = "1")
    protected int ngramMinNCombo;
   
    public static final String PARAM_NGRAM_MAX_N_COMBO = "ngramMaxNCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N_COMBO, mandatory = false, defaultValue = "3")
    protected int ngramMaxNCombo;
    
    public static final String PARAM_NGRAM_USE_TOP_K_COMBO = "ngramUseTopKCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_COMBO, mandatory = false, defaultValue = "500")
    protected int ngramUseTopKCombo;    

    public static final String PARAM_NGRAM_FREQ_THRESHOLD_COMBO = "ngramFreqThresholdCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD_COMBO, mandatory = false, defaultValue = "0.01")
    protected float ngramFreqThresholdCombo;


    protected Set<String> topKSetCombo;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        topKSetCombo = getTopNgramsCombo();
        
        prefix = "ngrams_";

        return true;
    }
    
    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {     
        JCas view1;
        JCas view2;
        try{
            view1 = jcas.getView("PART_ONE");
            view2 = jcas.getView("PART_TWO");   
        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }
        
        FrequencyDistribution<String> view1Ngrams = null;
        FrequencyDistribution<String> view2Ngrams = null;
//        FrequencyDistribution<String> pairNgrams = null;
                    
        if (classificationUnit == null) {
            view1Ngrams = NGramUtils.getDocumentNgrams(view1,
                    ngramLowerCase, ngramMinN1, ngramMaxN1, stopwords);
            view2Ngrams = NGramUtils.getDocumentNgrams(view2, 
                    ngramLowerCase, ngramMinN2, ngramMaxN2, stopwords);
        }
        else {
            view1Ngrams = NGramUtils.getAnnotationNgrams(view1, classificationUnit,
                    ngramLowerCase, ngramMinN1, ngramMaxN1, stopwords);
            view2Ngrams = NGramUtils.getAnnotationNgrams(view2, classificationUnit,
                    ngramLowerCase, ngramMinN2, ngramMaxN2, stopwords);
        }
// FIXME something sensible that this could return here without using combos?
//        FrequencyDistribution<String> pairNgrams = NGramUtils.getCombinedNgrams(view1Ngrams,
//                view2Ngrams, ngramMinNCombo, ngramMinNCombo);
//         
        List<Feature> features = new ArrayList<Feature>();
//        for(String pairNgram: topKSetCombo){
//            String featureName = prefix + "_" + pairNgram;
//            if (pairNgrams.contains(pairNgram)) {
//                features.add(new Feature(featureName, 1));
//            }
//            else {
//                features.add(new Feature(featureName, 0));
//            }
//        }
        
        return features;
    }
    
//    public List<Feature> extract(JCas view1, JCas view2)
//        throws TextClassificationException
//    {
//        List<Feature> features = new ArrayList<Feature>();
//        
//        prefix = new String("");
//        
//        List<Feature> view1Ngrams = super.extract(view1, null);
//        List<Feature> view2Ngrams = super.extract(view2, null);
//        
//        for(Feature ngram1: view1Ngrams){
//        	for(Feature ngram2: view2Ngrams){
//        		String featureName = "comboNg"+ngram1.getName()+ngram2.getName();
//        		Object featureValue = 0;
//        		if(ngram1.getValue().toString().equals("1") && ngram2.getValue().toString().equals("1")){
//        			featureValue = 1;
//        		}
//        		
//        		System.out.println("New pair ngram: " + featureName + "  featureValue: " + featureValue);
//        		features.add(new Feature(featureName, featureValue));
//        	}
//        }
//        return features;
//    }
//
    
    private static Set<String> getTopNgramsCombo()
        throws ResourceInitializationException
    {

      Set<String> topNGramsCombo = new HashSet<String>();
//      
//      PriorityQueue<TermFreqTuple> topN = new TermFreqQueue(ngramUseTopKCombo);
//      try{
//          IndexReader reader = DirectoryReader.open(FSDirectory.open(luceneDir));
//          
//          IndexSearcher is = new IndexSearcher(reader);
//          Query query = new MatchAllDocsQuery();
//          
//          TopDocs topDocs = is.search(query, reader.maxDoc());
//          ScoreDoc[] hits = topDocs.scoreDocs;
//          for (ScoreDoc hit : hits) {
//                int docId = hit.doc;
//                Document d = is.doc(docId);
//                String[] ngramArray1 = d.getValues(LUCENE_NGRAM_FIELD1);
//                String[] ngramArray2 = d.getValues(LUCENE_NGRAM_FIELD2);
//                for(String ngram1: ngramArray1){
//                    if (topK1.contains(ngram1) && topKSetAll.contains(ngram1)){
//                        for(String ngram2: ngramArray2){
//                            if (topKSetView2.contains(ngram2) && topKSetAll.contains(ngram2)){
//                                int combinedSize = ngram1.split("_").length + ngram2.split("_").length;
//                                if(combinedSize <= ngramMaxNCombo && combinedSize >= ngramMaxNCombo){
//                                    topN.insertWithOverflow(new TermFreqTuple(combo(ngram1, ngram2), 1));
//                                }
//                            }
//                        }
//                    }
//                }
//          }
//      }
//      catch(IOException e){
//          throw new ResourceInitializationException();
//      }
//
//      for (int i=0; i < topN.size(); i++) {
//          TermFreqTuple tuple = topN.pop();
////          System.out.println(tuple.getTerm() + " - " + tuple.getFreq());
//          topNGramsCombo.add(tuple.getTerm());
//      }
      
      return topNGramsCombo;
    }
    
    private String combo(String ngram1, String ngram2){
        return ComboUtils.combo(prefix, ngram1, ngram2);
    }
}