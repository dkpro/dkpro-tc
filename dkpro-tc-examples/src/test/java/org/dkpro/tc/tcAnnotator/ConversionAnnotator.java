/*******************************************************************************
 * Copyright 2018
 * Language Technology Lab
 * University of Duisburg-Essen
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
package org.dkpro.tc.tcAnnotator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

/**
 * This is a dummy annotator that takes the predicted classification outcomes
 * and creates POS annotations from the predicted values. This annotator is not
 * intended for productive use but can be adapted accordingly.
 */
public class ConversionAnnotator extends JCasAnnotator_ImplBase {
	
	public static final String PARAM_SUFFIX = "pointlessParameterToTestParameterPassing";
	@ConfigurationParameter(name = PARAM_SUFFIX, mandatory = false)
	private String suffix;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		for (TextClassificationOutcome o : JCasUtil.select(aJCas, TextClassificationOutcome.class)) {
			POS p = new POS(aJCas, o.getBegin(), o.getEnd());
			
			String val = o.getOutcome();
			if(suffix != null && !suffix.isEmpty()) {
				val += suffix;
			}
			p.setPosValue(val);
			p.addToIndexes();
		}

	}

}
