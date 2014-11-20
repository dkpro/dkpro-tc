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
package de.tudarmstadt.ukp.dkpro.tc.mallet.report;

/**
 * Constants that are used in reports
 */
public interface MalletReportConstants
{
    // accuracy
    public static final String CORRECT = "Correctly Classified Examples";
    public static final String INCORRECT = "Incorrectly Classified Examples";
    public static final String PCT_CORRECT = "Percentage Correct";
    public static final String PCT_INCORRECT = "Percentage Incorrect";

    // P/R/F/Accuracy
    public static final String PRECISION = "Precision";
    public static final String RECALL = "Recall";
    public static final String FMEASURE = "F-Measure";
    
    public static final String MACRO_AVERAGE_FMEASURE = "Macro-averaged F-Measure";
    
//    public static final String WGT_PRECISION = "Weighted Precision";
//    public static final String WGT_RECALL = "Weighted Recall";
//    public static final String WGT_FMEASURE = "Weighted F-Measure";
    
    public static final String NUMBER_EXAMPLES = "Absolute Number of Examples";
    public static final String NUMBER_LABELS = "Absolute Number of Labels";
}
