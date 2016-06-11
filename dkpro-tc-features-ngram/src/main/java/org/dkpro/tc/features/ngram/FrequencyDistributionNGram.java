/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.features.ngram;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import org.dkpro.tc.features.ngram.util.NGramUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
@Deprecated
public class FrequencyDistributionNGram
    extends FrequencyDistributionNGramFeatureExtractorBase
    implements FeatureExtractor
{

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
        Set<Feature> features = new HashSet<Feature>();
        FrequencyDistribution<String> documentNgrams = null;
        documentNgrams = NGramUtils.getDocumentNgrams(jcas, target, ngramLowerCase,
                filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords);
        
        if (tfIdfCalculation == true) {
        	double countCurrentDocumentNgrams = 0;
            for (String ngram : documentNgrams.getKeys()) {
            	countCurrentDocumentNgrams += documentNgrams.getCount(ngram);
            }
        	
        	for (String topNgram : topKSet.getKeys()) {
            	double tf = 0;
            	double idf = 0;
            	double tfIdf = 0;          	
            	
            	if (documentNgrams.getKeys().contains(topNgram)) {
            		// calculate the TF value: the occurrences number of the current n-gram in the document
                	// divided by the total number of n-gram occurrences in the document 
            		tf = documentNgrams.getCount(topNgram) / countCurrentDocumentNgrams;
            		
                	// calculate the IDF value: natural logarithm of dividing the total number of documents 
            		// by the number of documents containing the current top n-gram
                	idf = Math.log((double) dfStore.getDocumentCount() / dfStore.getDf(topNgram));
                			
        			// calculate the TF-IDF value
                	tfIdf = tf * idf;	
                }            	
            	features.add(new Feature(getFeaturePrefix() + "_" + topNgram, tfIdf));
            }            
    	} else if (tfIdfCalculation == false){
    		for (String topNgram : topKSet.getKeys()) {
                if (documentNgrams.getKeys().contains(topNgram)) {
                    features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1));
                }
                else {
                    features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0, true));
                }
            }
    	}        
        return features;
    }
}
