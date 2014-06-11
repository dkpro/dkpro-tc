package de.tudarmstadt.ukp.dkpro.tc.examples.io;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

/**
 * Reads plain text tweets, labels each tweet as sentence, and assigns no label to them.
 */
public class UnlabeledTweetReader
    extends TextReader
    implements TCReaderSingleLabel
{
    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        super.getNext(aCAS);

        JCas jcas;
        try {
            jcas = aCAS.getJCas();
            // consider a tweet to be a sentence
            Sentence sentenceAnno = new Sentence(jcas);
            sentenceAnno.setBegin(0);
            sentenceAnno.setEnd(jcas.getDocumentText().length());
            sentenceAnno.addToIndexes();
        }
        catch (CASException e) {
            throw new CollectionException();
        }

        TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
        outcome.setOutcome(getTextClassificationOutcome(jcas));
        outcome.addToIndexes();
    }

    @Override
    public String getTextClassificationOutcome(JCas jcas)
        throws CollectionException
    {
        return Constants.UNKNOWN_OUTCOME;
    }
}