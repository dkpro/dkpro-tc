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
package org.dkpro.tc.features.style;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.DocumentFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Given a list of topic terms, extracts the ratio of topic terms to all terms.
 */
public class TopicWordsFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    // takes as parameter list of names of word-list-files in resources, outputs one attribute per
    // list
    public static final String PARAM_TOPIC_FILE_PATH = "topicFilePath";
    @ConfigurationParameter(name = PARAM_TOPIC_FILE_PATH, mandatory = true)
    private String topicFilePath;

    private String prefix;

    @Override
    public Set<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        if (topicFilePath == null || topicFilePath.isEmpty()) {
            System.out.println("Path to word list must be set!");
        }
        List<String> topics = null;
        Set<Feature> features = new HashSet<Feature>();
        List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
        try {
            topics = FileUtils.readLines(new File(topicFilePath));
            for (String t : topics) {
                features.addAll(countWordHits(t, tokens));
            }
        }
        catch (IOException e) {
            throw new TextClassificationException(e);
        }
        return features;
    }

    private List<Feature> countWordHits(String wordListName, List<String> tokens)
        throws TextClassificationException
    {

        // word lists are stored in resources folder relative to feature extractor
        String wordListPath = TopicWordsFeatureExtractor.class.getClassLoader()
                .getResource("./" + wordListName).getPath();
        List<String> topicwords = null;
        try {
            topicwords = FileUtils.readLines(new File(wordListPath), "utf-8");
        }
        catch (IOException e) {
            throw new TextClassificationException(e);
        }
        int wordcount = 0;
        for (String token : tokens) {
            if (topicwords.contains(token)) {
                wordcount++;
            }
        }
        double numTokens = tokens.size();
        // name the feature same as wordlist
        return Arrays.asList(new Feature(prefix + wordListName,
                numTokens > 0 ? (wordcount / numTokens) : 0));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        prefix = "TopicWords_";

        return true;
    }
}
