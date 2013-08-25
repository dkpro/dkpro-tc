package de.tudarmstadt.ukp.dkpro.tc.io;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

public interface TCReaderSingleLabel
{
    public String getTextClassificationOutcome(JCas jcas) throws CollectionException;
}
