package org.dkpro.tc.features.window;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class LemmaTextExtractor extends WindowFeatureExtractor<Token>{

	@Override
	protected Class<Token> getTargetType() {
		return Token.class;
	}

	@Override
	protected String getFeatureName() {		
		return "Lemma";
	}

	@Override
	protected String getFeatureValue(Token a) {
		Lemma l = a.getLemma();
		if(l != null)
			return l.getValue().toLowerCase();
		else
			return "null";
	}


}
