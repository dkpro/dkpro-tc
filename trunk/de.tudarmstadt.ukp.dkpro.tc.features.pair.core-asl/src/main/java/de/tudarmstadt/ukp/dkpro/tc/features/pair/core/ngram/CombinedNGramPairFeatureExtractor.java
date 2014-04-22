package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import com.google.common.collect.MinMaxPriorityQueue;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.TermFreqTuple;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.LuceneNGramPairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.ComboUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.CombinedNGramPairMetaCollector;

/**
 * Combination pair ngram feature extractor. Creates features that are combinations of ngrams from
 * both documents in the pair. Returns all combinations of qualified ngrams from each of the two
 * documents. A feature value is 1 if each of the ngrams appeared in their respective text, and 0
 * otherwise. <br />
 * For example, given two documents:<br />
 * Document 1: "Cats eat mice"<br />
 * Document 2: "Birds chase cats"<br />
 * <br />
 * some combination ngrams are:<br />
 * comboNG_cats_birds:1, comboNG_cats_eat_birds:1, comboNG_cats_birds_chase:1, etc. <br />
 * Note: To extract ngrams from a pair of documents that are not combinations of ngrams across the
 * documents, please use {@link LuceneNGramPairFeatureExtractor}.
 * 
 * @author Emily Jamison
 * 
 */
public class CombinedNGramPairFeatureExtractor
    extends LuceneNGramPairFeatureExtractor
    implements PairFeatureExtractor
{
    /**
     * Minimum token length of the combination. If neither ngram is empty, this value must be at
     * least 2.
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
     * If true, both orderings of ngram combinations will be used.<br />
     * Example: If ngram 'cat' comes from Document 1, and ngram 'dog' comes from Document 2, then
     * when this param = true, the features comboNG_cat_dog AND comboNG_dog_cat are produced.
     */
    public static final String PARAM_NGRAM_SYMMETRY_COMBO = "ngramUseSymmetricalCombos";
    @ConfigurationParameter(name = PARAM_NGRAM_SYMMETRY_COMBO, mandatory = false, defaultValue = "false")
    protected boolean ngramUseSymmetricalCombos;

    public static final String LUCENE_NGRAM_FIELDCOMBO = "ngramCombo";

    protected FrequencyDistribution<String> topKSetCombo;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        topKSetCombo = getTopNgramsCombo(ngramUseTopKCombo, LUCENE_NGRAM_FIELDCOMBO);
        return true;
    }
    
    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(CombinedNGramPairMetaCollector.class);

        return metaCollectorClasses;
    }

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        FrequencyDistribution<String> view1Ngrams = NGramUtils.getDocumentNgrams(view1, ngramLowerCase, filterPartialStopwordMatches,
                ngramMinN1, ngramMaxN1, stopwords);
        FrequencyDistribution<String> view2Ngrams = NGramUtils.getDocumentNgrams(view2, ngramLowerCase, filterPartialStopwordMatches,
                ngramMinN2, ngramMaxN2, stopwords);

        FrequencyDistribution<String> documentComboNgrams = ComboUtils
                .getCombinedNgrams(view1Ngrams, view2Ngrams, ngramMinNCombo, ngramMaxNCombo,
                        ngramUseSymmetricalCombos);

        List<Feature> features = new ArrayList<Feature>();
        prefix = "comboNG";
        features = addToFeatureArray(documentComboNgrams, topKSetCombo, features);

        return features;
    }

    private FrequencyDistribution<String> getTopNgramsCombo(int topNgramThreshold, String fieldName)
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
                            //add conditions here, like ngram1 is in most freq ngrams1...
                            String combo1 = term.split(ComboUtils.JOINT)[0];
                            String combo2 = term.split(ComboUtils.JOINT)[1];
                            int combinedSize = combo1.split("_").length
                                  + combo2.split("_").length;
                            if(topKSetView1.contains(combo1) 
                            		&& topKSet.contains(combo1) 
                            		&& topKSetView2.contains(combo2) 
                            		&& topKSet.contains(combo2)
                            		&& combinedSize <= ngramMaxNCombo
                                    && combinedSize >= ngramMinNCombo){
                            	//print out here for testing
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
}