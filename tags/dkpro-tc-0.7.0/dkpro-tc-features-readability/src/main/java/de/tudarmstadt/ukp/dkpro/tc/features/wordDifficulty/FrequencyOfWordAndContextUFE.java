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
 */

package de.tudarmstadt.ukp.dkpro.tc.features.wordDifficulty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFileAccessProvider;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MissingValue;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MissingValue.MissingValueNonNominalType;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class FrequencyOfWordAndContextUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    public static final String PARAM_WEB1T_DIR = "DirectoryOfWeb1T";
    @ConfigurationParameter(name = PARAM_WEB1T_DIR, mandatory = true)
    protected static String web1TDir;

    public static final String PROBABILITY = "UnigramProb";
    public static final String TRIGRAM_PROBABILITY = "TrigramProb";

    public static final String LEFT_BIGRAM_PROB_NORM = "LeftBrigramProbNorm";
    public static final String RIGHT_BIGRAM_PROB_NORM = "RightBrigramProbNorm";
    public static final String BIGRAM_MODEL = "BigramModel";

    public static final String LANGUAGE = "languageCode";
    @ConfigurationParameter(name = LANGUAGE, description = "The language code", mandatory = true, defaultValue = "en")
    private String language;

    private FrequencyCountProvider frequencyProvider;
    private List<Feature> featList = new ArrayList<Feature>();
    private Logger logger;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        super.initialize(aSpecifier, aAdditionalParams);
        logger = getUimaContext().getLogger();

        try {
            frequencyProvider = new Web1TFileAccessProvider(language, new File(web1TDir), 1, 3);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {

        Sentence sent = JCasUtil.selectCovering(Sentence.class, classificationUnit).get(0);
        String word = classificationUnit.getCoveredText();
        String leftWord = "";
        String rightWord = "";

        if (classificationUnit.getBegin() == sent.getBegin()) {
            // Word is beginning of sentence
            leftWord = "<S>";

        }
        else if (classificationUnit.getEnd() == sent.getEnd()) {
            // Word is end of sentence
            rightWord = "</S>";
        }
        else {
            try {
                leftWord = JCasUtil.selectPreceding(jcas, Token.class, classificationUnit, 1)
                        .get(0).getCoveredText();
                rightWord = JCasUtil.selectFollowing(jcas, Token.class, classificationUnit, 1)
                        .get(0).getCoveredText();
            }
            catch (IndexOutOfBoundsException e) {

            }
        }
        String leftBigram = leftWord + " " + word;
        String rightBigram = word + " " + rightWord;
        String trigram = leftWord + " " + word + " " + rightWord;

        // Unigram probabilities
        double uprob;
        try {
            uprob = frequencyProvider.getLogProbability(word);

            addToFeatureList(PROBABILITY, uprob, word);

            double lprob = frequencyProvider.getLogProbability(leftWord);
            double rprob = frequencyProvider.getLogProbability(rightWord);

            // Trigram probability, classification unit is in the middle
            double tprob = frequencyProvider.getLogProbability(trigram);
            addToFeatureList(TRIGRAM_PROBABILITY, tprob, trigram);

            // normalized bigram probabilities
            double leftBigramProb = frequencyProvider.getLogProbability(leftBigram);
            double rightBigramProb = frequencyProvider.getLogProbability(rightBigram);

            double leftBigramProbNorm = 0.0;
            double rightBigramProbNorm = 0.0;

            if (lprob > 0.0 && leftBigramProb > 0.0) {
                leftBigramProbNorm = leftBigramProb / lprob;
            }

            if (rprob > 0.0 && rightBigramProb > 0.0) {
                rightBigramProbNorm = rightBigramProb / rprob;
            }

            double bigramModel = leftBigramProbNorm * uprob * rightBigramProbNorm;
            addToFeatureList(LEFT_BIGRAM_PROB_NORM, leftBigramProbNorm, leftBigram);
            addToFeatureList(RIGHT_BIGRAM_PROB_NORM, rightBigramProbNorm, rightBigram);
            addToFeatureList(BIGRAM_MODEL, bigramModel, trigram);
        }
        catch (IOException e) {
            throw new TextClassificationException(e);
        }
        return featList;
    }

    private void addToFeatureList(String featureName, Double prob, String phrase)
    {
        if (prob.isNaN() || prob.isInfinite()) {
            prob = 0.0;
            logger.log(Level.INFO, "No prob for: " + phrase);
        }
        // the frequency calculation of ngrams with - or ' does not work properly
        // weka replaces the missing value with the mean of the feature for most classifiers

        if (prob == 0.0 && StringUtils.containsAny(phrase, "-'")) {
            featList.addAll(Arrays.asList(new Feature(featureName, new MissingValue(
                    MissingValueNonNominalType.NUMERIC))));
        }
        featList.addAll(Arrays.asList(new Feature(PROBABILITY, prob)));

    }

}