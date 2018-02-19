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
package org.dkpro.tc.core.task.uima;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.Constants;

public class OutcomeCollector extends JCasAnnotator_ImplBase implements Constants {
	
	public static final String PARAM_TARGET_FOLDER = "PARAM_TARGET_FOLDER";
	@ConfigurationParameter(name = "PARAM_TARGET_FOLDER", mandatory = true)
	private String targetFolder;

	Set<String> outcomes = new HashSet<>();

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<TextClassificationOutcome> tcos = JCasUtil.select(aJCas, TextClassificationOutcome.class);
		for(TextClassificationOutcome o : tcos){
			outcomes.add(o.getOutcome());
		}
	}

	@Override
	public void collectionProcessComplete() {
		File file = new File(targetFolder, Constants.FILENAME_OUTCOMES);
		try {
			FileUtils.writeLines(new File(targetFolder, Constants.FILENAME_OUTCOMES), "utf-8", outcomes);
		} catch (IOException e) {
			throw new UnsupportedOperationException("Failed to write outcomes to [" + file.getAbsolutePath() + "]");
		}
	}
}