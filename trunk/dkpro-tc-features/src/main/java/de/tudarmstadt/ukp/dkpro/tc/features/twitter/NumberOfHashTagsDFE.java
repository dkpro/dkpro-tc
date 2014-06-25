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
package de.tudarmstadt.ukp.dkpro.tc.features.twitter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * A feature extracting the number of hashtags in a tweet.
 * 
 * @author Johannes Daxenberger
 * 
 */
public class NumberOfHashTagsDFE
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{

    /**
     * Pattern compiling a regex for twitter hashtags.
     */
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#[a-zA-Z0-9_]+");

    @Override
    public List<Feature> extract(JCas jCas)
        throws TextClassificationException
    {
        Matcher hashTagMatcher = HASHTAG_PATTERN.matcher(jCas.getDocumentText());
        int numberOfHashTags = 0;
        while (hashTagMatcher.find()) {
            numberOfHashTags++;
        }
        return new Feature(NumberOfHashTagsDFE.class.getSimpleName(), numberOfHashTags).asList();
    }

}
