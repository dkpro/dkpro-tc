package de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import dkpro.similarity.algorithms.api.SimilarityException;
import dkpro.similarity.algorithms.lexical.string.GreedyStringTiling;

public class GreedyStringTilingFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    protected GreedyStringTiling measure = new GreedyStringTiling(3);

    @Override
    public List<IFeature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {

        try {
            double similarity = measure.getSimilarity(view1.getDocumentText(),
                    view2.getDocumentText());

            return Arrays.<IFeature>asList(new Feature("Similarity" + measure.getName(), similarity));
        }
        catch (SimilarityException e) {
            throw new TextClassificationException(e);
        }
    }
}