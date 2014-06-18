package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.chunk;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;

/**
 * Pair-wise feature extractor Computes the average token length of all noun chunks in a view and
 * returns the difference of both views.
 * 
 * @author nico.erbs@gmail.com
 * 
 */
public class DiffNounChunkTokenLength
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        return Arrays.asList(new Feature(
                "DiffNounPhraseTokenLength",
                getAverageNounPhraseTokenLength(view1) - getAverageNounPhraseTokenLength(view2)));

    }

    /**
     * Returns average token length of chunks in a view
     * 
     * @param view
     *            the view of the JCas
     * @return average token length of all chunks
     */
    private double getAverageNounPhraseTokenLength(JCas view)
    {
        int totalNumber = 0;
        for (Chunk chunk : JCasUtil.select(view, Chunk.class)) {
            totalNumber += JCasUtil.selectCovered(view, Token.class, chunk).size();
        }
        return totalNumber / (double) JCasUtil.select(view, Chunk.class).size();
    }
}
