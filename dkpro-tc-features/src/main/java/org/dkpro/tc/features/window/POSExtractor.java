package org.dkpro.tc.features.window;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class POSExtractor extends WindowFeatureExtractor<POS>{

	@Override
	protected Class<POS> getTargetType() {
		return POS.class;
	}

	@Override
	protected String getFeatureName() {		
		return "POS";
	}

	
	@Override
	protected String getFeatureValue(POS a) {
		return a.getPosValue();
	}
}
