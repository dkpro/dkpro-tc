package de.tudarmstadt.ukp.dkpro.tc.core.feature;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

/**
 * Adds the documentId to the features.
 * 
 * Needs to be temporarily filtered out for building the model and for classification.
 * 
 * @author zesch
 *
 */
public class AddIdFeatureExtractor
    implements FeatureExtractor
{

    public static final String ID_FEATURE_NAME = "DKProTCInstanceID";
    
    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws TextClassificationException
    {
        String docId = DocumentMetaData.get(jcas).getDocumentId();
        
        return Arrays.asList(new Feature(ID_FEATURE_NAME, docId));
    }
}