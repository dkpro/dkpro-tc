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
package org.dkpro.tc.ml.report;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.report.util.MetricComputationUtil;

public class ResultPerCategoryCalculator
    implements Constants
{
    private File id2o;
    private String learningMode;

    public ResultPerCategoryCalculator(File id2o, String learningMode)
    {
        this.id2o = id2o;
        this.learningMode = learningMode;

        if (!this.learningMode.equals(LM_SINGLE_LABEL)) {
            throw new IllegalArgumentException("Single label mode required");
        }

    }

    public void writeResults(File fscoreFile) throws Exception
    {

        List<String[]> computeFScores = MetricComputationUtil.computePerCategoryResults(id2o, learningMode);

        StringBuilder sb = new StringBuilder();
        sb.append("#Label\tFScore\tPrecision\tRecall\n");
        computeFScores.forEach(
                s -> sb.append(String.format("%10s\t%.4f\t%.4f\t%.4f\n", 
                        s[0],
                        Double.parseDouble(s[1]),
                        Double.parseDouble(s[2]),
                        Double.parseDouble(s[3]))
                        ));

        FileUtils.writeStringToFile(fscoreFile, sb.toString(), "utf-8");
    }

}
