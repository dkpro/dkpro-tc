package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.style;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;

/**
 * Pair-wise feature extractor Computes the type-token-ratio in a view and returns the difference of
 * type-token-rations in both views.
 * 
 * @author nico.erbs@gmail.com
 * 
 */
public class TypeTokenPairFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        return Arrays.asList(
                new Feature("DiffTypeTokenRatio",

                        getTypeTokenRatio(view1) / getTypeTokenRatio(view2))
                );
    }

    /**
     * 
     * @param view
     *            the view for which the type-token-ratio is computed
     * @return type-token-ratio
     */
    private double getTypeTokenRatio(JCas view)
    {
        Set<String> types = new HashSet<String>();
        for (Lemma lemma : JCasUtil.select(view, Lemma.class)) {
            types.add(lemma.getValue());
        }
        return types.size() / (double) JCasUtil.select(view, Lemma.class).size();
    }
}
