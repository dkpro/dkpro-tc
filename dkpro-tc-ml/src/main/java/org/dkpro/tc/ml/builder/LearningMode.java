/*******************************************************************************
 * Copyright 2019
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

public enum LearningMode
    implements
    Constants
{
    /**
     * The objective during prediction is determining exactly one label from a pre-defined set of
     * labels, for instance sentiment classification with the possible labels: neutral, positive and
     * negative, i.e. the categories are mutually exclusive and only one is correct. In case more
     * than two labels are possible in principle, see {@link LearningMode#MULTI_LABEL}
     */
    SINGLE_LABEL(LM_SINGLE_LABEL),

    /**
     * The objective during prediction is determining a numeric value from a scale, for instance
     * automatic scoring of essays on a scale that reflects the points from zero points to one
     * hundred (or any other numeric range).
     */
    REGRESSION(LM_REGRESSION), 
    
    /**
     * The objective during prediction is determining one or more labels from a pre-defined set of
     * labels, for instance categorization of movies in which a movie can be assigned to multiple
     * categories such as drama, thriller or mystery.
     */
    MULTI_LABEL(LM_MULTI_LABEL);

    private String name;

    private LearningMode(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
