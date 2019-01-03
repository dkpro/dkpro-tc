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
package org.dkpro.tc.features.twitter;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * A feature extracting the number of hashtags in a tweet.
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NumberOfHashTags
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{

    /**
     * Pattern compiling a regex for twitter hashtags.
     */
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#[a-zA-Z0-9_]+");

    @Override
    public Set<Feature> extract(JCas jCas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        Matcher hashTagMatcher = HASHTAG_PATTERN
                .matcher(jCas.getDocumentText().substring(aTarget.getBegin(), aTarget.getEnd()));
        int numberOfHashTags = 0;
        while (hashTagMatcher.find()) {
            numberOfHashTags++;
        }
        return new Feature(NumberOfHashTags.class.getSimpleName(), numberOfHashTags,
                FeatureType.NUMERIC).asSet();
    }

}
