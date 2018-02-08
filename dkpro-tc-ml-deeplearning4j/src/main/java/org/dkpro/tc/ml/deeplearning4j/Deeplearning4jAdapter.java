/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.ml.deeplearning4j;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.ml.deeplearning4j.reports.Deeplearning4jMetaReport;
import org.dkpro.tc.ml.deeplearning4j.reports.Deeplearning4jOutcomeIdReport;

public class Deeplearning4jAdapter
    implements TcDeepLearningAdapter
{

    @Override
    public TaskBase getTestTask()
    {
        return new Deeplearning4jTestTask();
    }

    @Override
    public ReportBase getOutcomeIdReportClass()
    {
        return new Deeplearning4jOutcomeIdReport();
    }

    @Override
    public int lowestIndex()
    {
        return 0;
    }

    @Override
    public ReportBase getMetaCollectionReport()
    {
        return new Deeplearning4jMetaReport();
    }

    @Override
    public String toString(){
    	return getClass().getSimpleName();
    }
}
