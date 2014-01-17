package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.chunk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

/**
 * Pair-wise feature extractor. Computes how many noun chunks two views share.
 * 
 * @author nico.erbs@gmail.com
 * 
 */
public class SharedNounChunks
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    protected boolean normalizeWithFirst;

    public SharedNounChunks(boolean normalizeWithFirst)
    {
        this.normalizeWithFirst = normalizeWithFirst;
    }

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {

        if (normalizeWithFirst) {
            return Arrays.asList(
                    new Feature("SharedNounChunkView1", getSharedNounChunksCount(view1, view2))
                    );
        }
        else {
            return Arrays.asList(
                    new Feature("SharedNounChunkView2", getSharedNounChunksCount(view2, view1))
                    );
        }

    }

    /**
     * Computes the ratio of shared nouns
     * 
     * @param view1
     *            First view to be processed
     * @param view2
     *            Second view to be processed
     * @return The quotient of shared noun chunks in both views and noun chunks in the first view
     */
    private double getSharedNounChunksCount(JCas view1, JCas view2)
    {

        Set<String> chunks1 = new HashSet<String>();
        for (Chunk chunk : JCasUtil.select(view1, Chunk.class)) {
            chunks1.add(chunk.getCoveredText());
        }
        Set<String> chunks2 = new HashSet<String>();
        for (Chunk chunk : JCasUtil.select(view2, Chunk.class)) {
            chunks2.add(chunk.getCoveredText());
        }
        chunks1.retainAll(chunks2);
        return chunks1.size() / (double) JCasUtil.select(view1, Chunk.class).size();
    }

}
