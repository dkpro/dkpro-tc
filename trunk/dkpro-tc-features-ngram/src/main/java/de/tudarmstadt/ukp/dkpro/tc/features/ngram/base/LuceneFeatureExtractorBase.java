/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.features.ngram.base;

import java.io.File;

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
import org.apache.uima.util.Level;

import com.google.common.collect.MinMaxPriorityQueue;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.TermFreqTuple;

public abstract class LuceneFeatureExtractorBase
    extends NGramFeatureExtractorBase
{
    public static final String PARAM_LUCENE_DIR = "luceneDir";
    @ConfigurationParameter(name = PARAM_LUCENE_DIR, mandatory = true)
    protected File luceneDir;
    
    public static final String LUCENE_NGRAM_FIELD = "ngram";

    @Override
    protected FrequencyDistribution<String> getTopNgrams()
        throws ResourceInitializationException
    {       

    	FrequencyDistribution<String> topNGrams = new FrequencyDistribution<String>();
        
        MinMaxPriorityQueue<TermFreqTuple> topN = MinMaxPriorityQueue.maximumSize(getTopN()).create();

        IndexReader reader;
        try {
            reader = DirectoryReader.open(FSDirectory.open(luceneDir));
            Fields fields = MultiFields.getFields(reader);
            if (fields != null) {
                Terms terms = fields.terms(getFieldName());
                if (terms != null) {
                    TermsEnum termsEnum = terms.iterator(null);
                    BytesRef text = null;
                    while ((text = termsEnum.next()) != null) {
                        String term = text.utf8ToString();
                        long freq = termsEnum.totalTermFreq();
                        if(passesScreening(term)){ 
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
        for (int i=0; i < size; i++) {
            TermFreqTuple tuple = topN.poll();
            topNGrams.addSample(tuple.getTerm(), tuple.getFreq());
        }
        
        getLogger().log(Level.INFO, "+++ TAKING " + topNGrams.getB() + " NGRAMS");

        return topNGrams;
    }
        
    /**
     * @return The field name that this lucene-based ngram FE uses for storing the ngrams
     */
    @Override
    protected abstract String getFieldName();
    
    /**
     * @return How many of the most frequent ngrams should be returned.
     */
    @Override
    protected abstract int getTopN();
    
    /**
     * Permits the Pair FE's to use {@link #getTopNgrams()}: can be optionally overridden,
     * to constrain which ngrams are used as features.  (Pair ngram FE's generate a huge
     * number of features, that must usually be constrained.)  Without this method, getTopNgrams()
     * must be overridden in {@link LucenePairFeatureExtractorBase} with essentially the same method,
     * but with the constraint option in place, resulting in code duplication.
     * 
     * @param term potential new feature
     * @return
     */
    protected boolean passesScreening(String term){
        return true;
    }
}