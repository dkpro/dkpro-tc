package de.tudarmstadt.ukp.dkpro.tc.core.initializer;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

public interface SingleLabelOutcomeAnnotator {

    /**
     * Returns the text classification outcome for the current single-label instance
     * 
     * @param jcas
     * @return
     * @throws CollectionException
     */
    public String getTextClassificationOutcome(JCas jcas) throws AnalysisEngineProcessException;
}
