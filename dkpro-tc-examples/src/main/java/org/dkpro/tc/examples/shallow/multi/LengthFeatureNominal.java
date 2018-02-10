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
package org.dkpro.tc.examples.shallow.multi;

import java.util.Collection;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Extracts the number of sentences in this classification unit
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class LengthFeatureNominal
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    public static final String FEATURE_NAME = "NominalLengthFeature";
    
    

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget classificationUnit)
        throws TextClassificationException
    {
    	
    	Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
    	if(tokens.size() > 150){
    		return new Feature(FEATURE_NAME, LengthEnum.LONG, FeatureType.NOMINAL).asSet();
    	}else if(tokens.size() > 100){
    		return new Feature(FEATURE_NAME, LengthEnum.MIDDLE, FeatureType.NOMINAL).asSet();
    	}else{
    		return new Feature(FEATURE_NAME, LengthEnum.SHORT, FeatureType.NOMINAL).asSet();
    	}
    }
}
