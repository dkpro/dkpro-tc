package de.tudarmstadt.ukp.dkpro.tc.examples.io;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.core.io.SingleLabelReaderBase;

/**
 * Reads plain text tweets and labels each tweet as sentence.
 */
public class LabeledTweetReader
    extends SingleLabelReaderBase
{
    @Override
    public String getTextClassificationOutcome(JCas jcas)
        throws CollectionException
    {
        try {
            // consider a tweet to be a sentence
            Sentence sentenceAnno = new Sentence(jcas);
            sentenceAnno.setBegin(0);
            sentenceAnno.setEnd(jcas.getDocumentText().length());
            sentenceAnno.addToIndexes();

            String uriString = DocumentMetaData.get(jcas).getDocumentUri();
            return new File(new URI(uriString).getPath()).getParentFile().getName();
        }
        catch (URISyntaxException e) {
            throw new CollectionException(e);
        }
    }
}