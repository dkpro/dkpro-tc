package de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups.io;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.core.io.SingleLabelReaderBase;

public class TwentyNewsgroupsCorpusReader
    extends SingleLabelReaderBase
{
    @Override
    public String getTextClassificationOutcome(JCas jcas)
            throws CollectionException
    {
        try {
            String uriString = DocumentMetaData.get(jcas).getDocumentUri();
            return new File(new URI(uriString).getPath()).getParentFile().getName();
        }
        catch (URISyntaxException e) {
            throw new CollectionException(e);
        }
    }
}