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
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

public class TokenRatioFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String FN_TOKEN_RATIO = "TokenRatio";

    private String token;

    public TokenRatioFeatureExtractor(String token)
    {
        this.token = token;
    }

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {

        double tokenRatio = 0.0;
        int tokenCount = 0;
        int n = 0;
        for (Token t : JCasUtil.select(jcas, Token.class)) {
            n++;

            String text = t.getCoveredText().toLowerCase();
            if (text.equals(token)) {
                tokenCount++;
            }
        }
        if (n > 0) {
            tokenRatio = (double) tokenCount / n;
        }

        List<Feature> featList = new ArrayList<Feature>();
        featList.addAll(Arrays.asList(new Feature(FN_TOKEN_RATIO + "_" + token, tokenRatio)));

        return featList;
    }
}