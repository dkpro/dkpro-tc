package de.tudarmstadt.ukp.dkpro.tc.features.length;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

/**
 * Extracts the number of characters in the unit (basically, its length).
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NrOfCharsUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    /**
     * Public name of the feature "number of characters"
     */
    public static final String NR_OF_CHARS = "NrofChars";

    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
        int nrOfChars = classificationUnit.getEnd() - classificationUnit.getBegin();
        List<Feature> featList = new ArrayList<Feature>();
        featList.add(new Feature(NR_OF_CHARS, nrOfChars));

        return featList;
    }
}