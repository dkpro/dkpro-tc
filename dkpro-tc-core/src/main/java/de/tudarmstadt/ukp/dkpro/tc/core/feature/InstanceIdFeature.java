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
package de.tudarmstadt.ukp.dkpro.tc.core.feature;

import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.ID_FEATURE_NAME;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class InstanceIdFeature {

	
	public static Feature retrieve(JCas jcas) throws ResourceInitializationException {
		
		String fullId = getFullId(jcas);
		return new Feature(ID_FEATURE_NAME, fullId);
	};
	
	public static Feature retrieve(JCas jcas, TextClassificationUnit unit) throws ResourceInitializationException{
		
		String fullId = getFullId(jcas);
		
		String[] parts = fullId.split("_");
		fullId = StringUtils.join(Arrays.copyOfRange(parts, 0, parts.length-1), "_");
		
        fullId = fullId + "_" + unit.getId();
            
        String suffix = unit.getSuffix();
        if (suffix != null && suffix.length() > 0) {
            fullId = fullId + "_" + suffix;
            
        }
		
		
		return new Feature(ID_FEATURE_NAME, fullId);
	};
	
	public static Feature retrieve(JCas jcas, TextClassificationUnit unit, Integer sequenceId) 
		throws ResourceInitializationException 		
	{
		String fullId = getFullId(jcas);

		String[] parts = fullId.split("_");
		fullId = StringUtils.join(Arrays.copyOfRange(parts, 0, parts.length-1), "_");

        fullId = fullId + "_" + sequenceId;
        fullId = fullId + "_" + unit.getId();
            
        String suffix = unit.getSuffix();
        if (suffix != null && suffix.length() > 0) {
            fullId = fullId + "_" + suffix;
        }
	    
		return new Feature(ID_FEATURE_NAME, fullId);
	};
	
	private static String getFullId(JCas jcas) 
			throws ResourceInitializationException
	{		
		String fullId = DocumentMetaData.get(jcas).getDocumentId();	
		
		if (fullId == null) {
			throw new ResourceInitializationException(new Throwable("DocumentId in DocumentMetaData cannot be null."));
		}
		
		return fullId;
	}
}
