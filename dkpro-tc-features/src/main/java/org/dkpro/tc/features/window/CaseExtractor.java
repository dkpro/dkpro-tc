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
