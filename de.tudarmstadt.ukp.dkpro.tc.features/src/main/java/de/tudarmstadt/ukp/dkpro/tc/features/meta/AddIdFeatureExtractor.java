package de.tudarmstadt.ukp.dkpro.tc.features.meta;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * Adds the documentId to the features.
 * 
 * Needs to be temporarily filtered out for building the model and for classification.
 * 
 * @author zesch
 *
 */
public class AddIdFeatureExtractor
    implements SimpleFeatureExtractor
{

    public static final String ID_FEATURE_NAME = "DKProTCInstanceID";
    
    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws CleartkExtractorException
    {
        String docId = DocumentMetaData.get(jcas).getDocumentId();

        return Arrays.asList(new Feature(ID_FEATURE_NAME, docId));
    }
}