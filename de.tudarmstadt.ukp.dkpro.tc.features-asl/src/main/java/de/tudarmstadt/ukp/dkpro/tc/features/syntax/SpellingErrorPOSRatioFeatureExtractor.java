package de.tudarmstadt.ukp.dkpro.tc.features.syntax;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
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
import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

/**
 * Computes for each coarse grained POS tag the ratio of being affected by a spelling error. For
 * example, if there are 4 spelling errors in the document, and 3 of them affect nouns, while one
 * affects a verb, the ratio will be 0.75 for nouns, 0.25 for verbs, and 0.0 for all other POS.
 * 
 * @author zesch
 * 
 */
public class SpellingErrorPOSRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_ADJ_ERROR_RATIO = "AdjErrorRatio";
    public static final String FN_ADV_ERROR_RATIO = "AdvErrorRatio";
    public static final String FN_ART_ERROR_RATIO = "ArtErrorRatio";
    public static final String FN_CARD_ERROR_RATIO = "CardErrorRatio";
    public static final String FN_CONJ_ERROR_RATIO = "ConjErrorRatio";
    public static final String FN_N_ERROR_RATIO = "NounErrorRatio";
    public static final String FN_O_ERROR_RATIO = "OtherErrorRatio";
    public static final String FN_PP_ERROR_RATIO = "PrepErrorRatio";
    public static final String FN_PR_ERROR_RATIO = "PronErrorRatio";
    public static final String FN_PUNC_ERROR_RATIO = "PuncErrorRatio";
    public static final String FN_V_ERROR_RATIO = "VerbErrorRatio";

    @Override
    public List<IFeature> extract(JCas jcas)
        throws TextClassificationException
    {
        List<IFeature> featList = new ArrayList<IFeature>();

        int nrOfSpellingAnomalies = 0;
        int adjErrors = 0;
        int advErrors = 0;
        int artErrors = 0;
        int cardErrors = 0;
        int conjErrors = 0;
        int nounErrors = 0;
        int otherErrors = 0;
        int prepErrors = 0;
        int pronErrors = 0;
        int puncErrors = 0;
        int verbErrors = 0;

        for (SpellingAnomaly anomaly : JCasUtil.select(jcas, SpellingAnomaly.class)) {
            for (POS pos : JCasUtil.selectCovered(jcas, POS.class, anomaly)) {
                if (pos instanceof ADJ) {
                    adjErrors++;
                }
                else if (pos instanceof ADV) {
                    advErrors++;
                }
                else if (pos instanceof ART) {
                    artErrors++;
                }
                else if (pos instanceof CARD) {
                    cardErrors++;
                }
                else if (pos instanceof CONJ) {
                    conjErrors++;
                }
                else if (pos instanceof N) {
                    nounErrors++;
                }
                else if (pos instanceof O) {
                    otherErrors++;
                }
                else if (pos instanceof PP) {
                    prepErrors++;
                }
                else if (pos instanceof PR) {
                    pronErrors++;
                }
                else if (pos instanceof PUNC) {
                    puncErrors++;
                }
                else if (pos instanceof V) {
                    verbErrors++;
                }
            }

            nrOfSpellingAnomalies++;
        }

        featList.add(new Feature(FN_ADJ_ERROR_RATIO, (double) adjErrors
                / nrOfSpellingAnomalies));
        featList.add(new Feature(FN_ADV_ERROR_RATIO, (double) advErrors
                / nrOfSpellingAnomalies));
        featList.add(new Feature(FN_ART_ERROR_RATIO, (double) artErrors
                / nrOfSpellingAnomalies));
        featList.add(new Feature(FN_CARD_ERROR_RATIO, (double) cardErrors
                / nrOfSpellingAnomalies));
        featList.add(new Feature(FN_CONJ_ERROR_RATIO, (double) conjErrors
                / nrOfSpellingAnomalies));
        featList.add(new Feature(FN_N_ERROR_RATIO, (double) nounErrors
                / nrOfSpellingAnomalies));
        featList.add(new Feature(FN_O_ERROR_RATIO, (double) otherErrors
                / nrOfSpellingAnomalies));
        featList.add(new Feature(FN_PR_ERROR_RATIO, (double) pronErrors
                / nrOfSpellingAnomalies));
        featList.add(new Feature(FN_PP_ERROR_RATIO, (double) prepErrors
                / nrOfSpellingAnomalies));
        featList.add(new Feature(FN_PUNC_ERROR_RATIO, (double) puncErrors
                / nrOfSpellingAnomalies));
        featList.add(new Feature(FN_V_ERROR_RATIO, (double) verbErrors
                / nrOfSpellingAnomalies));

        return featList;
    }
}