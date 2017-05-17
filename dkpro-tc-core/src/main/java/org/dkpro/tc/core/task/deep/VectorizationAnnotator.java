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
package org.dkpro.tc.core.task.deep;

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.DeepLearningConstants;

public class VectorizationAnnotator extends JCasAnnotator_ImplBase {
	public static final String PARAM_TARGET_DIRECTORY = "targetDirectory";
	@ConfigurationParameter(name = PARAM_TARGET_DIRECTORY, mandatory = true)
	protected File targetFolder;

	File instanceVectorFile;
	File labelVectorFile;
	File instanceMapping;
	File labelMapping;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		instanceVectorFile = new File(targetFolder, DeepLearningConstants.FILENAME_INSTANCE_VECTOR);
		labelVectorFile = new File(targetFolder, DeepLearningConstants.FILENAME_LABEL_VECTOR);
		instanceMapping = new File(targetFolder, DeepLearningConstants.FILENAME_INSTANCE_MAPPING);
		labelMapping = new File(targetFolder, DeepLearningConstants.FILENAME_LABEL_MAPPING);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	}

	@Override
	public void collectionProcessComplete() {
	}

}
