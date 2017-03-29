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
package org.dkpro.tc.core.feature;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class SequenceContextMetaCollector
    extends ContextMetaCollector_ImplBase
{

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {

        Collection<TextClassificationSequence> sequences = JCasUtil.select(jcas,
                TextClassificationSequence.class);
        for (TextClassificationSequence seq : sequences) {
            int id = seq.getId();
            for (TextClassificationTarget unit : JCasUtil.selectCovered(jcas,
                    TextClassificationTarget.class, seq)) {
                String idString;
                try {
                    idString = (String) InstanceIdFeature.retrieve(jcas, unit, id).getValue();
                    ContextMetaCollectorUtil.addContext(jcas, unit, idString, bw);
                }
                catch (Exception e) {
                    throw new AnalysisEngineProcessException(e);
                }
            }
        }
    }
}