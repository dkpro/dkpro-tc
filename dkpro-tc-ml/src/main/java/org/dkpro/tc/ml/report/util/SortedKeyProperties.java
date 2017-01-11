/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.ml.report.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Provides sorted keys for properties file to support human readability i.e. sustain items in th
 * sequence they were classified as additional debug support
 */
@SuppressWarnings("serial")
public class SortedKeyProperties
    extends Properties
{

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Enumeration keys()
    {

        final Object[] keys = super.keySet().toArray();
        Arrays.sort(keys);

        return new Enumeration()
        {
            int idx;

            @Override
            public boolean hasMoreElements()
            {
                return idx < keys.length;
            }

            @Override
            public Object nextElement()
            {
                return keys[idx++];
            }
        };
    }
}
