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

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Calculates the proportions of tokens that are longer than max (default=5) characters
 * and tokens shorter than min (default=3) characters to all tokens. This property can 
 * be useful for capturing stylistic differences, e.g. in gender recognition.
 * WARNING: Short token ratio includes also all single-character tokens,
 * such as interpunction. 
 */
public class LongWordsFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String PARAM_MIN_CHARS = "minCharsInWord";
    @ConfigurationParameter(name = PARAM_MIN_CHARS, mandatory = true, defaultValue="3")
    private int min;
    
    public static final String PARAM_MAX_CHARS = "maxCharsInWord";
    @ConfigurationParameter(name = PARAM_MAX_CHARS, mandatory = true, defaultValue="5")
    private int max;
    
    public static final String FN_LW_RATIO = "LongTokenRatio"; // over 5 chars
    public static final String FN_SW_RATIO = "ShortTokenRatio"; // under 3 chars

    @Override
    public List<Feature> extract(JCas jcas)
    {

        double longTokenRatio = 0.0;
        int longTokenCount = 0;
        double shortTokenRatio = 0.0;
        int shortTokenCount = 0;
        int n = 0;
        for (Token t : JCasUtil.select(jcas, Token.class)) {
            n++;

            String text = t.getCoveredText();
            if (text.length() < min) {
                shortTokenCount++;
            }
            else if (text.length() > max) {
                longTokenCount++;
            }
        }
        if (n > 0) {
            longTokenRatio = (double) longTokenCount / n;
            shortTokenRatio = (double) shortTokenCount / n;
        }

        List<Feature> featList = new ArrayList<Feature>();
        featList.add(new Feature(FN_LW_RATIO, longTokenRatio));
        featList.add(new Feature(FN_SW_RATIO, shortTokenRatio));

        return featList;
    }
}