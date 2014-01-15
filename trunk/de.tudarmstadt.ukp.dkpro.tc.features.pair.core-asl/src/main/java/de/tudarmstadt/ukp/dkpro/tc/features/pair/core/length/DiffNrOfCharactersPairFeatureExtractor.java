package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeature;

/**
 * Pair-wise feature extractor Computes the number of characters in a view and returns the
 * difference of both views.
 * 
 * @author nico.erbs@gmail.com
 * 
 */
public class DiffNrOfCharactersPairFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        return Arrays.<Feature>asList(
                new SimpleFeature("DiffNrOfCharacters",
                        view1.getDocumentText().length() -
                                view2.getDocumentText().length())
                );

    }
}
