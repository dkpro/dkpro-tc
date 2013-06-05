package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureExtractor;

/**
 * Pair-wise feature extractor
 * Computes the number of characters in a view and returns the difference of both views.
 * @author erbs
 *
 */
public class DiffNrOfCharactersPairFeatureExtractor
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        return Arrays.asList(
                new Feature("DiffNrOfCharacters",
                        view1.getDocumentText().length() -
                                view2.getDocumentText().length())
                );

    }
}
