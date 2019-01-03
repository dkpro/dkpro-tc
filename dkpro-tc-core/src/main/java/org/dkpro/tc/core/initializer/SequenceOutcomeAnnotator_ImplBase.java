/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.core.initializer;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

public abstract class SequenceOutcomeAnnotator_ImplBase
    extends JCasAnnotator_ImplBase
    implements SequenceOutcomeAnnotator
{

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException
    {
        for (TextClassificationTarget unit : JCasUtil.selectCovered(jcas,
                TextClassificationTarget.class,
                JCasUtil.selectSingle(jcas, TextClassificationSequence.class))) {
            TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, unit.getBegin(),
                    unit.getEnd());
            outcome.setOutcome(getTextClassificationOutcome(jcas, unit));
            outcome.setWeight(getTextClassificationOutcomeWeight(jcas, unit));
            outcome.addToIndexes();
        }
    }

    public double getTextClassificationOutcomeWeight(JCas jcas, TextClassificationTarget unit)
    {
        /**
         * By default, set all the instance outcome weights equally to one
         */
        return 1.0;
    }
}
