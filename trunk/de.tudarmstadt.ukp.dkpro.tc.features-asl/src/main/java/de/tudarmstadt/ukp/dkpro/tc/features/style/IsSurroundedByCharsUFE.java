package de.tudarmstadt.ukp.dkpro.tc.features.style;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

/**
 * Extracts information about whether the classification unit is situated between two characters
 * (left and right boundary tokens).
 * 
 * @author Andriy Nadolskyy
 * @author daxenberger
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class IsSurroundedByCharsUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    /**
     * A String consisting of two characters by which the classification unit is surrounded to the
     * left (position 0 in the string) and right (position 1), e.g. quotation marks, or opening and
     * closing brackets"
     */
    public static final String PARAM_SURROUNDING_CHARS = "surroundingChars";
    @ConfigurationParameter(name = PARAM_SURROUNDING_CHARS, mandatory = true, description = "A String consisting of two characters"
            + " by which the classification unit is surrounded to the left (position 0 in the string) and right (position 1)"
            + ", e.g. quotation marks, or opening and closing brackets")
    private String surroundingChars;

    /**
     * Public name of the feature "Between two tokens"
     */
    public static final String SURROUNDED_BY_CHARS = "SurroundedByChars";

    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
        List<Feature> featList = new ArrayList<Feature>();
        int maxTokensCount = JCasUtil.select(jcas, Token.class).size();

        // error conditions
        if (surroundingChars.length() != 2) {
            throw new TextClassificationException(
                    "Please initialize PARAM_SURROUNDING_CHARS correctly.");
        }

        char leftBoundary = surroundingChars.charAt(0);
        char rightBoundary = surroundingChars.charAt(1);

        for (int j = 1; j < maxTokensCount; j++) {
            List<Token> precedingTokens = JCasUtil.selectPreceding(jcas, Token.class,
                    classificationUnit,
                    j);
            List<Token> followingTokens = JCasUtil.selectFollowing(jcas, Token.class,
                    classificationUnit,
                    j);

            // please beware that, if boundaries are equal, this method is not fool-proof
            // e.g. for cases such as : --" it " is a " test "--
            // we will silently ignore this here
            if (precedingTokens.size() > 0 && followingTokens.size() > 0) {
                Token p = precedingTokens.get(precedingTokens.size() - 1);
                Token f = followingTokens.get(0);
                if (p.getCoveredText().equals(String.valueOf(leftBoundary))
                        && f.getCoveredText().equals(String.valueOf(rightBoundary))) {
                    featList.add(new Feature(SURROUNDED_BY_CHARS, true));
                    return featList;
                }
            }
        }

        featList.add(new Feature(SURROUNDED_BY_CHARS, false));
        return featList;
    }
}