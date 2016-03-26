/*******************************************************************************
 * Copyright 2015
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.Level;
import org.codehaus.plexus.util.FileUtils;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.meta.MetaCollector;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.api.type.TextClassificationFocus;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.dkpro.tc.core.util.TaskUtils;

public class ContextCollectorUFE extends FeatureExtractorResource_ImplBase implements ClassificationUnitFeatureExtractor, MetaDependent
{
	public static final int CONTEXT_WIDTH = 30;

	private StringBuilder sb;
	
	@ConfigurationParameter(name = UnitContextMetaCollector.PARAM_CONTEXT_FILE, mandatory = true)
    private File contextFile;
	
	@Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) {
		sb = new StringBuilder();
		
		boolean result = false;
		
		try {
			result = super.initialize(aSpecifier, aAdditionalParams);
						
		} catch (ResourceInitializationException e) {
			this.getLogger().log(Level.WARNING, "Error trying to initialize: " + e);
		}
		
		return result;
	}

	@Override
	public List<java.lang.Class<? extends MetaCollector>> getMetaCollectorClasses() {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(UnitContextMetaCollector.class);

        return metaCollectorClasses;
	};
	
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationUnit unit)
			throws TextClassificationException {
		
		TextClassificationFocus focus = JCasUtil.selectSingle(jcas, TextClassificationFocus.class);
		
		TextClassificationUnit unitUnderFocus = TaskUtils.tryGetMatchingUnitForFocus(jcas, focus);
		if(unitUnderFocus != null && unitUnderFocus.equals(unit)) {
        	String idString = (String) InstanceIdFeature.retrieve(jcas, unit).getValue();
        	ContextMetaCollectorUtil.addContext(jcas, unit, idString, sb);
        }
		
		if(DocumentMetaData.get(jcas).getIsLastSegment() == true) {
			try {
				FileUtils.fileAppend(contextFile.getAbsolutePath(), sb.toString());
			} catch (IOException e) {
				throw new TextClassificationException(e);
			}
		}
		
		return new HashSet<Feature>();
	}		
}
