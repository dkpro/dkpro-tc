package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public abstract class SequenceLabelReaderBase
    extends TextReader
    implements TCReaderSequence
{

    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        super.getNext(aCAS);
        
        JCas jcas;
        try {
            jcas = aCAS.getJCas();
        }
        catch (CASException e) {
            throw new CollectionException();
        }

        for (TextClassificationUnit unit : JCasUtil.selectCovered(jcas, TextClassificationUnit.class, JCasUtil.selectSingle(jcas, TextClassificationSequence.class))) {
            TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, unit.getBegin(), unit.getEnd());
            outcome.setOutcome(getTextClassificationOutcome(jcas, unit));
            outcome.addToIndexes();
        }
    }
}