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
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.features.ngram.LuceneSkipNGram;
import org.dkpro.tc.features.ngram.meta.LuceneSkipNgramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LuceneSkipNgramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
{
    public static final String LUCENE_SKIP_NGRAM_FIELD = "skipngram";

    public static final String PARAM_SKIP_NGRAM_MIN_N = "skipNgramMinN";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_MIN_N, mandatory = true, defaultValue = "2")
    protected int skipMinN;

    public static final String PARAM_SKIP_NGRAM_MAX_N = "skipNgramMaxN";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    protected int skipMaxN;

    public static final String PARAM_SKIP_SIZE = "skipSize";
    @ConfigurationParameter(name = PARAM_SKIP_SIZE, mandatory = true, defaultValue = "2")
    protected int skipSize;

    public static final String PARAM_SKIP_NGRAM_USE_TOP_K = "skipNgramUseTopK";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int skipNgramUseTopK;

    public static final String PARAM_SKIP_NGRAM_LOWER_CASE = "skipNgramLowercase";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    protected boolean skipToLowerCase;

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
                throws ResourceInitializationException
    {
        return Arrays.asList(new MetaCollectorConfiguration(LuceneSkipNgramMetaCollector.class,
                parameterSettings).addStorageMapping(
                        LuceneSkipNgramMetaCollector.PARAM_TARGET_LOCATION,
                        LuceneSkipNGram.PARAM_SOURCE_LOCATION,
                        LuceneSkipNgramMetaCollector.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_SKIP_NGRAM_FIELD + featureExtractorName;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "skipngram";
    }

    @Override
    protected int getTopN()
    {
        return skipNgramUseTopK;
    }
}