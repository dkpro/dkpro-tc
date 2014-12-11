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
package de.tudarmstadt.ukp.dkpro.tc.mallet.writer;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.api.features.MissingValue.MissingValueType;

/**
 * @deprecated As of release 0.7.0, only dkpro-tc-ml-crfsuite is supported
 */
public class MalletFeatureEncoder
{

    /**
     * A map returning a String value for each valid {@link MissingValueType}
     * 
     * @return a map with {@link MissingValueType} keys, and strings as values
     */
    public static Map<MissingValueType, String> getMissingValueConversionMap()
    {
        Map<MissingValueType, String> map = new HashMap<MissingValueType, String>();
        // for booelan attributes: false
        map.put(MissingValueType.BOOLEAN, "0");
        // for numeric attributes: zero
        map.put(MissingValueType.NUMERIC, "0");
        // TODO is this really what we want?
        // for nominal attributes: the first
        map.put(MissingValueType.NOMINAL, "0");
        // TODO is this really what we want?
        // for string attributes: the first
        map.put(MissingValueType.STRING, "0");
        return map;
    }
}
