package de.tudarmstadt.ukp.dkpro.tc.features.syntax;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CARD;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

public class POSRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_ADJ_RATIO = "AdjRatioFeature";
    public static final String FN_ADV_RATIO = "AdvRatioFeature";
    public static final String FN_ART_RATIO = "ArtRatioFeature";
    public static final String FN_CARD_RATIO = "CardRatioFeature";
    public static final String FN_CONJ_RATIO = "ConjRatioFeature";
    public static final String FN_N_RATIO = "NRatioFeature";
    public static final String FN_O_RATIO = "ORatioFeature";
    public static final String FN_PP_RATIO = "PpRatioFeature";
    public static final String FN_PR_RATIO = "PrRatioFeature";
    public static final String FN_PUNC_RATIO = "PuncRatioFeature";
    public static final String FN_V_RATIO = "VRatioFeature";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        List<Feature> featList = new ArrayList<Feature>();

        double total = JCasUtil.select(jcas, POS.class).size();
        double adj = select(jcas, ADJ.class).size() / total;
        double adv = select(jcas, ADV.class).size() / total;
        double art = select(jcas, ART.class).size() / total;
        double card = select(jcas, CARD.class).size() / total;
        double conj = select(jcas, CONJ.class).size() / total;
        double noun = select(jcas, N.class).size() / total;
        double other = select(jcas, O.class).size() / total;
        double prep = select(jcas, PP.class).size() / total;
        double pron = select(jcas, PR.class).size() / total;
        double punc = select(jcas, PUNC.class).size() / total;
        double verb = select(jcas, V.class).size() / total;

        featList.addAll(Arrays.asList(new Feature(FN_ADJ_RATIO, adj)));
        featList.addAll(Arrays.asList(new Feature(FN_ADV_RATIO, adv)));
        featList.addAll(Arrays.asList(new Feature(FN_ART_RATIO, art)));
        featList.addAll(Arrays.asList(new Feature(FN_CARD_RATIO, card)));
        featList.addAll(Arrays.asList(new Feature(FN_CONJ_RATIO, conj)));
        featList.addAll(Arrays.asList(new Feature(FN_N_RATIO, noun)));
        featList.addAll(Arrays.asList(new Feature(FN_O_RATIO, other)));
        featList.addAll(Arrays.asList(new Feature(FN_PR_RATIO, prep)));
        featList.addAll(Arrays.asList(new Feature(FN_PP_RATIO, pron)));
        featList.addAll(Arrays.asList(new Feature(FN_PUNC_RATIO, punc)));
        featList.addAll(Arrays.asList(new Feature(FN_V_RATIO, verb)));

        return featList;
    }
}