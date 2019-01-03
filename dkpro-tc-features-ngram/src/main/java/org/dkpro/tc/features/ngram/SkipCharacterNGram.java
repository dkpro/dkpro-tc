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

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.meta.SkipCharacterNGramMC;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Extracts characters skip-ngrams.
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class SkipCharacterNGram
    extends AbstractNgram
{
    public static final String PARAM_CHAR_SKIP_SIZE = "charSkipSize";
    @ConfigurationParameter(name = PARAM_CHAR_SKIP_SIZE, mandatory = true)
    protected int charSkipSize;
    
    public static final String FEATURE_PREFIX = "cSkNg";
    
    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        if (prepFeatSet == null) {
            prepare();
        }

        FrequencyDistribution<String> skipCharNgrams = SkipCharacterNGramMC.getCharacterSkipNgrams(jcas,
                aTarget, ngramLowerCase, ngramMinN, ngramMaxN, charSkipSize);

         
        return getFeatureSet(skipCharNgrams);
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays.asList(
                new MetaCollectorConfiguration(SkipCharacterNGramMC.class, parameterSettings)
                        .addStorageMapping(SkipCharacterNGramMC.PARAM_TARGET_LOCATION,
                                SkipCharacterNGram.PARAM_SOURCE_LOCATION,
                                SkipCharacterNGramMC.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return SkipCharacterNGramMC.LUCENE_FIELD + featureExtractorName;
    }
    
    @Override
    protected String getFeaturePrefix()
    {
        return FEATURE_PREFIX;
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }
    
    @Override
    protected String ngramType()
    {
        return "CHARACTER-SKIP-";
    }
}