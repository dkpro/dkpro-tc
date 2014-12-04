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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.features.readability.util.ReadabilityUtils;

public class AcademicTokenRatioExtractor

    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    /**
     * Calculates the ratio of academic words according to the Coxhead word list (
     * http://simple.wiktionary.org/wiki/Wiktionary:Academic_word_list) as described in: Sowmya
     * Vajjala and Detmar Meurers. 2012. On improving the accuracy of readability classification
     * using insights from second language acquisition. In Proceedings of the Seventh Workshop on
     * Building Educational Applications Using NLP. Association for Computational Linguistics,
     * Stroudsburg, PA, USA, 163-173.
     * 
     * In addition, we also consider the COCA-Academic word list:
     * http://www.academicvocabulary.info/download.asp
     * 
     * @author beinborn
     **/

    List<String> cocaWords;
    List<String> coxheadWords;
    boolean listsInitialized;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {

        super.initialize(aSpecifier, aAdditionalParams);
        return true;
    }

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        if (!listsInitialized) {
            try {
                cocaWords = new ArrayList<String>();
                coxheadWords = new ArrayList<String>();
                cocaWords.addAll(FileUtils.readLines(new File(new DkproContext().getWorkspace()
                        .getAbsolutePath() + "/acadCore-coca.txt"), "utf-8"));

                coxheadWords.addAll(FileUtils.readLines(new File(new DkproContext().getWorkspace()
                        .getAbsolutePath() + "/Coxhead_academicWords_en.txt"), "utf-8"));
            }
            catch (IOException e) {
                throw new TextClassificationException(e);
            }

            listsInitialized = true;
        }
        int sumCocaWords = 0;
        int sumCoxheadWords = 0;
        int nrOfWords = 0;
        for (Token tok : JCasUtil.select(jcas, Token.class)) {

            if (ReadabilityUtils.isLexicalWordEn(tok)) {
                nrOfWords++;

                String lemma = tok.getLemma().getValue().toLowerCase();

                if (cocaWords.contains(lemma)) {
                    sumCocaWords++;
                }
                if (coxheadWords.contains(lemma)) {
                    sumCoxheadWords++;
                }
            }
        }
        List<Feature> featList = new ArrayList<Feature>();
        featList.add(new Feature("RatioOfAcademicWords_Coxhead", sumCoxheadWords
                / (double) nrOfWords));
        featList.add(new Feature("RatioOfAcademicWords_Coca", sumCocaWords / (double) nrOfWords));
        return featList;
    }
}