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
import org.dkpro.tc.features.ngram.meta.PosNGramMC;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Extracts POS n-grams.
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class PosNGram
    extends AbstractNgram
{

    public static final String PARAM_USE_CANONICAL_POS = "useCanonicalPos";
    @ConfigurationParameter(name = PARAM_USE_CANONICAL_POS, mandatory = true, defaultValue = "true")
    protected boolean useCanonicalTags;

    @Override
    public Set<Feature> extract(JCas view, TextClassificationTarget classificationUnit)
        throws TextClassificationException
    {
        
        if (prepFeatSet == null) {
            prepare();
        }

        FrequencyDistribution<String> documentPOSNgrams = null;
        documentPOSNgrams = PosNGramMC.getDocumentPosNgrams(view, classificationUnit, ngramMinN,
                ngramMaxN, useCanonicalTags);

 
        return getFeatureSet(documentPOSNgrams);
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays.asList(new MetaCollectorConfiguration(PosNGramMC.class, parameterSettings)
                .addStorageMapping(PosNGramMC.PARAM_TARGET_LOCATION, PosNGram.PARAM_SOURCE_LOCATION,
                        PosNGramMC.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return PosNGramMC.LUCENE_POS_NGRAM_FIELD + featureExtractorName;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return getClass().getSimpleName();
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }
}
