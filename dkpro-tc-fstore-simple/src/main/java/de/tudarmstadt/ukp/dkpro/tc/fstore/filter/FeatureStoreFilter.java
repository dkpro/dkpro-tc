package de.tudarmstadt.ukp.dkpro.tc.fstore.filter;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;

public interface FeatureStoreFilter {

	/**
	 * Applies the filter to the given feature store.
	 */
	public void applyFilter(FeatureStore store);
	
	/**
	 * Whether the filter is applicable on training instances
	 */
	public boolean isApplicableForTraining();
	
	/**
	 * Whether the filter is applicable on testing instances
	 */
	public boolean isApplicableForTesting();
	
}
