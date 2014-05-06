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

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NrOfTokensDFE
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{

    public static final String FN_NR_OF_TOKENS = "NrofTokens";
    public static final String FN_TOKENS_PER_SENTENCE = "NrofTokensPerSentence";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        List<Feature> featList = new ArrayList<Feature>();
        int numTokens = JCasUtil.select(jcas, Token.class).size();
        int numSentences = JCasUtil.select(jcas, Sentence.class).size();

        featList.add(new Feature(FN_NR_OF_TOKENS, numTokens));
        if (numSentences > 0) {
            featList.add(new Feature(FN_TOKENS_PER_SENTENCE, (double) numTokens / numSentences));
        }
        return featList;
    }
}
