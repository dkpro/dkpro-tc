/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.features.window;

import org.apache.commons.lang.StringUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class CaseExtractor extends WindowFeatureExtractor<Token>{

	@Override
	protected Class<Token> getTargetType() {
		return Token.class;
	}

	@Override
	protected String getFeatureName() {		
		return "Case";
	}

	
	@Override
	protected String getFeatureValue(Token a) {
		String text = a.getCoveredText();
		return CaseExtractor.getCasing(text);			
	}
	
	public static String getCasing(String text) {
		if(StringUtils.isNumeric(text))
			return "numeric";
		if(StringUtils.isAllLowerCase(text))
			return "allLower";
		else if(StringUtils.isAllUpperCase(text))
			return "allUpper";
		else if(Character.isUpperCase(text.charAt(0)))
			return "initialUpper";
		else
			return "other";	
	}
}
