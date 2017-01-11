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

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class WordShapeExtractor extends WindowFeatureExtractor<Token>{

	@Override
	protected Class<Token> getTargetType() {
		return Token.class;
	}

	@Override
	protected String getFeatureName() {		
		return "WordShape";
	}

	@Override
	protected String getFeatureValue(Token a) {
		String text = a.getCoveredText();		
		 
		CharsetEncoder isoEncoder = Charset.forName("ISO-8859-1").newEncoder(); 

		text = text.replace("“", "\"").replace("„", "\"").replace("–", "-").replace("−", "-").replace("…", "...").replace("—", "-").replace("’", "'").replace("’", "'");

		String shape = WordShapeClassifier.wordShape(text, WordShapeClassifier.WORDSHAPECHRIS2);
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = shape.length(); i < n; i++) {
		    char c = shape.charAt(i);
		    
		    if(!isoEncoder.canEncode(c)) {
				sb.append("-NON-ISO-");
			} else {
				sb.append(c);
			}
		}
		

		return sb.toString();
	}

}
