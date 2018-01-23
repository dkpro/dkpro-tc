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
package org.dkpro.tc.core.util;

/**
 * Constants that are used in reports
 */
public interface ReportConstants
{
	// GENERAL
    static final String MEASURES = "Measures";
	
    // accuracy
    static final String CORRECT = "Correctly Classified Examples";
    static final String INCORRECT = "Incorrectly Classified Examples";
    static final String PCT_CORRECT = "Percentage Correct";
    static final String PCT_INCORRECT = "Percentage Incorrect";
    static final String PCT_UNCLASSIFIED = "Percentage Unclassified";

    // P/R/F
    static final String PRECISION = "Unweighted Precision";
    static final String RECALL = "Unweighted Recall";
    static final String FMEASURE = "Unweighted F-Measure";
    static final String WGT_PRECISION = "Weighted Precision";
    static final String WGT_RECALL = "Weighted Recall";
    static final String WGT_FMEASURE = "Weighted F-Measure";

    // regression
    static final String CORRELATION = "Pearson Correlation";
    static final String MEAN_ABSOLUTE_ERROR = "Mean absolute error";
    static final String RELATIVE_ABSOLUTE_ERROR = "Relative absolute error";
    static final String ROOT_MEAN_SQUARED_ERROR = "Root mean squared error";
    static final String ROOT_RELATIVE_SQUARED_ERROR = "Root relative squared error";
    
    // multi-label classification
    static final String AVERAGE_THRESHOLD = "Averaged Threshold";
    static final String LABEL_CARDINALITY_REAL = "Label Cardinality real";
    static final String LABEL_CARDINALITY_PRED = "Label Cardinality predicted";
    static final String EMPTY_VECTORS = "Empty Vectors";
    static final String HEMMING_ACCURACY = "Hemming Accuracy";
    static final String ZERO_ONE_LOSS = "Zero One Loss";
    static final String EXAMPLE_BASED_LOG_LOSS = "Example Based LogLoss";
    static final String LABEL_BASED_LOG_LOSS = "Label Based LogLoss";
    static final String TP_RATE = "True Positive Rate";
    static final String FP_RATE = "False Positive Rate";
    
    static final String NUMBER_EXAMPLES = "Absolute Number of Examples";
    static final String NUMBER_LABELS = "Absolute Number of Labels";
}
