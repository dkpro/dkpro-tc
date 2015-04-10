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

package de.tudarmstadt.ukp.dkpro.tc.features.wordDifficulty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

/**
 * @author beinborn
 * 
 *         We would prefer to have the wordListLocation and the feature name as parameters.
 *         Unfortunately, we cannot instantiate the same feature extractor several times with
 *         different parameters in one experiments. Therefore, we use subclasses of this extractor
 *         and change the initialization-method accordingly.
 */
public abstract class IsInWordListUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    private boolean lowercase;

    private String featureName = "isInList";
    private List<String> wordsInList;

    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams,
            File wordList, String featureName, boolean lowercase)
        throws ResourceInitializationException
    {
        super.initialize(aSpecifier, aAdditionalParams);
        wordsInList = new ArrayList<String>();
        String line = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(wordList));
            while ((line = br.readLine()) != null) {
                wordsInList.add(line.trim());
            }
            br.close();
        }
        catch (IOException e) {

            throw new ResourceInitializationException(e);
        }
        this.featureName = featureName;
        return true;
    }

    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams,
            File wordList, String featureName)
        throws ResourceInitializationException
    {

        return initialize(aSpecifier, aAdditionalParams, wordList, featureName, true);
    }

    @Override
    public List<Feature> extract(JCas view, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {

        List<Feature> featList = new ArrayList<Feature>();

        Token tok = JCasUtil.selectCovered(Token.class, classificationUnit).get(0);
        String lemma = tok.getLemma().getValue();
        if (lowercase) {
            lemma = lemma.toLowerCase();
        }
        featList.add(new Feature(featureName, wordsInList.contains(lemma)));
        return featList;
    }

    public List<String> getWordsInList()
    {
        return wordsInList;
    }

}