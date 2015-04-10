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

import static java.util.Arrays.asList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LucenePOSNGramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class LucenePOSNGramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
    implements MetaDependent
{
    public static final String LUCENE_POS_NGRAM_FIELD = "posngram";

    public static final String PARAM_POS_NGRAM_MIN_N = "posNgramMinN";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	protected int posNgramMinN;

    public static final String PARAM_POS_NGRAM_MAX_N = "posNgramMaxN";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
	protected int posNgramMaxN;

    public static final String PARAM_POS_NGRAM_USE_TOP_K = "posNgramUseTopK";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int posNgramUseTopK;

    public static final String PARAM_USE_CANONICAL_POS = "useCanonicalPos";
    @ConfigurationParameter(name = PARAM_USE_CANONICAL_POS, mandatory = true, defaultValue = "true")
    protected boolean useCanonicalTags;

    @Override
    protected String getFieldName()
    {
        return LUCENE_POS_NGRAM_FIELD;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "posngram";
    }

    @Override
    protected int getTopN()
    {
        return posNgramUseTopK;
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses()
        throws ResourceInitializationException
    {
        return asList(new MetaCollectorConfiguration(LucenePOSNGramMetaCollector.class).
                addStorageMapping(
                        LucenePOSNGramMetaCollector.PARAM_TARGET_LOCATION, 
                        PARAM_SOURCE_LOCATION, 
                        LucenePOSNGramMetaCollector.LUCENE_DIR));
    }
}
