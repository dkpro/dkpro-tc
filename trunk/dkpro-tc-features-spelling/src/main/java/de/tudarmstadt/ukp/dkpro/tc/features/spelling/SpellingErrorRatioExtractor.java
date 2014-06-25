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
package de.tudarmstadt.ukp.dkpro.tc.features.spelling;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Extracts the ratio of wrongly spelled tokens to all tokens.
 * 
 * @author zesch
 *
 */
public class SpellingErrorRatioExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{

    // TODO Issue 125: could be generalized to AnnotationRatioFE
    
    @Override
    public List<Feature> extract(JCas view)
        throws TextClassificationException
    {
        int nrOfSpellingErrors = JCasUtil.select(view, SpellingAnomaly.class).size();
        int nrOfTokens = JCasUtil.select(view, Token.class).size();
        
        double ratio = 0.0;
        if (nrOfTokens > 0) {
            ratio = (double) nrOfSpellingErrors / nrOfTokens;
        }
        return Arrays.asList(new Feature("SpellingErrorRatio", ratio));
    }
}