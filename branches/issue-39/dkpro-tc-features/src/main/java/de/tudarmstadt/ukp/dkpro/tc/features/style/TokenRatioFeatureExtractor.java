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
package de.tudarmstadt.ukp.dkpro.tc.features.style;

import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Extracts the ratio of a certain token to all tokens in the document
 */
public class TokenRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_TOKEN_RATIO = "TokenRatio";

    private String token;

    public TokenRatioFeatureExtractor(String token)
    {
        this.token = token;
    }

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        double tokenRatio = 0.0;
        int tokenCount = 0;
        int n = 0;
        for (Token t : JCasUtil.select(jcas, Token.class)) {
            n++;

            String text = t.getCoveredText().toLowerCase();
            if (text.equals(token)) {
                tokenCount++;
            }
        }
        if (n > 0) {
            tokenRatio = (double) tokenCount / n;
        }

        return new Feature(FN_TOKEN_RATIO + "_" + token, tokenRatio).asList();
    }
}