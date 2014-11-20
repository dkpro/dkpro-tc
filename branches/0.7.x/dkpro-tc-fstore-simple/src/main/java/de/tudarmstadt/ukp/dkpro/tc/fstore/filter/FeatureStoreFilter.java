/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.fstore.filter;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;

/**
 * Filter for feature store
 */
public interface FeatureStoreFilter
{

    /**
     * Applies the filter to the given feature store
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
