/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.features.tcu;

import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * A feature that allows to provide context surface information as feature.
 */
public class TargetSurfaceFormContextFeature
    extends TcuLookUpTable
{
    public static final String PARAM_LOWER_CASE = "useLowerCase";
    @ConfigurationParameter(name = PARAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    protected boolean useLowerCase;

    /**
     * If zero is provided the surface form of the current target is set as feature value. By
     * setting positive or negative indices local context can be set i.e. -1 will set the text of
     * the previous target annotation that occurs before the annotation for which the feature
     * extractor is currently executed. Likewise +1 will set the text of the following target annotation.
     */
    public static final String PARAM_RELATIVE_TARGET_ANNOTATION_INDEX = "targetAnnotation";
    @ConfigurationParameter(name = PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, mandatory = true)
    protected Integer shiftIdx;

    public static final String FEATURE_NAME = "context_";
    final static String END_OF_SEQUENCE = "EOS";
    static final String BEG_OF_SEQUENCE = "BOS";
    static final String OUT_OF_BOUNDARY = "OOB";

    public Set<Feature> extract(JCas aView, TextClassificationTarget unit)
        throws TextClassificationException
    {
        super.extract(aView, unit);

        Integer currentTargetIdx = super.unitBegin2Idx.get(unit.getBegin());

        Integer targetIdx = currentTargetIdx + shiftIdx;

        String featureVal = getTargetText(targetIdx);
        return new Feature(FEATURE_NAME + toHumanReadable(shiftIdx), featureVal).asSet();
    }

    private String toHumanReadable(Integer shiftIdx)
    {
        if (shiftIdx < 0) {
            return "minus" + Math.abs(shiftIdx);
        }
        else if (shiftIdx > 0) {
            return "plus" + Math.abs(shiftIdx);
        }
        return "current";
    }

    private String lowerCase(String token)
    {
        if (useLowerCase) {
            return token.toLowerCase();
        }

        return token;
    }

    private String getTargetText(Integer idx)
    {
        if (idx == -1) {
            return BEG_OF_SEQUENCE;
        }else if(idx < -1){
            return OUT_OF_BOUNDARY;
        }
        else if (idx == units.size()) {
            return END_OF_SEQUENCE;
        }else if(idx > units.size()){
            return OUT_OF_BOUNDARY;
        }
        
        return lowerCase(units.get(idx).getCoveredText());
    }

}
