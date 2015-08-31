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
package de.tudarmstadt.ukp.dkpro.tc.core.initializer;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public abstract class SequenceOutcomeAnnotator_ImplBase
	extends JCasAnnotator_ImplBase
	implements SequenceOutcomeAnnotator
{
 
	@Override
	public void process(JCas jcas)
			throws AnalysisEngineProcessException
	{
        for (TextClassificationUnit unit : JCasUtil.selectCovered(jcas, TextClassificationUnit.class, JCasUtil.selectSingle(jcas, TextClassificationSequence.class))) {
            TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, unit.getBegin(), unit.getEnd());
            outcome.setOutcome(getTextClassificationOutcome(jcas, unit));
            outcome.setWeight(getTextClassificationOutcomeWeight(jcas, unit));
            outcome.addToIndexes();
        }
	}
    
	public double getTextClassificationOutcomeWeight(JCas jcas, TextClassificationUnit unit) {
    	/**
    	 * By default, set all the instance outcome weights equally to one
    	 */
		return 1.0;
	}
}
