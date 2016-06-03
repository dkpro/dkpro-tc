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
package org.dkpro.tc.core.feature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.meta.MetaCollector;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.api.type.TextClassificationUnit;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class ContextCollectorUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor, MetaDependent
{
    public static final int CONTEXT_WIDTH = 30;

    @ConfigurationParameter(name = UnitContextMetaCollector.PARAM_CONTEXT_FILE, mandatory = true)
    private File contextFile;

    private BufferedWriter bw;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
    {
        boolean result = false;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(contextFile), "utf-8"));
            result = super.initialize(aSpecifier, aAdditionalParams);
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }

        return result;
    }

    @Override
    public List<java.lang.Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(UnitContextMetaCollector.class);

        return metaCollectorClasses;
    };

    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationUnit unit)
        throws TextClassificationException
    {
        try {
            String idString = (String) InstanceIdFeature.retrieve(jcas, unit).getValue();
            ContextMetaCollectorUtil.addContext(jcas, unit, idString, bw);

            if (DocumentMetaData.get(jcas).getIsLastSegment() == true) {
                bw.close();
            }
        }
        catch (IOException e) {
            throw new TextClassificationException(e);
        }
        return new HashSet<Feature>();
    }
}
