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
