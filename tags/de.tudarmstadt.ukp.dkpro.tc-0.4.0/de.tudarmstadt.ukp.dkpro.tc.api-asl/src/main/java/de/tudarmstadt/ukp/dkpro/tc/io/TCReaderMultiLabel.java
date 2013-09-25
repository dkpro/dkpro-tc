package de.tudarmstadt.ukp.dkpro.tc.io;

import java.util.Set;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

/**
 * Interface that should be implemented by readers for multi label setups.
 * 
 * @author zesch
 *
 */
public interface TCReaderMultiLabel
{
    public Set<String> getTextClassificationOutcomes(JCas jcas) throws CollectionException;
}