package de.tudarmstadt.ukp.dkpro.tc.features.syntax;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

public class PluralRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_PLURAL_RATIO = "PluralRatio";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        int plural = 0;
        int singular = 0;

        for (POS tag : JCasUtil.select(jcas, N.class)) {
            // FIXME depends on tagset
            if ((tag.getPosValue().equals("NNS")) || (tag.getPosValue().equals("NNPS"))
                    || (tag.getPosValue().equals("NNS"))) {
                plural++;
            }
            else if ((tag.getPosValue().equals("NNP")) || (tag.getPosValue().equals("NN"))) {
                singular++;
            }
        }
        List<Feature> featList = new ArrayList<Feature>();
        if ((singular + plural) > 0) {
            featList.add(new Feature(FN_PLURAL_RATIO, (double) plural
                    / (singular + plural)));
        }
        return featList;
    }
}