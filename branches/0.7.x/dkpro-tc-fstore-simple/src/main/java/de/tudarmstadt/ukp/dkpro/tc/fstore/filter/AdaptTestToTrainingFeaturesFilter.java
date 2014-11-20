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

import java.util.TreeSet;

/**
 * Filter for injecting feature space (feature names) that were seen during training to the
 * feature store during testing.
 *
 * @author Torsten Zesch
 * @author Ivan Habernal
 */
public class AdaptTestToTrainingFeaturesFilter
        implements FeatureStoreFilter
{

    private TreeSet<String> trainingFeatureNames;

    /**
     * Sets the feature names known from training
     *
     * @param trainingFeatureNames feature names
     */
    public void setFeatureNames(TreeSet<String> trainingFeatureNames)
    {
        this.trainingFeatureNames = trainingFeatureNames;
    }

    @Override
    public void applyFilter(FeatureStore store)
    {
        if (store.isSettingFeatureNamesAllowed()) {
            store.setFeatureNames(this.trainingFeatureNames);
        }
    }

    @Override
    public boolean isApplicableForTraining()
    {
        return false;
    }

    @Override
    public boolean isApplicableForTesting()
    {
        return true;
    }
}