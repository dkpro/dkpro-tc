package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.POSNGramMetaCollector;

public class POSNGramFeatureExtractor
extends FeatureExtractorResource_ImplBase
implements MetaDependent
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
    
    public static final String PARAM_FREQ_THRESHOLD = "FreqThreshold";
    public static final String PARAM_USE_TOP_K = "TopK";

    @ConfigurationParameter(name = PARAM_USE_TOP_K, mandatory = false)
    private int topK = 500;
    @ConfigurationParameter(name = PARAM_FREQ_THRESHOLD, mandatory = false)
    private float freqThreshold = 0.01f;
    
    private boolean useFreqThreshold = false;
    protected Set<String> topKSet;
    protected String prefix;
    private FrequencyDistribution<String> trainingFD;

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

        topKSet = getTopNgrams(fdFile);

        prefix = "POS_";
        
        return true;
    }
    
    
    protected Set<String> getTopNgrams(String s)
            throws ResourceInitializationException
        {
            try {
                trainingFD = new FrequencyDistribution<String>();
                trainingFD.load(new File(s));
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
            catch (ClassNotFoundException e) {
                throw new ResourceInitializationException(e);
            }

            Set<String> topNGrams = new HashSet<String>();

            if (useFreqThreshold) {
                double total = trainingFD.getN();
                double max = 0;
                for (String key : trainingFD.getKeys()) {
                    double freq = trainingFD.getCount(key) / total;
                    max = Math.max(max, freq);
                    if (freq >= freqThreshold) {
                        topNGrams.add(key);
                    }
                }
            }
            else {

                Map<String, Long> map = new HashMap<String, Long>();

                for (String key : trainingFD.getKeys()) {
                    map.put(key, trainingFD.getCount(key));
                }

                Map<String, Long> sorted_map = new TreeMap<String, Long>(new NGramFeatureExtractor().new ValueComparator(map));
                sorted_map.putAll(map);

                int i = 0;
                for (String key : sorted_map.keySet()) {
                    if (i > topK) {
                        break;
                    }
                    topNGrams.add(key);
                    i++;
                }
            }

            System.out.println("+++ TAKING " + topNGrams.size() + " POS NGRAMS");

            return topNGrams;
        }

	@Override
	public List<String> getMetaCollectorClasses() {
        List<String> metaCollectorClasses = new ArrayList<String>();
        metaCollectorClasses.add(POSNGramMetaCollector.class.getName());
        
        return metaCollectorClasses;
	}
}