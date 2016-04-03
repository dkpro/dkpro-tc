package org.dkpro.tc.features.window;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class CoveredLowerTextExtractor extends WindowFeatureExtractor<Token>{

	@Override
	protected Class<Token> getTargetType() {
		return Token.class;
	}

	@Override
	protected String getFeatureName() {		
		return "Token";
	}

	@Override
	protected String getFeatureValue(Token a) {
				
		return a.getCoveredText().trim().toLowerCase();
	}


}
