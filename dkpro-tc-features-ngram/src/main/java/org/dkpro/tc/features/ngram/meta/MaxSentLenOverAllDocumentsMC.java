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
package org.dkpro.tc.features.ngram.meta;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class MaxSentLenOverAllDocumentsMC extends LuceneMetaCollector {

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	}

	@Override
	protected FrequencyDistribution<String> getNgramsFD(JCas jcas) throws TextClassificationException {

		FrequencyDistribution<String> fd = new FrequencyDistribution<>();

		for (Sentence s : JCasUtil.select(jcas, Sentence.class)) {
			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, s);

			StringBuilder sb = new StringBuilder();
			for (Token t : tokens) {
				sb.append(t.getCoveredText() + "_");
			}

			String key = tokens.size() + "_" + sb.toString().hashCode();
			if (fd.contains(key)) {
				// do not add ''same'' sentences multiple times 
				continue;
			}

			fd.addSample(key, tokens.size());
		}

		return fd;
	}

	@Override
	protected String getFieldName() {
		return featureExtractorName;
	}
}