package de.tudarmstadt.ukp.dkpro.tc.features.length;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

/**
 * Extracts the number of tokens per sentence in the classification unit
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class NrOfTokensPerSentenceUFE
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    /**
     * Public name of the feature "number of tokens per sentence" in this classification unit
     */
    public static final String FN_TOKENS_PER_SENTENCE = "NrofTokensPerSentence";

    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
        List<Feature> featList = new ArrayList<Feature>();

        int numTokens = JCasUtil.selectCovered(jcas, Token.class, classificationUnit).size();
        int numSentences = JCasUtil.selectCovered(jcas, Sentence.class, classificationUnit).size();

        double ratio = numTokens / numSentences;

        if (numSentences == 0) {
            featList.add(new Feature(FN_TOKENS_PER_SENTENCE, 0.));
        }
        else {
            featList.add(new Feature(FN_TOKENS_PER_SENTENCE, ratio));
        }
        return featList;
    }
}
