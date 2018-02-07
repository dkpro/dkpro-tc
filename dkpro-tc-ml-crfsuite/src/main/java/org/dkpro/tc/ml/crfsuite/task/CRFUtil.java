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
package org.dkpro.tc.ml.crfsuite.task;

import java.util.ArrayList;
import java.util.List;

import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;

public class CRFUtil
{

    public static String getAlgorithm(List<Object> objArgs)
    {
    	
    	List<String> classificationArguments =  objArgs2StringArgs(objArgs);

        if (classificationArguments != null) {
            for (int i = 0; i < classificationArguments.size(); i++) {
                String e = classificationArguments.get(i);

                if (e.equals("-a")) {
                    if (i + 1 >= classificationArguments.size()) {
                        throw new IllegalArgumentException(
                                "Found parameter [-a] but no algorithm name was specified");
                    }

                    if (!isValidAlgoName(classificationArguments.get(i + 1))) {
                        throw new IllegalArgumentException("The algorithm name ["
                                + classificationArguments.get(i + 1) + "] is unknown - see ["
                                + CRFSuiteAdapter.class.getName() + "] for available algorithm");
                    }

                    return classificationArguments.get(i + 1);
                }
                if (!e.startsWith("-")) {
                    return classificationArguments.get(i);
                }

            }
        }

        return CRFSuiteAdapter.ALGORITHM_LBFGS;
    }

	public static List<String> objArgs2StringArgs(List<Object> objArgs) {
		List<String> classificationArguments = new ArrayList<>();
		for (int i = 1; i < objArgs.size(); i++) {
			classificationArguments.add((String) objArgs.get(i));
		}

		return classificationArguments;
	}

	private static boolean isValidAlgoName(String string)
    {
        return string.equals(CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR)
                || string.equals(CRFSuiteAdapter.ALGORITHM_AVERAGED_PERCEPTRON)
                || string.equals(CRFSuiteAdapter.ALGORITHM_L2_STOCHASTIC_GRADIENT_DESCENT)
                || string.equals(CRFSuiteAdapter.ALGORITHM_LBFGS);
    }

    public static List<String> getAlgorithmConfigurationParameter(
            List<Object> objArgs)
    {
        if (objArgs == null) {
            return new ArrayList<>();
        }
        
        List<String> classificationArguments = objArgs2StringArgs(objArgs);

        List<String> parameter = new ArrayList<>();

        for (int i = 0; i < classificationArguments.size(); i++) {
            String e = classificationArguments.get(i);

            if (e.equals("-p")) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found parameter [-p] but no corresponding value for it");
                }
                parameter.add(e);
                parameter.add(classificationArguments.get(i + 1));
            }

        }

        return parameter;
    }

}
