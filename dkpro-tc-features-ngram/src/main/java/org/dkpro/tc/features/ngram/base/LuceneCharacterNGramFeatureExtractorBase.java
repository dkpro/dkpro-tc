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
package org.dkpro.tc.features.ngram.base;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;
import org.dkpro.tc.features.ngram.meta.LuceneCharacterNGramMetaCollector;

public class LuceneCharacterNGramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
    implements MetaDependent
{
    public static final String LUCENE_CHAR_NGRAM_FIELD = "charngram";

    public static final String PARAM_CHAR_NGRAM_MIN_N = "charNgramMinN";
    @ConfigurationParameter(name = PARAM_CHAR_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    protected int charNgramMinN;

    public static final String PARAM_CHAR_NGRAM_MAX_N = "charNgramMaxN";
    @ConfigurationParameter(name = PARAM_CHAR_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    protected int charNgramMaxN;

    public static final String PARAM_CHAR_NGRAM_USE_TOP_K = "charNgramUseTopK";
    @ConfigurationParameter(name = PARAM_CHAR_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int charNgramUseTopK;

    public static final String PARAM_CHAR_NGRAM_LOWER_CASE = "charNgramLowerCase";
    @ConfigurationParameter(name = PARAM_CHAR_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "false")
    protected boolean charNgramLowerCase;

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
                throws ResourceInitializationException
    {
        return Arrays.asList(new MetaCollectorConfiguration(LuceneCharacterNGramMetaCollector.class,
                parameterSettings).addStorageMapping(
                        LuceneCharacterNGramMetaCollector.PARAM_TARGET_LOCATION,
                        LuceneCharacterNGram.PARAM_SOURCE_LOCATION,
                        LuceneCharacterNGramMetaCollector.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_CHAR_NGRAM_FIELD + featureExtractorName;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return LUCENE_CHAR_NGRAM_FIELD;
    }

    @Override
    protected int getTopN()
    {
        return charNgramUseTopK;
    }
}