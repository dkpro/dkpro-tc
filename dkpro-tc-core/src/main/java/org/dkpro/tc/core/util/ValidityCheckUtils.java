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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;

public class ValidityCheckUtils {

	public static int learningModeLabel2int(String learningMode)
			throws AnalysisEngineProcessException
	{
		int learningModeI = 0;

        if (learningMode.equals(Constants.LM_SINGLE_LABEL)) {
            learningModeI = 1;
        }
        else if (learningMode.equals(Constants.LM_MULTI_LABEL)) {
            learningModeI = 2;
        }
        else if (learningMode.equals(Constants.LM_REGRESSION)) {
            learningModeI = 3;
        }
        else {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "Please set a valid learning mode"));
        }
        
        return learningModeI;
	}
	
	public static int featureModeLabel2int(String featureMode)
			throws AnalysisEngineProcessException
	{
		int featureModeI = 0;

        if (featureMode.equals(Constants.FM_DOCUMENT)) {
            featureModeI = 1;
        }
        else if (featureMode.equals(Constants.FM_UNIT)) {
            featureModeI = 2;
        }
        else if (featureMode.equals(Constants.FM_PAIR)) {
            featureModeI = 3;
        }
        else if (featureMode.equals(Constants.FM_SEQUENCE)) {
            featureModeI = 4;
        }
        else {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "Please set a valid feature mode"));
        }
        
        return featureModeI;
	}
}
