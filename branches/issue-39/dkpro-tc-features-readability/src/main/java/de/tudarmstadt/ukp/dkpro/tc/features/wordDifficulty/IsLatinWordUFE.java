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
import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;

//this extractor 

public class IsLatinWordUFE
    extends IsInWordListUFE
    implements ClassificationUnitFeatureExtractor
{
    /**
     * @author beinborn
     * 
     *         This feature extractor tests, if the solution is in a list of words with latin roots.
     *         Unless you explicitly provide a word list file, it queries a list of English words
     *         derived from Latin extracted from Wikipedia.
     */
    public static final String PARAM_LOWERCASE = "lowercase";
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = false, defaultValue = "true")
    protected boolean lowercase;

    public static final String PARAM_SOURCE_LOCATION = "latinWordListLocation";
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = false, defaultValue = "")
    protected String wordListLocation;

    public static final String PARAM_LANGUAGE = "language";
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false, defaultValue = "en")
    protected String language;

    public static final String NAME = "IsWordWithLatinRoot";

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {

        return super.initialize(aSpecifier, aAdditionalParams, getLatinWordList(), NAME, lowercase);

    }

    private File getLatinWordList()
        throws ResourceInitializationException

    {

        if (wordListLocation.length() > 0) {
            return new File(wordListLocation);
        }
        else {
            if (language.equals("en")) {
                return new File("src/main/resources/wordLists/EnglishWordsWithLatinOrigin.txt");
            }

            else {
                throw new ResourceInitializationException(
                        "Please provide the parameter PARAM_SOURCE_LOCATION to indicate the location of your word list file. ",
                        null);
            }
        }
    }
}