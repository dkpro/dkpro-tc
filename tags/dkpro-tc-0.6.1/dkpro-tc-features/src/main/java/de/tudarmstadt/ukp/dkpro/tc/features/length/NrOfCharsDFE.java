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
package de.tudarmstadt.ukp.dkpro.tc.features.length;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue.MissingValueNonNominalType;

/**
 * Extracts the number of characters in the document, per sentence, and per token
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NrOfCharsDFE
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{

    /**
     * Public name of the feature "number of characters"
     */
    public static final String FN_NR_OF_CHARS = "NrofChars";
    /**
     * Public name of the feature "number of characters per sentence"
     */
    public static final String FN_NR_OF_CHARS_PER_SENTENCE = "NrofCharsPerSentence";
    /**
     * Public name of the feature "number of characters per token"
     */
    public static final String FN_NR_OF_CHARS_PER_TOKEN = "NrofCharsPerToken";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        double nrOfChars = jcas.getDocumentText().length();
        double nrOfSentences = JCasUtil.select(jcas, Sentence.class).size();
        double nrOfTokens = JCasUtil.select(jcas, Token.class).size();

        List<Feature> featList = new ArrayList<Feature>();
        featList.add(new Feature(FN_NR_OF_CHARS, nrOfChars));

        if (nrOfSentences == 0) {
            featList.add(new Feature(FN_NR_OF_CHARS_PER_SENTENCE, new MissingValue(
                    MissingValueNonNominalType.NUMERIC)));
        }
        else {
            featList.add(new Feature(FN_NR_OF_CHARS_PER_SENTENCE, nrOfChars / nrOfSentences));
        }

        if (nrOfTokens == 0) {
            featList.add(new Feature(FN_NR_OF_CHARS_PER_TOKEN, new MissingValue(
                    MissingValueNonNominalType.NUMERIC)));
        }
        else {
            featList.add(new Feature(FN_NR_OF_CHARS_PER_TOKEN, nrOfChars / nrOfTokens));
        }
        return featList;
    }
}