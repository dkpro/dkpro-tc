/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.core.feature;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationFocus;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;


public class SequenceContextMetaCollector 
	extends ContextMetaCollector_ImplBase
{
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
  
		TextClassificationFocus focus = JCasUtil.selectSingle(jcas, TextClassificationFocus.class);
		TextClassificationSequence sequence = JCasUtil.selectCovered(jcas, TextClassificationSequence.class, focus).get(0);
		int id = sequence.getId();
        for (TextClassificationUnit unit : JCasUtil.selectCovered(jcas, TextClassificationUnit.class, sequence)) {
            String idString = (String) InstanceIdFeature.retrieve(jcas, unit, id).getValue();
            addContext(jcas, unit, idString, sb);
        }
	}
}