package de.tudarmstadt.ukp.dkpro.tc.features.syntax;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Quick, very simplified approximation of usage of past tense
 * in comparison to present/future tense in the text.
 * 
 * Works for Penn Treebank POS tags only.
 * 
 * Captures the ratio of all verbs to
 * "VBD" (verb praeterite) and "VBN" (verb past participle) as past and
 * "VB" (verb base form), "VBP" (verb present) and "VBZ" (verb present 3rd pers sg) as present/future.
 * The output is multiplied by 100 as the values are usually very small.
 */
public class PastVsFutureFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_PAST_RATIO = "PastVerbRatio";
    public static final String FN_FUTURE_RATIO = "FutureVerbRatio";
    public static final String FN_FUTURE_VS_PAST_RATIO = "FutureVsPastVerbRatio";

    @Override
    public List<Feature> extract(JCas jcas)
    {
        double pastRatio = 0.0;
        double futureRatio = 0.0;
        double futureToPastRatio = 0.0;
        int pastVerbs = 0;
        int futureVerbs = 0;
        int verbs = 0;

        for (V tag : JCasUtil.select(jcas, V.class)) {
            verbs++;
            // FIXME Issue 123: depends on tagset
            if (tag.getPosValue().contains("VBD") || tag.getPosValue().contains("VBN")) {
                pastVerbs++;
            }
            if (tag.getPosValue().contains("VB") || tag.getPosValue().contains("VBP")
                    || tag.getPosValue().contains("VBZ")) {
                futureVerbs++;
            }
        }
        if (verbs > 0) {
            pastRatio = (double) pastVerbs * 100 / verbs;
            futureRatio = (double) futureVerbs * 100 / verbs;
        }
        if ((pastRatio > 0) && (futureRatio > 0)) {
            futureToPastRatio = futureRatio / pastRatio;
        }

        List<Feature> featList = new ArrayList<Feature>();
        featList.add(new Feature(FN_PAST_RATIO, pastRatio));
        featList.add(new Feature(FN_FUTURE_RATIO, futureRatio));
        featList.add(new Feature(FN_FUTURE_VS_PAST_RATIO, futureToPastRatio));

        return featList;
    }
}
