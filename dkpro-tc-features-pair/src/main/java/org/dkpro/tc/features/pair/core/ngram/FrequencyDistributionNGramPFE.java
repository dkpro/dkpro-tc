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
package org.dkpro.tc.features.pair.core.ngram;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.FrequencyDistributionNGram;

/**
 * Pair-wise feature extractor Returns the ngrams in a view with the view name as prefix
 */
@Deprecated
public class FrequencyDistributionNGramPFE
    extends FrequencyDistributionNGram
    implements PairFeatureExtractor
{
	
	String viewPrefix;

    @Override
    public Set<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        Set<Feature> features = new HashSet<Feature>();
        
        TextClassificationTarget target1 = JCasUtil.selectSingle(view1, TextClassificationTarget.class);
        TextClassificationTarget target2 = JCasUtil.selectSingle(view2, TextClassificationTarget.class);

        viewPrefix = "ngrams_" + view1.getViewName();
        features.addAll(super.extract(view1,target1));
        viewPrefix = "ngrams_" + view2.getViewName();
        features.addAll(super.extract(view2,target2));
        return features;
    }

//    protected void setStopwords(Set<String> newStopwords)
//    {
//        stopwords = newStopwords;
//    }

//    protected void makeTopKSet(FrequencyDistribution<String> topK)
//    {
//        topKSet = topK;
//    }
    
    @Override
    protected String getFeaturePrefix()
    {
    	return viewPrefix;
    }
}
