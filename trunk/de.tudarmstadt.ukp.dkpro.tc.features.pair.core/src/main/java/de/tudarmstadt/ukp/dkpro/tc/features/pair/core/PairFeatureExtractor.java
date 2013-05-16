package de.tudarmstadt.ukp.dkpro.tc.features.pair.core;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;

public interface PairFeatureExtractor 
{
    public List<Feature> extract(JCas view1, JCas view2)
        throws CleartkExtractorException;
}
