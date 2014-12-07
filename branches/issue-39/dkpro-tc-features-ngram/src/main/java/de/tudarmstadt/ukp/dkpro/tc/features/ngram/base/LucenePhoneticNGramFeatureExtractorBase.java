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

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LucenePhoneticNGramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.Token" })
public class LucenePhoneticNGramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
    implements MetaDependent
{
    public static final String LUCENE_PHONETIC_NGRAM_FIELD = "phoneticngram";

    public static final String PARAM_PHONETIC_NGRAM_MIN_N = "phoneticNgramMinN";
    @ConfigurationParameter(name = PARAM_PHONETIC_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	protected int phoneticNgramMinN;

    public static final String PARAM_PHONETIC_NGRAM_MAX_N = "phoneticNgramMaxN";
    @ConfigurationParameter(name = PARAM_PHONETIC_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
	protected int phoneticNgramMaxN;

    public static final String PARAM_PHONETIC_NGRAM_USE_TOP_K = "phoneticNgramUseTopK";
    @ConfigurationParameter(name = PARAM_PHONETIC_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int phoneticNgramUseTopK;

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LucenePhoneticNGramMetaCollector.class);
        
        return metaCollectorClasses;
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_PHONETIC_NGRAM_FIELD;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return LUCENE_PHONETIC_NGRAM_FIELD;
    }

    @Override
    protected int getTopN()
    {
        return phoneticNgramUseTopK;
    }
}