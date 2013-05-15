package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.initializable.Initializable;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class POSNGramFeatureExtractor
    extends NGramFeatureExtractor
    implements SimpleFeatureExtractor, Initializable
{

    public static final String PARAM_POS_NGRAM_MIN_N = "POSNGramMinSize";
    public static final String PARAM_POS_NGRAM_MAX_N = "POSNGramMaxSize";
    public static final String PARAM_POS_NGRAM_FD_FILE = "POSNGramFDFile";

    @ConfigurationParameter(name = PARAM_POS_NGRAM_FD_FILE, mandatory = true)
    private String fdFile;

    private int minN = 1;
    private int maxN = 3;
    protected Set<String> topKSet;
    private String prefix;

    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws CleartkExtractorException
    {
        List<Feature> features = new ArrayList<Feature>();
        FrequencyDistribution<String> documentPOSNgrams;
        documentPOSNgrams = NGramUtils.getDocumentPOSNgrams(jcas, minN, maxN);

        for (String topNgram : topKSet) {
            if (documentPOSNgrams.getKeys().contains(topNgram)) {
                features.add(new Feature(prefix + "_" + topNgram, 1));
            }
            else {
                features.add(new Feature(prefix + "_" + topNgram, 0));
            }
        }
        return features;
    }

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {

        topKSet = super.getTopNgrams(fdFile);

        prefix = "POS_";

    }
}
