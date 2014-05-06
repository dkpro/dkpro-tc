package de.tudarmstadt.ukp.dkpro.tc.api.io;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

/**
 * Interface that should be implemented by readers for sequence labeling setups.
 * 
 * @author zesch
 *
 */
public interface TCReaderSequence
{
    public String getTextClassificationOutcome(JCas jcas, TextClassificationUnit unit) throws CollectionException;
}