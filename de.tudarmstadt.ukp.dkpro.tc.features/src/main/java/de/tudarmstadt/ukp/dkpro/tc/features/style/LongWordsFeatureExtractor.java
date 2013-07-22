package de.tudarmstadt.ukp.dkpro.tc.features.style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/*
 * Calculates the proportions of tokens that are longer than 5 characters
 * and tokens shorter than 3 characters to all tokens. This property can 
 * be useful for capturing stylistic differences, e.g. in gender recognition.
 * WARNING: Short token ratio includes also all single-character tokens,
 * such as interpunction. 
 */
public class LongWordsFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_LW_RATIO = "LongTokenRatio"; // over 5 chars
    public static final String FN_SW_RATIO = "ShortTokenRatio"; // under 3 chars

    @Override
    public List<Feature> extract(JCas jcas)
    {

        double longTokenRatio = 0.0;
        int longTokenCount = 0;
        double shortTokenRatio = 0.0;
        int shortTokenCount = 0;
        int n = 0;
        for (Token t : JCasUtil.select(jcas, Token.class)) {
            n++;

            String text = t.getCoveredText();
            if (text.length() < 3) {
                shortTokenCount++;
            }
            else if (text.length() > 5) {
                longTokenCount++;
            }
        }
        if (n > 0) {
            longTokenRatio = (double) longTokenCount / n;
            shortTokenRatio = (double) shortTokenCount / n;
        }

        List<Feature> featList = new ArrayList<Feature>();
        featList.addAll(Arrays.asList(new Feature(FN_LW_RATIO, longTokenRatio)));
        featList.addAll(Arrays.asList(new Feature(FN_SW_RATIO, shortTokenRatio)));

        return featList;
    }
}