package de.tudarmstadt.ukp.dkpro.tc.api.io;

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
    /**
     * Returns the set of text classification outcomes for the current multi-label instance
     * 
     * @param jcas
     * @return
     * @throws CollectionException
     */
    public Set<String> getTextClassificationOutcomes(JCas jcas) throws CollectionException;
}