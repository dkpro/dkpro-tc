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
package de.tudarmstadt.ukp.dkpro.tc.features.readability;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures.Measures;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Computes the readability measures ari, coleman_liau, flesch, fog, kincaid, lix and smog as
 * implemented in de.tudarmstadt.ukp.dkpro.core.readability-asl
 * 
 * @TODO add parameter to select the readability measures
 * @author beinborn
 * 
 */
public class TraditionalReadabilityMeasuresFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {

        ReadabilityMeasures readability = new ReadabilityMeasures();
        List<Feature> featList = new ArrayList<Feature>();
        if (jcas.getDocumentLanguage() != null) {
            readability.setLanguage(jcas.getDocumentLanguage());
        }

        int nrOfSentences = JCasUtil.select(jcas, Sentence.class).size();
        List<String> words = new ArrayList<String>();
        for (Token t : JCasUtil.select(jcas, Token.class)) {
            words.add(t.getCoveredText());
        }

        for (Measures measure : Measures.values()) {
            featList.add(new Feature(measure.name(), readability.getReadabilityScore(measure,
                    words, nrOfSentences)));
        }

        return featList;
    }
}