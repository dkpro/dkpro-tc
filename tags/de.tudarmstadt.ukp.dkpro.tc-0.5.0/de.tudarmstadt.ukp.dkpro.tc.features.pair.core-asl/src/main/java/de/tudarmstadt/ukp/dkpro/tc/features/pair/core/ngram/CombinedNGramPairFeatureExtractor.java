package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.PriorityQueue;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.TermFreqQueue;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.TermFreqTuple;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * Combination pair ngram feature extractor.
 * Creates features that are combinations of ngrams from both documents in the pair.
 * Returns all combinations of qualified ngrams from each of the two documents.
 * A feature value is 1 if each of the ngrams appeared in their 
 * respective text, and 0 otherwise.
 * <br />
 * For example, given two documents:<br />
 * Document 1: "Cats eat mice"<br />
 * Document 2: "Birds chase cats"<br />
 * <br />
 * some combination ngrams are:<br />
 * comboNG_cats_birds:1, comboNG_cats_eat_birds:1, comboNG_cats_birds_chase:1, etc.
 * <br />
 * Note: To extract ngrams from a pair of documents that are not combinations of
 * ngrams across the documents, please use {@link LuceneNGramPairFeatureExtractor}.
 * 
 * @author Emily Jamison
 *
 */
public class CombinedNGramPairFeatureExtractor
    extends LuceneNGramPairFeatureExtractor
{    
	/**
	 * Minimum token length of the combination.  If neither ngram is empty, 
	 * this value must be at least 2.
	 */
    public static final String PARAM_NGRAM_MIN_N_COMBO = "ngramMinNCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N_COMBO, mandatory = false, defaultValue = "2")
    protected int ngramMinNCombo;
	/**
	 * Maximum token length of the combination
	 */
    public static final String PARAM_NGRAM_MAX_N_COMBO = "ngramMaxNCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N_COMBO, mandatory = false, defaultValue = "4")
    protected int ngramMaxNCombo;
	/**
	 * Use this number of most frequent combinations
	 */
    public static final String PARAM_NGRAM_USE_TOP_K_COMBO = "ngramUseTopKCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_COMBO, mandatory = false, defaultValue = "500")
    protected int ngramUseTopKCombo;    
    /**
     * All combinations with a frequency above this value will be used.
     */
    public static final String PARAM_NGRAM_FREQ_THRESHOLD_COMBO = "ngramFreqThresholdCombo";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD_COMBO, mandatory = false, defaultValue = "0.01")
    protected float ngramFreqThresholdCombo;
    /**
     * If true, both orderings of ngram combinations will be used.<br />
     * Example: If ngram 'cat' comes from Document 1, and ngram 'dog' comes from Document 2,
     * then when this param = true, the features comboNG_cat_dog AND comboNG_dog_cat are produced.
     */
    public static final String PARAM_NGRAM_SYMMETRY_COMBO = "ngramUseSymmetircalCombos";
    @ConfigurationParameter(name = PARAM_NGRAM_SYMMETRY_COMBO, mandatory = false, defaultValue = "false")
    protected boolean ngramUseSymmetricalCombos;
    
    protected FrequencyDistribution<String> topKSetCombo;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        topKSetCombo = getTopNgramsCombo();
        
        prefix = "comboNG";

        return true;
    }
    
    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {     
        FrequencyDistribution<String> view1Ngrams = getViewNgrams(
        		AbstractPairReader.PART_ONE, jcas, classificationUnit);
        FrequencyDistribution<String> view2Ngrams = getViewNgrams(
        		AbstractPairReader.PART_TWO, jcas, classificationUnit);
        
        FrequencyDistribution<String> documentComboNgrams = NGramUtils.getCombinedNgrams(view1Ngrams,
                view2Ngrams, ngramMinNCombo, ngramMaxNCombo, ngramUseSymmetricalCombos);
         
        List<Feature> features = new ArrayList<Feature>();
        features = addToFeatureArray(documentComboNgrams, topKSetCombo, features);
        
        return features;
    }

    
    private FrequencyDistribution<String> getTopNgramsCombo()
        throws ResourceInitializationException
    {
    	FrequencyDistribution<String> topNGramsCombo = new FrequencyDistribution<String>();
      
    	PriorityQueue<TermFreqTuple> topN = new TermFreqQueue(ngramUseTopKCombo);
    	try{
	          IndexReader reader = DirectoryReader.open(FSDirectory.open(luceneDir));
	          
	          IndexSearcher is = new IndexSearcher(reader);
	          Query query = new MatchAllDocsQuery();
	          TopDocs topDocs = is.search(query, reader.maxDoc());
	          ScoreDoc[] hits = topDocs.scoreDocs;
	          
	          for (ScoreDoc hit : hits) {
	                int docId = hit.doc;
	                Document d = is.doc(docId);
	                String[] ngramArray1 = d.getValues(LUCENE_NGRAM_FIELD1);
	                String[] ngramArray2 = d.getValues(LUCENE_NGRAM_FIELD2);
	                for(String ngram1: ngramArray1){
	                    if (topKSetView1.contains(ngram1) && topKSetAll.contains(ngram1)){
	                        for(String ngram2: ngramArray2){
	                            if (topKSetView2.contains(ngram2) && topKSetAll.contains(ngram2)){
	                                int combinedSize = ngram1.split("_").length + ngram2.split("_").length;
	                                if(combinedSize <= ngramMaxNCombo && combinedSize >= ngramMaxNCombo){
	                                    topN.insertWithOverflow(new TermFreqTuple(combo(ngram1, ngram2), 1));
	                                    if(ngramUseSymmetricalCombos){
		                                    topN.insertWithOverflow(new TermFreqTuple(combo(ngram2, ngram1), 1));
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	          }
    	}
    	catch(IOException e){
    		throw new ResourceInitializationException();
    	}

    	for (int i=0; i < topN.size(); i++) {
    		TermFreqTuple tuple = topN.pop();
//          System.out.println(tuple.getTerm() + " - " + tuple.getFreq());
    		topNGramsCombo.addSample(tuple.getTerm(), tuple.getFreq());
    	}
      
    	return topNGramsCombo;
    }
    
    private String combo(String ngram1, String ngram2){
        return ComboUtils.combo(prefix, ngram1, ngram2);
    }
}