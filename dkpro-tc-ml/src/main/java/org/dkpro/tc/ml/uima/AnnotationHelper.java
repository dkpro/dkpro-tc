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

package org.dkpro.tc.ml.uima;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.dkpro.tc.core.Constants;

/**
 * Creates TC Unit and TC Outcome annotations for all units of the
 * given type.
 * 
 * @author Martin Wunderlich (martin@wunderlich.com)
 *
 */
public class AnnotationHelper extends JCasAnnotator_ImplBase {
	public static final String PARAM_NAME_UNIT_ANNOTATION = "unitAnnotation";
	@ConfigurationParameter(name = PARAM_NAME_UNIT_ANNOTATION, mandatory = true)
	private String nameUnit;
	private int currentId = 0;
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		if(nameUnit == null || nameUnit.isEmpty())
			throw new IllegalStateException("The name of the unit to annotate must be set.");
		
		Type type = aJCas.getCas().getTypeSystem().getType(nameUnit);

		Collection<AnnotationFS> units = CasUtil.select(aJCas.getCas(), type);

		for (AnnotationFS unit : units) {
			int begin = unit.getBegin();
			int end = unit.getEnd();
			
			TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas, begin, end);
	        outcome.setOutcome(Constants.TC_OUTCOME_DUMMY_VALUE);
	        outcome.addToIndexes();
			
	        TextClassificationUnit tcUnit = new TextClassificationUnit(aJCas, begin, end);
	        tcUnit.setId( getNextId() );
	        tcUnit.addToIndexes();
    	}
	}

	private int getNextId() {
		return currentId++;
	}
}
