package de.tudarmstadt.ukp.dkpro.tc.core.feature;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Adds the documentId to the features.
 * 
 * Needs to be temporarily filtered out for building the model and for classification.
 * 
 * @author zesch
 * 
 */
public class AddIdFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{

    /**
     * Public name of this feature
     */
    public static final String ID_FEATURE_NAME = "DKProTCInstanceID";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        String docId = DocumentMetaData.get(jcas).getDocumentId();
        if (docId == null) {
            throw new TextClassificationException("DocumentId cannot be empty");
        }

        return Arrays.asList(new Feature(ID_FEATURE_NAME, docId));
    }
}