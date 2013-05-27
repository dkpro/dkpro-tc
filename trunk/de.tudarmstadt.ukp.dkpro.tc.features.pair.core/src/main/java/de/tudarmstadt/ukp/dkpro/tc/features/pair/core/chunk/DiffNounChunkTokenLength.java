package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.chunk;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureExtractor;

public class DiffNounChunkTokenLength
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        return Arrays
                .asList(new Feature(
                        "DiffNounPhraseTokenLength",
                        getAverageNounPhraseTokenLength(view1)
                                - getAverageNounPhraseTokenLength(view2)));

    }

    private double getAverageNounPhraseTokenLength(JCas view)
    {
        int totalNumber = 0;
        for (Chunk chunk : JCasUtil.select(view, Chunk.class)) {
            totalNumber += JCasUtil.selectCovered(view, Token.class, chunk).size();
        }
        return totalNumber / (double) JCasUtil.select(view, Chunk.class).size();
    }
}
