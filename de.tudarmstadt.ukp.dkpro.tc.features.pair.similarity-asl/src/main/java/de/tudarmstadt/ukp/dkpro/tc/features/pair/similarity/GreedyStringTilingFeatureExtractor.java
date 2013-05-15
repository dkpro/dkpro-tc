package de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;

import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureExtractor;
import de.tudarmstadt.ukp.similarity.algorithms.api.SimilarityException;
import de.tudarmstadt.ukp.similarity.algorithms.lexical.string.GreedyStringTiling;

public class GreedyStringTilingFeatureExtractor
    implements PairFeatureExtractor
{

    protected GreedyStringTiling measure = new GreedyStringTiling(3);

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws CleartkExtractorException
    {

        try {
            double similarity = measure.getSimilarity(view1.getDocumentText(), view2.getDocumentText());

            return Arrays.asList(new Feature("Similarity" + measure.getName(), similarity));
        }
        catch (SimilarityException e) {
            throw new CleartkExtractorException(e);
        }
    }
}