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

import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.features.ngram.meta.LuceneNGramMetaCollector;

public abstract class LuceneNgramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
{
    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays.asList(new MetaCollectorConfiguration(LuceneNGramMetaCollector.class, parameterSettings)
                .addStorageMapping(
                        LuceneNGramMetaCollector.PARAM_TARGET_LOCATION,
                             LuceneNGram.PARAM_SOURCE_LOCATION, 
                        LuceneNGramMetaCollector.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_NGRAM_FIELD;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "ngram";
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }
}