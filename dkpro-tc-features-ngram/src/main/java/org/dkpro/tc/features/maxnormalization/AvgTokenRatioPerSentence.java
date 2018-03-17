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
package org.dkpro.tc.features.maxnormalization;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.MaximumNormalizationExtractorBase;
import org.dkpro.tc.features.ngram.meta.maxnormalization.MaxNrOfTokensOverAllSentenceMC;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Ratio of the number of characters in a document with respect to the longest document in the
 * training data
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class AvgTokenRatioPerSentence
    extends MaximumNormalizationExtractorBase
{
    public static final String FEATURE_NAME = "TokenRatioPerTarget";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {

        long maxLen = getMax();

        double avg = 0.0;

        Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
        for (Sentence s : sentences) {
            Collection<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, s);
            avg += tokens.size();
        }
        avg /= sentences.size();

        double ratio = getRatio(avg, maxLen);

        return new Feature(FEATURE_NAME, ratio, FeatureType.NUMERIC).asSet();
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(
            Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {

        return Arrays.asList(new MetaCollectorConfiguration(MaxNrOfTokensOverAllSentenceMC.class,
                parameterSettings).addStorageMapping(
                        MaxNrOfTokensOverAllSentenceMC.PARAM_TARGET_LOCATION,
                        AvgTokenRatioPerSentence.PARAM_SOURCE_LOCATION,
                        MaxNrOfTokensOverAllSentenceMC.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return MaxNrOfTokensOverAllSentenceMC.LUCENE_FIELD + featureExtractorName;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return getClass().getSimpleName();
    }

}
