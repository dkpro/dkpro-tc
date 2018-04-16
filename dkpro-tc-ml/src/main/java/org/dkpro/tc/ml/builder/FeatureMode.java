/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.ml.builder;

import org.dkpro.tc.core.Constants;

public enum FeatureMode
    implements
    Constants
{
    /**
     * This mode is for cases in which the classification result is based on the full text of a
     * document such as e-mails, postings etc. Spam classification or sentiment analysis are
     * examples for document classification.
     */
    DOCUMENT(FM_DOCUMENT),

    /**
     * This mode is for cases in which the classification result is based on or more relevant
     * targets in the document. If for instance a classification on all words with a capital letter
     * shall be performed this mode is suited.
     */
    UNIT(FM_UNIT),

    /**
     * This mode is for cases in which one wants to utilize knowledge from a comparison of two
     * documents.
     */
    PAIR(FM_PAIR),

    /**
     * This mode is for cases in which a sequence of labels has to be determined where the
     * previously predicted sequence of labels is informative for the next decision. Typical example
     * is named entity recognition or part-of-speech tagging.
     */
    SEQUENCE(FM_SEQUENCE);

    private String name;

    private FeatureMode(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
