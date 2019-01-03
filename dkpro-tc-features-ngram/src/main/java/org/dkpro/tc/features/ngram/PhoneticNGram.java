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
package org.dkpro.tc.features.ngram;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.meta.PhoneticNGramMC;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Extracts token ngrams where tokens are first converted to their phonetic representation (e.g.
 * Soundex).
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class PhoneticNGram
    extends AbstractNgram
{

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        
        if (prepFeatSet == null) {
            prepare();
        }

        FrequencyDistribution<String> documentNgrams = PhoneticNGramMC
                .getDocumentPhoneticNgrams(jcas, aTarget, ngramMinN, ngramMaxN);

        return getFeatureSet(documentNgrams);
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays
                .asList(new MetaCollectorConfiguration(PhoneticNGramMC.class, parameterSettings)
                        .addStorageMapping(PhoneticNGramMC.PARAM_TARGET_LOCATION,
                                PhoneticNGram.PARAM_SOURCE_LOCATION, PhoneticNGramMC.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return PhoneticNGramMC.LUCENE_PHONETIC_NGRAM_FIELD + featureExtractorName;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return PhoneticNGramMC.LUCENE_PHONETIC_NGRAM_FIELD;
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }
}