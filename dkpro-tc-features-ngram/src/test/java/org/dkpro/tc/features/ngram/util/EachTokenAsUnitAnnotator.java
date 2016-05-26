/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.features.ngram.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationUnit;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class EachTokenAsUnitAnnotator
    extends JCasAnnotator_ImplBase
{
    int tcId = 0;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {

        List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));

        for (Token token : tokens) {
            TextClassificationUnit unit = new TextClassificationUnit(aJCas, token.getBegin(),
                    token.getEnd());
            unit.setId(tcId++);
            unit.setSuffix(token.getCoveredText());
            unit.addToIndexes();

            TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas,
                    token.getBegin(), token.getEnd());
            outcome.setOutcome("X");
            outcome.addToIndexes();
        }
    }
}
