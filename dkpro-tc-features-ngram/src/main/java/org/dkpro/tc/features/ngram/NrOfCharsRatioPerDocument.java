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
package org.dkpro.tc.features.ngram;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.meta.MaxNrOfCharsPerCasMC;

/**
 * Ratio of the number of characters in a document with respect to the longest document in the training data
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class NrOfCharsRatioPerDocument extends MaximunNormalizationExtractorBase  {

	public static final String FEATURE_NAME = "NumberOfCharsPertokenRatio";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
			throws TextClassificationException {

		long maxLen = getMax();

		int len = target.getCoveredText().length();
		
		double ratio = getRatio(len, maxLen);
		return new Feature(FEATURE_NAME, ratio, FeatureType.NUMERIC).asSet();
	}

	@Override
	public List<MetaCollectorConfiguration> getMetaCollectorClasses(Map<String, Object> parameterSettings)
			throws ResourceInitializationException {

		return Arrays.asList(
				new MetaCollectorConfiguration(MaxNrOfCharsPerCasMC.class, parameterSettings)
						.addStorageMapping(MaxNrOfCharsPerCasMC.PARAM_TARGET_LOCATION,
								NrOfCharsRatioPerDocument.PARAM_SOURCE_LOCATION,
								MaxNrOfCharsPerCasMC.LUCENE_DIR));
	}

	@Override
	protected String getFieldName() {
		return MaxNrOfCharsPerCasMC.LUCENE_MAX_CHAR_FIELD + featureExtractorName;
	}

	@Override
	protected String getFeaturePrefix() {
		return "maxTokenCountPerToken";
	}

}
