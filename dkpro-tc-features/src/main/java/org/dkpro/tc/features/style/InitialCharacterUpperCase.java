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
package org.dkpro.tc.features.style;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Extracts whether the first character of the classification unit is upper-case or not.
 */
public class InitialCharacterUpperCase
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    /**
     * Public name of the feature "Initial character upper case"
     */
    public static final String FEATURE_NAME = "InitialCharacterUpperCaseUFE";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        String token = aTarget.getCoveredText();

        boolean bool = Character.isUpperCase(token.charAt(0));
        return new Feature(FEATURE_NAME, bool ? 1.0 : 0.0, bool == false, FeatureType.BOOLEAN)
                .asSet();
    }
}