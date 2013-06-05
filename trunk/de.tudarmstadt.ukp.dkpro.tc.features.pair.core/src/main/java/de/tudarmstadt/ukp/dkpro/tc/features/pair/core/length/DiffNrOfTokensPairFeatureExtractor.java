package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureExtractor;

/**
 * Pair-wise feature extractor
 * Computes the number of tokens in a view and returns the difference of both views.
 * @author erbs
 *
 */
public class DiffNrOfTokensPairFeatureExtractor
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        return Arrays.asList(
                new Feature("DiffNrOfTokens",
                        JCasUtil.select(view1, Token.class).size()
                                - JCasUtil.select(view2, Token.class).size())
                );
    }
}
