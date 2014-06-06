package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta;

import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.collect.MinMaxPriorityQueue;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LuceneFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.TermFreqTuple;

public abstract class LucenePairFeatureExtractorBase
	extends LuceneFeatureExtractorBase
{
    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int kngramUseTopK;
    /**
     * Use this number of most frequent ngrams from View 1's.
     */
    public static final String PARAM_NGRAM_USE_TOP_K_VIEW1 = "NgramUseTopK1";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_VIEW1, mandatory = true, defaultValue = "500")
    protected int ngramUseTopK1;
    /**
     * Use this number of most frequent ngrams from View 2's.
     */
    public static final String PARAM_NGRAM_USE_TOP_K_VIEW2 = "NgramUseTopK2";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K_VIEW2, mandatory = true, defaultValue = "500")
    protected int ngramUseTopK2;
    /**
     * Whether features should be marked with binary (occurs, doesn't occur in this document pair)
     * values, versus the document count of the feature. In combo ngrams this is (doc1freq *
     * doc2freq). Note this only applies to feature values; frequency selection of features is based
     * on frequency across documents, not within documents.
     */
    public static final String PARAM_NGRAM_BINARY_FEATURE_VALUES_COMBO = "ngramBinaryFeatureValuesCombos";
    @ConfigurationParameter(name = PARAM_NGRAM_BINARY_FEATURE_VALUES_COMBO, mandatory = false, defaultValue = "true")
    protected boolean ngramBinaryFeatureValuesCombos;
    
    protected FrequencyDistribution<String> topKSetView1;
    protected FrequencyDistribution<String> topKSetView2;
    
    //FIXME This is a hack to deal with getTopNgrams() in LuceneFeatureExtractorBase, which can take no args
    protected String fieldOfTheMoment;
    protected int topNOfTheMoment;
	


    protected List<Feature> addToFeatureArray(FrequencyDistribution<String> viewNgrams,
            FrequencyDistribution<String> topKSet, List<Feature> features)
    {
        for (String ngram : topKSet.getKeys()) {
            long value = 1;
            if (!ngramBinaryFeatureValuesCombos) {
                value = viewNgrams.getCount(ngram);
            }
            if (viewNgrams.contains(ngram)) {
                features.add(new Feature(prefix + NGramUtils.NGRAM_GLUE + ngram, value));
            }
            else {
                features.add(new Feature(prefix + NGramUtils.NGRAM_GLUE + ngram, 0));
            }
        }
        return features;
    }

    protected FrequencyDistribution<String> getTopNgrams(int topNgramThreshold, String fieldName)
        throws ResourceInitializationException
    {

        FrequencyDistribution<String> topNGrams = new FrequencyDistribution<String>();

        MinMaxPriorityQueue<TermFreqTuple> topN = MinMaxPriorityQueue
                .maximumSize(topNgramThreshold).create();
        IndexReader reader;
        try {
            reader = DirectoryReader.open(FSDirectory.open(luceneDir));
            Fields fields = MultiFields.getFields(reader);
            if (fields != null) {
                Terms terms = fields.terms(fieldName);
                if (terms != null) {
                    TermsEnum termsEnum = terms.iterator(null);
                    BytesRef text = null;
                    while ((text = termsEnum.next()) != null) {
                        String term = text.utf8ToString();
                        long freq = termsEnum.totalTermFreq();
                        
                        if(conditionsAreMet(term)){
                        	topN.add(new TermFreqTuple(term, freq));
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

        int size = topN.size();
        for (int i = 0; i < size; i++) {
            TermFreqTuple tuple = topN.poll();
            // System.out.println(tuple.getTerm() + " - " + tuple.getFreq());
            topNGrams.addSample(tuple.getTerm(), tuple.getFreq());
        }

        return topNGrams;
    }
    protected boolean conditionsAreMet(String term){
    	return true;
    }
  
    //FIXME This class must be instantiated in NGramFeatureExtractorBase, but can't be anything useful then.
  @Override
  protected String getFeaturePrefix()
  {
      return "ThisIsAnError";
  }

}
