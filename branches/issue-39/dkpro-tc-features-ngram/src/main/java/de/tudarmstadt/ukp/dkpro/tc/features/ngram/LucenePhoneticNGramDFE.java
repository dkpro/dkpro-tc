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
package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePhoneticNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;

/**
 * Extracts token ngrams where tokens are first converted to their phonetic representation (e.g. Soundex).
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LucenePhoneticNGramDFE
    extends LucenePhoneticNGramFeatureExtractorBase
    implements DocumentFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
    	
        List<Feature> features = new ArrayList<Feature>();
        FrequencyDistribution<String> documentNgrams = NGramUtils.getDocumentPhoneticNgrams(jcas, phoneticNgramMinN, phoneticNgramMaxN);

        for (String topNgram : topKSet.getKeys()) {
            if (documentNgrams.getKeys().contains(topNgram)) {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0));
            }
        }
        return features;
    }
}