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
package de.tudarmstadt.ukp.dkpro.tc.ml.report.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Utilities to pretty print parameter sets.
 * 
 */
public class PrettyPrintUtils
{
    /**
     * Pretty prints classifier name and arguments
     * 
     * @param classArgs
     *            a string with the classifier name as first comma separated token, followed by a
     *            list of arguments in angle brackets as provided by the discriminators.txt
     * @return a short name
     */
    public static String prettyPrintClassifier(String classArgs)
    {
        String[] splittedArgs = classArgs.substring(1, classArgs.length()-1).split(",");
        String classifierName = splittedArgs[0];
        String options = StringUtils.join(splittedArgs, "", 1, splittedArgs.length);
        return classifierName + options;
    }
    
    /**
     * Pretty prints the feature set
     * 
     * @param featureSet
     *            a comma separated list with feature names in angle brackets as provided by the
     *            discriminators.txt
     * @param cutPackageNames
     *            whether to keep or cut packages in features names
     * @return a shorter representation of names
     */
    public static String prettyPrintFeatureSet(String featureSet, boolean cutPackageNames)
    {
        List<String> buffer = new ArrayList<String>();
        String[] splittedArgs = featureSet.substring(1, featureSet.length() - 1).split(",");
        for (String fullFeatureName : splittedArgs) {
            if (cutPackageNames) {
                buffer.add(cutPackageNames(fullFeatureName.trim()));
            }
            else {
                buffer.add(fullFeatureName.trim());
            }
        }
        return StringUtils.join(buffer, ",");
    }

    private static String cutPackageNames(String splittedArg)
    {
        String[] nameArray = splittedArg.trim().split("\\.");
        String featureName = nameArray[nameArray.length - 1];
        return featureName;
    }

    /**
     * Pretty prints the feature arguments
     * 
     * @param featureArgs
     *            a comma separated list with feature arguments in angle brackets as provided by the
     *            discriminators.txt
     * @return a shorter representation of names
     */
    public static String prettyPrintFeatureArgs(String featureArgs)
    {
        ArrayList<String> names = new ArrayList<String>();
        String[] splittedArgs = featureArgs.substring(1, featureArgs.length() - 1).split(",");
        // feature args must come in pairs
        for (int i = 0; i < splittedArgs.length; i += 2) {
            String argName = splittedArgs[i].trim();
            String argVal = splittedArgs[i + 1].trim();
            String fullArg = argName + ":" + argVal;
            names.add(fullArg);
        }
        return StringUtils.join(names, ",");
    }
}
