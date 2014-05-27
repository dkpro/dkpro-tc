package de.tudarmstadt.ukp.dkpro.tc.features.style;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

/**
 * Extracts whether the first character of the classification unit is upper-case or not.
 * 
 * @author Andriy Nadolskyy
 * @author daxenberger
 */
public class InitialCharacterUpperCaseUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    /**
     * Public name of the feature "Initial character upper case"
     */
    public static final String INITIAL_CH_UPPER_CASE = "InitialCharacterUpperCaseUFE";

    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
        String token = classificationUnit.getCoveredText();

        List<Feature> featList = new ArrayList<Feature>();
        boolean bool = false;
        if (Character.isUpperCase(token.charAt(0))) {
            featList.add(new Feature(INITIAL_CH_UPPER_CASE, !bool));
        }
        else {
            featList.add(new Feature(INITIAL_CH_UPPER_CASE, bool));
        }
        return featList;
    }
}