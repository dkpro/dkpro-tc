package de.tudarmstadt.ukp.dkpro.tc.demo.sentimentpolarity.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class MovieReviewCorpusReader
    extends TextReader
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

        DocumentMetaData dmd = DocumentMetaData.get(aCAS);
        File parentFile;
        try {
            parentFile = new File(new URI(dmd.getDocumentUri()).getPath()).getParentFile();
            
            TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
            outcome.setOutcome(parentFile.getName());
            outcome.addToIndexes();
        }
        catch (URISyntaxException e) {
            throw new CollectionException(e);
        }
    }
}