package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

public class POSNGramFeatureExtractor
    extends NGramFeatureExtractor
{

    public static final String PARAM_POS_NGRAM_FD_FILE = "POSNGramFDFile";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_FD_FILE, mandatory = true)
    private String fdFile;
    
    public static final String PARAM_POS_NGRAM_MIN_N = "POSNGramMinSize";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_MIN_N, mandatory = false, defaultValue="1")
    private int minN;

    public static final String PARAM_POS_NGRAM_MAX_N = "POSNGramMaxSize";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_MAX_N, mandatory = false, defaultValue="3")
    private int maxN;
    
    protected Set<String> topKSet;
    private String prefix;

    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws TextClassificationException
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
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        topKSet = super.getTopNgrams(fdFile);

        prefix = "POS_";
        
        return true;
    }
}