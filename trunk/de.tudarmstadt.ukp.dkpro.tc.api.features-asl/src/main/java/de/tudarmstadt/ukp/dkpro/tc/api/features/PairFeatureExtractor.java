package de.tudarmstadt.ukp.dkpro.tc.api.features;

import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

public interface PairFeatureExtractor 
{
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException;
}
