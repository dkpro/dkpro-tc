package org.dkpro.tc.examples.io.anno;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class UnitOutcomeAnnotator
    extends JCasAnnotator_ImplBase
{
    int tcId = 0;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {

        List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));

        for (Token token : tokens) {
            TextClassificationTarget target = new TextClassificationTarget(aJCas, token.getBegin(),
                    token.getEnd());
            target.setId(tcId++);
            target.setSuffix(token.getCoveredText());
            target.addToIndexes();

            TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas,
                    token.getBegin(), token.getEnd());
            outcome.setOutcome(getTextClassificationOutcome(aJCas, target));
            outcome.addToIndexes();

        }
    }

    public String getTextClassificationOutcome(JCas jcas, TextClassificationTarget target)
    {
        List<POS> posList = JCasUtil.selectCovered(jcas, POS.class, target);
        return posList.get(0).getPosValue();
    }

}
