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
package org.dkpro.tc.features.maxnormalization;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.MaximumNormalizationExtractorBase;
import org.dkpro.tc.features.ngram.meta.maxnormalization.MaxNrOfCharsOverAllTokensMC;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Ratio of the number of characters in a document with respect to the longest document in the training data
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class AvgTokenLengthRatioPerDocument extends MaximumNormalizationExtractorBase  {

	public static final String FEATURE_NAME = "NumberOfCharsPerTokenRatio";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget)
			throws TextClassificationException {

		long maxLen = getMax();
		
		double avgTokenLen=0.0;
		Collection<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, aTarget);
		for(Token t : tokens){
			avgTokenLen += t.getCoveredText().length();
		}
		avgTokenLen /= tokens.size();

		double ratio = getRatio(avgTokenLen, maxLen);
		return new Feature(FEATURE_NAME, ratio, FeatureType.NUMERIC).asSet();
	}

	@Override
	public List<MetaCollectorConfiguration> getMetaCollectorClasses(Map<String, Object> parameterSettings)
			throws ResourceInitializationException {

		return Arrays.asList(
				new MetaCollectorConfiguration(MaxNrOfCharsOverAllTokensMC.class, parameterSettings)
						.addStorageMapping(MaxNrOfCharsOverAllTokensMC.PARAM_TARGET_LOCATION,
								AvgTokenLengthRatioPerDocument.PARAM_SOURCE_LOCATION,
								MaxNrOfCharsOverAllTokensMC.LUCENE_DIR));
	}

	@Override
	protected String getFieldName() {
		return MaxNrOfCharsOverAllTokensMC.LUCENE_MAX_CHAR_FIELD + featureExtractorName;
	}

	@Override
	protected String getFeaturePrefix() {
		return getClass().getSimpleName();
	}

}
