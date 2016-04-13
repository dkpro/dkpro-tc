package org.dkpro.tc.examples.model;

import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationUnit;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class WekaUnitAnnotator
    extends JCasAnnotator_ImplBase
{
    int tcId = 0;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);

        for (Token token : tokens) {
                TextClassificationUnit unit = new TextClassificationUnit(aJCas, token.getBegin(),
                        token.getEnd());
                unit.setId(tcId++);
                unit.setSuffix(token.getCoveredText());
                unit.addToIndexes();

                TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas,
                        token.getBegin(), token.getEnd());
                outcome.setOutcome(getTextClassificationOutcome(aJCas, unit));
                outcome.addToIndexes();
        }
    }

    public String getTextClassificationOutcome(JCas jcas, TextClassificationUnit unit)
    {
        List<POS> posList = JCasUtil.selectCovered(jcas, POS.class, unit);
        String outcome = posList.get(0).getPosValue();
        return outcome;
    }

}
