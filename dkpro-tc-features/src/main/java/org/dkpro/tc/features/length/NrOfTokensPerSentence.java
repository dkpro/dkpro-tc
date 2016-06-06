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
package org.dkpro.tc.features.length;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.MissingValue;
import org.dkpro.tc.api.features.MissingValue.MissingValueNonNominalType;
import org.dkpro.tc.api.type.TextClassificationUnit;

/**
 * Extracts the number of tokens per sentence in the classification unit
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class NrOfTokensPerSentence
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    /**
     * Public name of the feature "number of tokens per sentence" in this classification unit
     */
    public static final String FN_TOKENS_PER_SENTENCE = "NrofTokensPerSentence";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
        Set<Feature> featSet = new HashSet<Feature>();

        int numSentences = JCasUtil.selectCovered(jcas, Sentence.class, classificationUnit).size();

        if (numSentences == 0) {
            featSet.add(new Feature(FN_TOKENS_PER_SENTENCE, new MissingValue(
                    MissingValueNonNominalType.NUMERIC)));
        }
        else {
        	int numTokens = JCasUtil.selectCovered(jcas, Token.class, classificationUnit).size();
        	double ratio = numTokens / (double) numSentences;

        	featSet.add(new Feature(FN_TOKENS_PER_SENTENCE, ratio));
        }
        return featSet;
    }
}
