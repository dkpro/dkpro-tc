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
package org.dkpro.tc.ml.report;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.report.util.MetricComputationUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

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
    
    public String getResults() throws Exception
    {

        List<String[]> computeFScores = MetricComputationUtil.computePerCategoryResults(id2o, learningMode);
        
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%25s\t%5s\t%-6s\t%-6s\t%-6s%n", "#Label", "FREQ", "P", "R", "F1"));
        
        for(String [] s : computeFScores) {
            sb.append(String.format(Locale.getDefault(), "%25s\t%5d\t%.4f\t%.4f\t%.4f%n", 
                    s[0],
                    Long.parseLong(s[1]),
                    nf.parse(catchNan(s[2])).doubleValue(),
                    nf.parse(catchNan(s[3])).doubleValue(),
                    nf.parse(catchNan(s[4])).doubleValue()
                    ));
        }
        
        return sb.toString();
    }

    public void writeResults(File fscoreFile) throws Exception
    {
        String results = getResults();
        FileUtils.writeStringToFile(fscoreFile, results, UTF_8);
    }
    
    private String catchNan(String s)
    {
    	if(s.equals("NaN")) {
    		return "0";
    	}
        return s;
    }

}
