/*
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
 */

package org.dkpro.tc.ml.svmhmm.util;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Stores the original token (text content) of the unit
 */
public class OriginalTextHolderFeatureExtractor
        extends FeatureExtractorResource_ImplBase
        implements FeatureExtractor
{

    /**
     * Feature public name
     */
    public static final String ORIGINAL_TEXT = "OriginalText";

    @Override public Set<Feature> extract(JCas jCas, TextClassificationTarget textClassificationUnit)
            throws TextClassificationException
    {
        return new Feature(ORIGINAL_TEXT, textClassificationUnit.getCoveredText()).asSet();
    }
}
