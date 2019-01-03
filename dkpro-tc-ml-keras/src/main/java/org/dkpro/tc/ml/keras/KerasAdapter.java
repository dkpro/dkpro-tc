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
package org.dkpro.tc.ml.keras;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.ml.keras.reports.KerasMetaReport;
import org.dkpro.tc.ml.report.deeplearning.DeepLearningId2OutcomeReport;
import org.dkpro.tc.ml.report.deeplearning.DeepLearningMajorityClass2OutcomeReport;
import org.dkpro.tc.ml.report.deeplearning.DeepLearningRandomBaseline2OutcomeReport;

public class KerasAdapter
    implements TcDeepLearningAdapter
{

    @Override
    public ExecutableTaskBase getTestTask()
    {
        return new KerasTestTask();
    }

    @Override
    public ReportBase getOutcomeIdReportClass()
    {
        return new DeepLearningId2OutcomeReport();
    }

    @Override
    public ReportBase getMajorityBaselineIdReportClass()
    {
        return new DeepLearningMajorityClass2OutcomeReport();
    }

    @Override
    public ReportBase getRandomBaselineIdReportClass()
    {
        return new DeepLearningRandomBaseline2OutcomeReport();
    }

    @Override
    public int lowestIndex()
    {
        // The value 0 zero might be used in zero-padding i.e. padding would not
        // be distinguishable from actual value zero
        return 1;
    }

    @Override
    public ReportBase getMetaCollectionReport()
    {
        return new KerasMetaReport();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }
}
