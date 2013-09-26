package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

/**
 * Common signature for feature extractors which extract their features from the entire document
 * (view). Feature extractors which require specific annotations from which to extract their
 * features, should not implement this interface.
 * 
 */
public interface DocumentFeatureExtractor
{

    /**
     * Extracts features from the document text of the given view.
     * 
     * @param view
     *            the current view of the document.
     * @return a list of features generated by the extractor for the document.
     */
    public List<Feature> extract(JCas view)
        throws TextClassificationException;

}
