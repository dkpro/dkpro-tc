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
package org.dkpro.tc.core.feature;

import static org.dkpro.tc.core.Constants.ID_FEATURE_NAME;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class InstanceIdFeature
{

    public static Feature retrieve(JCas jcas)
        throws TextClassificationException
    {
        String fullId = getFullId(jcas);
        return new Feature(ID_FEATURE_NAME, fullId);
    };

    public static Feature retrieve(JCas jcas, TextClassificationTarget unit)
        throws TextClassificationException
    {
        String fullId = getFullId(jcas);
        fullId = fullId + "_" + unit.getId();

        String suffix = unit.getSuffix();
        if (suffix != null && suffix.length() > 0) {
            fullId = fullId + "_" + suffix;

        }

        return new Feature(ID_FEATURE_NAME, fullId);
    };

    public static Feature retrieve(JCas jcas, TextClassificationTarget unit, Integer sequenceId)
        throws TextClassificationException
    {
        String fullId = getFullId(jcas);

        fullId = fullId + "_" + sequenceId;
        fullId = fullId + "_" + unit.getId();

        String suffix = unit.getSuffix();
        if (suffix != null && suffix.length() > 0) {
            fullId = fullId + "_" + suffix;
        }

        return new Feature(ID_FEATURE_NAME, fullId);
    };

    private static String getFullId(JCas jcas)
        throws TextClassificationException
    {
        JCasId jcasId = JCasUtil.selectSingle(jcas, JCasId.class);
        return "" + jcasId.getId();
    }
}
