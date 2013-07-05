package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.chunk;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

/**
 * Pair-wise feature extractor
 * Computes the average character lenght of all noun chunks in a view and reuturns the difference of both views.
 * @author erbs
 *
 */
public class DiffNounChunkCharacterLength
    extends PairFeatureExtractorResource_ImplBase
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {

        return Arrays.asList(
                new Feature("DiffNounPhraseCharacterLength",
                        getAverageNounPhraseCharacterLength(view1)
                                - getAverageNounPhraseCharacterLength(view2))
                );

    }

    private double getAverageNounPhraseCharacterLength(JCas view)
    {
        int totalNumber = 0;
        for (Chunk chunk : JCasUtil.select(view, Chunk.class)) {
            totalNumber += chunk.getCoveredText().length();
        }
        return totalNumber / (double) JCasUtil.select(view, Chunk.class).size();
    }
}
