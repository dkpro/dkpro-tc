package de.tudarmstadt.ukp.dkpro.tc.features.syntax;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

public class SuperlativeRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_SUPERLATIVE_RATIO_ADJ = "SuperlativeRatioAdj";
    public static final String FN_SUPERLATIVE_RATIO_ADV = "SuperlativeRatioAdv";

    @Override
    public List<IFeature> extract(JCas jcas)
        throws TextClassificationException
    {
        double adjRatio = 0;
        int superlativeAdj = 0;
        int adjectives = 0;
        for (ADJ tag : JCasUtil.select(jcas, ADJ.class)) {
            adjectives++;
            // FIXME depends on tagset
            if (tag.getPosValue().contains("JJS")) {
                superlativeAdj++;
            }
        }
        if (adjectives > 0) {
            adjRatio = (double) superlativeAdj / adjectives;
        }

        double advRatio = 0;
        int superlativeAdv = 0;
        int adverbs = 0;
        for (ADV tag : JCasUtil.select(jcas, ADV.class)) {
            adverbs++;
            // FIXME depends on tagset
            if (tag.getPosValue().contains("RBS")) {
                superlativeAdv++;
            }
        }
        if (adverbs > 0) {
            advRatio = (double) superlativeAdv / adverbs;
        }

        List<IFeature> featList = new ArrayList<IFeature>();
        featList.add(new Feature(FN_SUPERLATIVE_RATIO_ADJ, adjRatio));
        featList.add(new Feature(FN_SUPERLATIVE_RATIO_ADV, advRatio));

        return featList;
    }
}
