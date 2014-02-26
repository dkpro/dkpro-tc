package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.io.TCReaderMultiLabel;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class TestReaderMultiLabel
    extends TextReader
    implements TCReaderMultiLabel
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

        for (String outcomeValue : getTextClassificationOutcomes(jcas)) {
            TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
            outcome.setOutcome(outcomeValue);
            outcome.addToIndexes();
        }
    }

    @Override
    public Set<String> getTextClassificationOutcomes(JCas jcas)
        throws CollectionException
    {
        return new HashSet<String>(Arrays.asList(new String[] { "test_outcome1", "test_outcome2",
                "test_outcome3" }));
    }
}