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
package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.FrequencyDistributionNGramDFE;

/**
 * Pair-wise feature extractor Returns the ngrams in a view with the view name as prefix
 * 
 * @author nico.erbs@gmail.com
 * @author daxenberger
 * 
 */
@Deprecated
public class FrequencyDistributionNGramPFE
    extends FrequencyDistributionNGramDFE
    implements PairFeatureExtractor
{
	
	String viewPrefix;

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        List<Feature> features = new ArrayList<Feature>();

        viewPrefix = "ngrams_" + view1.getViewName();
        features.addAll(super.extract(view1));
        viewPrefix = "ngrams_" + view2.getViewName();
        features.addAll(super.extract(view2));
        return features;
    }

    protected void setStopwords(Set<String> newStopwords)
    {
        stopwords = newStopwords;
    }

    protected void makeTopKSet(FrequencyDistribution<String> topK)
    {
        topKSet = topK;
    }
    
    @Override
    protected String getFeaturePrefix()
    {
    	return viewPrefix;
    }
}
