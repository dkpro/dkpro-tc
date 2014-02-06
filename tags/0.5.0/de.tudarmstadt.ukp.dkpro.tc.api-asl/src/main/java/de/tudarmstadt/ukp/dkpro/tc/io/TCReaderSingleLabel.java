package de.tudarmstadt.ukp.dkpro.tc.io;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

/**
 * Interface that should be implemented by readers for single label setups.
 * 
 * @author zesch
 *
 */
public interface TCReaderSingleLabel
{
    public String getTextClassificationOutcome(JCas jcas) throws CollectionException;
}
