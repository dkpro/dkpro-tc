package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.style;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureExtractor;

public class TypeTokenPairFeatureExtractor
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

    private double getTypeTokenRatio(JCas view)
    {
        Set<String> types = new HashSet<String>();
        for (Lemma lemma : JCasUtil.select(view, Lemma.class)) {
            types.add(lemma.getValue());
        }
        return types.size() / (double) JCasUtil.select(view, Lemma.class).size();
    }
}
