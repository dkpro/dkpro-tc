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
package org.dkpro.tc.features.syntax;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

/**
 * Extracts the ratio of questions (indicated by a single question mark at the end) to total sentences.
 */
public class QuestionsRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    public static final String FN_QUESTION_RATIO = "QuestionRatio";

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {

        int nrOfSentences = JCasUtil.selectCovered(jcas, Sentence.class, target).size();
        String text = jcas.getDocumentText().substring(target.getBegin(), target.getEnd());

        Pattern p = Pattern.compile("\\?[^\\?]"); // don't count multiple question marks as multiple
                                                  // questions

        int matches = 0;
        Matcher m = p.matcher(text);
        while (m.find()) {
            matches++;
        }

        double questionRatio = 0.0;
        if (nrOfSentences > 0) {
            questionRatio = (double) matches / nrOfSentences;
        }

        return new Feature(FN_QUESTION_RATIO, questionRatio).asSet();
    }
}