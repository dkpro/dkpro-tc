/*******************************************************************************
 * Copyright 2018
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.LuceneFeatureExtractorBase;
import org.dkpro.tc.features.ngram.meta.CharacterNGramMC;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * Extracts character n-grams.
 */
@TypeCapability(inputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class CharacterNGram
    extends LuceneFeatureExtractorBase
    implements FeatureExtractor
{

    @Override
    public Set<Feature> extract(JCas jCas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        Set<Feature> features = new HashSet<Feature>();
        FrequencyDistribution<String> documentCharNgrams = CharacterNGramMC.getAnnotationCharacterNgrams(
        		aTarget, ngramLowerCase, ngramMinN, ngramMaxN, '^', '$');

        for (String topNgram : topKSet.getKeys()) {
            if (documentCharNgrams.getKeys().contains(topNgram)) {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1, FeatureType.BOOLEAN));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0, true, FeatureType.BOOLEAN));
            }
        }
        return features;
    }

    @Override
    protected String getFieldName()
    {
        return CharacterNGramMC.LUCENE_CHAR_NGRAM_FIELD + featureExtractorName;
    }


	@Override
	protected String getFeaturePrefix() {
		return getClass().getSimpleName();
	}

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
                throws ResourceInitializationException
    {
        return Arrays.asList(new MetaCollectorConfiguration(CharacterNGramMC.class,
                parameterSettings).addStorageMapping(
                        CharacterNGramMC.PARAM_TARGET_LOCATION,
                        CharacterNGram.PARAM_SOURCE_LOCATION,
                        CharacterNGramMC.LUCENE_DIR));
    }

    @Override
    protected void logSelectionProcess(long N)
    {
        getLogger().log(Level.INFO, "+++ SELECTING THE " + N + " MOST FREQUENT CHARACTER ["
                + range() + "]-GRAMS ("+caseSensitivity()+")");
    }
    
    private String range()
    {
        return ngramMinN == ngramMaxN ? ngramMinN + "" : ngramMinN + "-" + ngramMaxN;
    }

    private String caseSensitivity()
    {
        return ngramLowerCase ? "case-insensitive" : "case-sensitive";
    }
}