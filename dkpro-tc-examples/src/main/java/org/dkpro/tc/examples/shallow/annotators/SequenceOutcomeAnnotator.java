/**
 * Copyright 2019
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.examples.shallow.annotators;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class SequenceOutcomeAnnotator
    extends JCasAnnotator_ImplBase
{
    int tcId = 0;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        for (Sentence sent : JCasUtil.select(aJCas, Sentence.class)) {
            TextClassificationSequence sequence = new TextClassificationSequence(aJCas,
                    sent.getBegin(), sent.getEnd());
            sequence.addToIndexes();

            List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, sent);

            for (Token token : tokens) {
                TextClassificationTarget unit = new TextClassificationTarget(aJCas,
                        token.getBegin(), token.getEnd());
                unit.setId(tcId++);
                unit.setSuffix(token.getCoveredText());
                unit.addToIndexes();

                TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas,
                        token.getBegin(), token.getEnd());
                outcome.setOutcome(getTextClassificationOutcome(aJCas, unit));
                outcome.addToIndexes();
            }

        }
    }

    public String getTextClassificationOutcome(JCas jcas, TextClassificationTarget unit)
    {
        List<POS> posList = JCasUtil.selectCovered(jcas, POS.class, unit);
        return posList.get(0).getPosValue().replaceAll(" ", "_");
    }

}
