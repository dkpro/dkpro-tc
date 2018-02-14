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
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.base.LuceneFeatureExtractorBase;
import org.dkpro.tc.features.ngram.meta.MaximumNumberOfCharsPerCasMetaCollector;

/**
 * Extracts the number of sentences in this classification unit
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class NumberOfCharsRatio extends LuceneFeatureExtractorBase implements FeatureExtractor {

	public static final String FEATURE_NAME = "NumberOfCharsRatio";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
			throws TextClassificationException {

		long maxLen = getMax();

		int len = target.getCoveredText().length();
		
		double ratio = getRatio(len, maxLen);
		return new Feature(FEATURE_NAME, ratio, FeatureType.NUMERIC).asSet();
	}

	private double getRatio(int size, long maxLen) throws TextClassificationException {

		double value = (double) size / maxLen;

		if (value > 1.0) {
			// a larger value that during training was encountered; cap to 1.0
			value = 1.0;
		}

		if (value < 0) {
			throw new TextClassificationException("Negative sentence length encountered");
		}

		return value;
	}

	private long getMax() throws TextClassificationException {

		String string = "-1";
		try {
			string = getTopNgrams().getSampleWithMaxFreq().split("_")[0];
		} catch (ResourceInitializationException e) {
			throw new TextClassificationException(e);
		}
		return Long.parseLong(string);
	}

	@Override
	public List<MetaCollectorConfiguration> getMetaCollectorClasses(Map<String, Object> parameterSettings)
			throws ResourceInitializationException {

		return Arrays.asList(
				new MetaCollectorConfiguration(MaximumNumberOfCharsPerCasMetaCollector.class, parameterSettings)
						.addStorageMapping(MaximumNumberOfCharsPerCasMetaCollector.PARAM_TARGET_LOCATION,
								NumberOfCharsRatio.PARAM_SOURCE_LOCATION,
								MaximumNumberOfCharsPerCasMetaCollector.LUCENE_DIR));
	}

	@Override
	protected String getFieldName() {
		return MaximumNumberOfCharsPerCasMetaCollector.LUCENE_MAX_CHAR_FIELD + featureExtractorName;
	}

	@Override
	protected int getTopN() {
		return 1;
	}

	@Override
	protected String getFeaturePrefix() {
		return "maxTokenCountDoc";
	}

	@Override
	protected void logSelectionProcess(long N) {
		// no log message for this feature
	}
}
