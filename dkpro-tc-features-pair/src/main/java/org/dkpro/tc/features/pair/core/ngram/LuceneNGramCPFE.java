/*******************************************************************************
 * Copyright 2019
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.features.pair.core.ngram;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.util.NGramUtils;
import org.dkpro.tc.features.ngram.util.TermFreqTuple;
import org.dkpro.tc.features.pair.core.ngram.meta.ComboUtils;
import org.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramCPMetaCollector;
import org.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramPMetaCollector;

import com.google.common.collect.MinMaxPriorityQueue;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Combination pair ngram feature extractor. Creates features that are combinations of ngrams from
 * both documents in the pair. Returns all combinations of qualified ngrams from each of the two
 * documents. A feature value is 1 if each of the ngrams appeared in their respective text, and 0
 * otherwise. <br>
 * For example, given two documents:<br>
 * Document 1: "Cats eat mice"<br>
 * Document 2: "Birds chase cats"<br>
 * <br>
 * some combination ngrams are:<br>
 * comboNG_cats_birds:1, comboNG_cats_eat_birds:1, comboNG_cats_birds_chase:1, etc. <br>
 * Note: To extract ngrams from a pair of documents that are not combinations of ngrams across the
 * documents, please use {@link LuceneNGramPFE}.
 */
public class LuceneNGramCPFE
    extends LuceneNGramPFE
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
     * If true, both orderings of ngram combinations will be used.<br>
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
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays.asList(
                new MetaCollectorConfiguration(LuceneNGramPMetaCollector.class, parameterSettings)
                        .addStorageMapping(LuceneNGramCPMetaCollector.PARAM_TARGET_LOCATION,
                                LuceneNGramCPFE.PARAM_SOURCE_LOCATION,
                                LuceneNGramCPMetaCollector.LUCENE_DIR));
    }

    @Override
    public Set<Feature> extract(JCas view1, JCas view2) throws TextClassificationException
    {
        FrequencyDistribution<String> view1Ngrams = null;
        FrequencyDistribution<String> view2Ngrams = null;

        TextClassificationTarget aTarget1 = JCasUtil.selectSingle(view1,
                TextClassificationTarget.class);
        TextClassificationTarget aTarget2 = JCasUtil.selectSingle(view2,
                TextClassificationTarget.class);

        view1Ngrams = NGramUtils.getDocumentNgrams(view1, aTarget1, ngramLowerCase,
                filterPartialStopwordMatches, ngramMinN1, ngramMaxN1, stopwords, Token.class);
        view2Ngrams = NGramUtils.getDocumentNgrams(view2, aTarget2, ngramLowerCase,
                filterPartialStopwordMatches, ngramMinN2, ngramMaxN2, stopwords, Token.class);

        FrequencyDistribution<String> documentComboNgrams = ComboUtils.getCombinedNgrams(
                view1Ngrams, view2Ngrams, ngramMinNCombo, ngramMaxNCombo,
                ngramUseSymmetricalCombos);

        Set<Feature> features = new HashSet<Feature>();
        prefix = "comboNG";
        features = addToFeatureArray(documentComboNgrams, topKSetCombo, features);

        return features;
    }

    private FrequencyDistribution<String> getTopNgramsCombo(int topNgramThreshold, String fieldName)
        throws ResourceInitializationException
    {

        FrequencyDistribution<String> topNGrams = new FrequencyDistribution<String>();

        MinMaxPriorityQueue<TermFreqTuple> topN = MinMaxPriorityQueue.maximumSize(topNgramThreshold)
                .create();
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
                        // add conditions here, like ngram1 is in most freq ngrams1...
                        String combo1 = term.split(ComboUtils.JOINT)[0];
                        String combo2 = term.split(ComboUtils.JOINT)[1];
                        int combinedSize = combo1.split("_").length + combo2.split("_").length;
                        if (topKSetView1.contains(combo1) && topKSet.contains(combo1)
                                && topKSetView2.contains(combo2) && topKSet.contains(combo2)
                                && combinedSize <= ngramMaxNCombo
                                && combinedSize >= ngramMinNCombo) {
                            // print out here for testing
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