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
package org.dkpro.tc.features.tcu;

import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Sets the text of the TextClassificationUnit after next as feature value 
 */
public class NextNextUnit 
	extends TcuLookUpTable
{
    public static final String PARAM_LOWER_CASE = "useLowerCase";
    @ConfigurationParameter(name = PARAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    protected boolean useLowerCase;
    
    public static final String FEATURE_NAME = "nextNextUnit";
    final static String END_OF_SEQUENCE = "EOS";

    public Set<Feature> extract(JCas aView, TextClassificationTarget unit)
        throws TextClassificationException
    {
        super.extract(aView, unit);
        Integer idx = unitBegin2Idx.get(unit.getBegin());
        
        String featureVal = lowerCase(nextNextToken(idx));
        return new Feature(FEATURE_NAME, featureVal).asSet();
    }
    
    private String lowerCase(String token)
    {
        if(useLowerCase){
            return token.toLowerCase();
        }
        return token;
    }

    private String nextNextToken(Integer idx)
    {
        if (idx2SequenceEnd.get(idx) != null){
            return END_OF_SEQUENCE;
        }
        
        if (idx + 2 < units.size()) {
            return units.get(idx + 2).getCoveredText();
        }
        return END_OF_SEQUENCE;
    }

}
