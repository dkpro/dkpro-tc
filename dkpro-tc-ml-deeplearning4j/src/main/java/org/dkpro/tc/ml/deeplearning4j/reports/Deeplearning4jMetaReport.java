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

package org.dkpro.tc.ml.deeplearning4j.reports;

import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.reporting.ReportBase;

public class Deeplearning4jMetaReport
    extends ReportBase
{

    @Override
    public void execute() throws Exception
    {
        LogFactory.getLog(getClass()).info(
                "Deeplearning4j implements no Meta report as versioning is defined by the Maven dependency");
    }

}