/*******************************************************************************
 * Copyright 2015
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.SimpleDictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundedWord;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.JWordSplitterAlgorithm;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.SplitterAlgorithm;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class IsNounCompoundExtractor
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    /**
     * @author beinborn
     * 
     *         This feature extractor tests, if the classification unit is a noun compound using a
     *         splitter algorithm from dkpro.core.decompounding.
     */

    public static final String DICTIONARY_LOCATON = "locationOfDictionary";
    @ConfigurationParameter(name = DICTIONARY_LOCATON, description = "File location of a dictionary for the splitter", mandatory = true)
    private String dictionaryLocation;

    public static final String IS_COMPOUND = "IsCompound";

    private SplitterAlgorithm splitter;
    boolean isInitialized;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {

        super.initialize(aSpecifier, aAdditionalParams);
        initializeSplitterResource(dictionaryLocation);
        return true;

    }

    @Override
    public List<Feature> extract(JCas view, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {

        boolean isCompound = false;
        List<Feature> featList = new ArrayList<Feature>();

        POS pos = JCasUtil.selectCovered(Token.class, classificationUnit).get(0).getPos();

        String word = JCasUtil.selectCovered(Lemma.class, classificationUnit).get(0).getValue()
                .toLowerCase();

        // only check for noun compounds
        if (pos instanceof N) {
            try {
                isCompound = isCompound(word);
            }
            catch (ResourceInitializationException e) {
                throw new TextClassificationException(e);
            }

        }
        featList.add(new Feature(IS_COMPOUND, isCompound));
        return featList;
    }

    public boolean isCompound(String word)
        throws ResourceInitializationException
    {

        return getSplits(word).size() > 1;
    }

    public List<DecompoundedWord> getSplits(String word)
        throws ResourceInitializationException
    {
        return splitter.split(word).getAllSplits();
    }

    public void initializeSplitterResource(String dictionaryLocation)

    {
        splitter = new JWordSplitterAlgorithm();
        splitter.setDictionary(new SimpleDictionary(new File(dictionaryLocation)));
        isInitialized = true;
    }

}
