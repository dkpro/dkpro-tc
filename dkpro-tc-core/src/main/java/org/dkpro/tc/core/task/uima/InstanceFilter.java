/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.core.task.uima;

import java.io.File;
import java.util.Arrays;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.feature.filter.FeatureFilter;

public class InstanceFilter
    implements Constants
{

    private boolean isTesting;
    private String[] featureFilters;

    public InstanceFilter(String[] featureFilters, boolean isTesting)
    {
        this.featureFilters = Arrays.copyOf(featureFilters, featureFilters.length);
        this.isTesting = isTesting;
    }

    public void filter(File jsonTempFile) throws AnalysisEngineProcessException
    {
        for (String filterString : featureFilters) {
            FeatureFilter filter;
            try {
                filter = (FeatureFilter) Class.forName(filterString).newInstance();

                if (filter.isApplicableForTraining() && !isTesting
                        || filter.isApplicableForTesting() && isTesting) {
                    filter.applyFilter(jsonTempFile);
                }
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

}