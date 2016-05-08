/*
 * Copyright 2015
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
 */

package org.dkpro.tc.svmhmm.random;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.ml.report.InnerBatchUsingTCEvaluationReport;
import org.dkpro.tc.svmhmm.SVMHMMAdapter;

/**
 * Random classifier for sequence labeling build upon SVMhmm adapter
 */
public class RandomSVMHMMAdapter
        extends SVMHMMAdapter
{
    @Override public ExecutableTaskBase getTestTask()
    {
        return new SVMHMMRandomTestTask();
    }

    @Override public Class<? extends ReportBase> getBatchTrainTestReportClass()
    {
        return InnerBatchUsingTCEvaluationReport.class;
    }
}
