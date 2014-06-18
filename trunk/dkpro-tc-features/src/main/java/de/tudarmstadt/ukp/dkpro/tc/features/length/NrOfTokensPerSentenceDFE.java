package de.tudarmstadt.ukp.dkpro.tc.features.length;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue.MissingValueNonNominalType;

/**
 * Extracts the ratio of tokens per sentence
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NrOfTokensPerSentenceDFE
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    /**
     * Public name of the feature "number of tokens per sentence"
     */
    public static final String FN_TOKENS_PER_SENTENCE = "NrofTokensPerSentence";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        List<Feature> featList = new ArrayList<Feature>();
        double numTokens = JCasUtil.select(jcas, Token.class).size();
        double numSentences = JCasUtil.select(jcas, Sentence.class).size();

        double ratio = numTokens / numSentences;

        if (numSentences == 0) {
            featList.add(new Feature(FN_TOKENS_PER_SENTENCE, new MissingValue(
                    MissingValueNonNominalType.NUMERIC)));
        }
        else {
            featList.add(new Feature(FN_TOKENS_PER_SENTENCE, ratio));
        }
        return featList;
    }
}
