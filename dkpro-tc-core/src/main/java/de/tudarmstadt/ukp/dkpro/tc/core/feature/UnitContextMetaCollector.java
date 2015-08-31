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
package de.tudarmstadt.ukp.dkpro.tc.core.feature;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

/** A dummy meta-collector which merely sets up the correct file path
 * for the context file. This is required by the {@link ContextCollectorUFE},
 * which in turn is a prerequisite for the {@link BatchTrainTestDetailedOutcomeReport}.
 *
 */
public class UnitContextMetaCollector
	extends ContextMetaCollector_ImplBase
{
    
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// Intentionally do nothing here.
	}
	
	@Override
	public Map<String, String> getParameterKeyPairs() {
        Map<String, String> mapping = new HashMap<String, String>();
        
        mapping.put(PARAM_CONTEXT_FILE, Constants.ID_CONTEXT_KEY);
                
        return mapping;
	}
}