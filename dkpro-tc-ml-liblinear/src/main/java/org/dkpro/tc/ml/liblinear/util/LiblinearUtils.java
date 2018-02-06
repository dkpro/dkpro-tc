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
package org.dkpro.tc.ml.liblinear.util;

import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.EPISILON_DEFAULT;
import static org.dkpro.tc.ml.liblinear.LiblinearTestTask.PARAM_C_DEFAULT;

import java.util.List;

import org.apache.commons.logging.LogFactory;

import de.bwaldvogel.liblinear.SolverType;

public class LiblinearUtils
{
    public static SolverType getSolver(List<Object> classificationArguments)
    {
        if (classificationArguments == null) {
            return SolverType.L2R_LR;
        }

        SolverType type = null;
        for (int i = 1; i < classificationArguments.size(); i++) {
            String e = (String) classificationArguments.get(i);
            if (e.equals("-s")) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found parameter [-s] but no solver type was specified");
                }

                String algo = (String) classificationArguments.get(i + 1);
                switch (algo) {
                case "0":
                    type = SolverType.L2R_LR;
                    break;
                case "1":
                    type = SolverType.L2R_L2LOSS_SVC_DUAL;
                    break;
                case "2":
                    type = SolverType.L2R_L2LOSS_SVC;
                    break;
                case "3":
                    type = SolverType.L2R_L1LOSS_SVC_DUAL;
                    break;
                case "4":
                    type = SolverType.MCSVM_CS;
                    break;
                case "5":
                    type = SolverType.L1R_L2LOSS_SVC;
                    break;
                case "6":
                    type = SolverType.L1R_LR;
                    break;
                case "7":
                    type = SolverType.L2R_LR_DUAL;
                    break;
                case "11":
                    type = SolverType.L2R_L2LOSS_SVR;
                    break;
                case "12":
                    type = SolverType.L2R_L2LOSS_SVR_DUAL;
                    break;
                case "13":
                    type = SolverType.L2R_L1LOSS_SVR_DUAL;
                    break;
                default:
                    throw new IllegalArgumentException("An unknown solver was specified [" + algo
                            + "] which is unknown i.e. check parameter [-s] in your configuration");
                }

            }
        }

        if (type == null) {
            // parameter -s was not specified in the parameters so we set a default value
            type = SolverType.L2R_LR;
        }

        LogFactory.getLog(LiblinearUtils.class).info("Will use solver " + type.toString() + ")");
        return type;
    }

    public static double getParameterC(List<Object> classificationArguments)
    {
        if (classificationArguments == null) {
            return PARAM_C_DEFAULT;
        }

        for (int i = 1; i < classificationArguments.size(); i++) {
            String e = (String) classificationArguments.get(i);
            if (e.equals("-c")) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found parameter [-c] but no value was specified");
                }

                Double value;
                try {
                    value = Double.valueOf((String)classificationArguments.get(i + 1));
                }
                catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "The value of parameter -c has to be a floating point value but was ["
                                    + classificationArguments.get(i + 1) + "]",
                            ex);
                }
                return value;
            }
        }

        LogFactory.getLog(LiblinearUtils.class)
                .info("Parameter c is set to default value [" + PARAM_C_DEFAULT + "]");
        return PARAM_C_DEFAULT;
    }

    public static double getParameterEpsilon(List<Object> classificationArguments)
    {
        if (classificationArguments == null) {
            return EPISILON_DEFAULT;
        }

        for (int i = 1; i < classificationArguments.size(); i++) {
            String e = (String) classificationArguments.get(i);
            if (e.equals("-e")) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found parameter [-e] but no value was specified");
                }

                Double value;
                try {
                    value = Double.valueOf((String)classificationArguments.get(i + 1));
                }
                catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "The value of parameter -e has to be a floating point value but was ["
                                    + classificationArguments.get(i + 1) + "]",
                            ex);
                }
                return value;
            }
        }

        LogFactory.getLog(LiblinearUtils.class).info("Parameter epsilon is set to [0.01]");
        return EPISILON_DEFAULT;
    }

}
