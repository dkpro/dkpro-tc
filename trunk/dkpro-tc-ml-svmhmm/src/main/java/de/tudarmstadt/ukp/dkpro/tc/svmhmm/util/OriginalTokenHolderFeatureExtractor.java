/*
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
 */

package de.tudarmstadt.ukp.dkpro.tc.svmhmm.util;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.uima.jcas.JCas;

import java.util.Arrays;
import java.util.List;

/**
 * Stores the original token (text content) of the unit
 *
 * @author Ivan Habernal
 */
public class OriginalTokenHolderFeatureExtractor
        extends FeatureExtractorResource_ImplBase
        implements ClassificationUnitFeatureExtractor
{

    /**
     * Feature public name
     */
    public static final String ORIGINAL_TOKEN = "OriginalToken";

    @Override public List<Feature> extract(JCas jCas, TextClassificationUnit textClassificationUnit)
            throws TextClassificationException
    {
        return Arrays.asList(new Feature(ORIGINAL_TOKEN, textClassificationUnit.getCoveredText()));
    }
}
