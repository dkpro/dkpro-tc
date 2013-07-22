package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FocusAnnotationFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramMetaCollector;

public class NGramFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements MetaDependent, FocusAnnotationFeatureExtractor
{

    public static final String PARAM_NGRAM_MIN_N = "NGramMinSize";
    public static final String PARAM_NGRAM_MAX_N = "NGramMaxSize";
    public static final String PARAM_NGRAM_FD_FILE = "NGramFDFile";
    public static final String PARAM_STOPWORDS_FILE = "StopwordsFile";
    public static final String PARAM_FREQ_THRESHOLD = "FreqThreshold";
    public static final String PARAM_USE_TOP_K = "TopK";
    public static final String PARAM_LOWER_CASE = "LowerCaseNGrams";
    public static final String PARAM_MIN_TOKEN_LENGTH_THRESHOLD = "MinTokenLengthThreshold";

    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N, mandatory = false)
    private int minN = 1;
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N, mandatory = false)
    private int maxN = 3;
    @ConfigurationParameter(name = PARAM_NGRAM_FD_FILE, mandatory = true)
    private String fdFile;
    @ConfigurationParameter(name = PARAM_USE_TOP_K, mandatory = false)
    private int topK = 500;
    @ConfigurationParameter(name = PARAM_STOPWORDS_FILE, mandatory = false)
    private String stopFile;
    @ConfigurationParameter(name = PARAM_FREQ_THRESHOLD, mandatory = false)
    private float freqThreshold = 0.01f;
    @ConfigurationParameter(name = PARAM_LOWER_CASE, mandatory = false)
    private boolean lowerCaseNGrams = true;
    @ConfigurationParameter(name = PARAM_MIN_TOKEN_LENGTH_THRESHOLD, mandatory = false)
    private int minTokenLengthThreshold = 1;

    private boolean useFreqThreshold = false;
    protected Set<String> topKSet;
    protected String prefix;
    private FrequencyDistribution<String> trainingFD;

    @Override
    public List<String> getMetaCollectorClasses()
    {
        List<String> metaCollectorClasses = new ArrayList<String>();
        metaCollectorClasses.add(NGramMetaCollector.class.getName());

        return metaCollectorClasses;
    }

    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws TextClassificationException
    {
        List<Feature> features = new ArrayList<Feature>();
        FrequencyDistribution<String> documentNgrams = null;
        // try to check if stopword list is defined, if so, use it
        if (stopFile != null && !stopFile.isEmpty()) {
            try {
                File stopwordsFile = new File(stopFile);
                // each line of the file contains one stopword
                Set<String> stopwords = new HashSet<String>();
                stopwords.addAll(FileUtils.readLines(stopwordsFile,
                        "UTF_8"));
                if (focusAnnotation == null) {
                    documentNgrams = NGramUtils.getDocumentNgrams(jcas, lowerCaseNGrams, minN,
                            maxN, stopwords);
                }
                else {
                    documentNgrams = NGramUtils.getAnnotationNgrams(jcas, focusAnnotation,
                            lowerCaseNGrams, minN, maxN, stopwords);
                }
            }
            catch (Exception e) {
                System.out.println("Problems with file at " + stopFile);
            }
        }
        else {
            if (focusAnnotation == null) {
                documentNgrams = NGramUtils.getDocumentNgrams(jcas, lowerCaseNGrams, minN, maxN);
            }
            else {
                documentNgrams = NGramUtils.getAnnotationNgrams(jcas, focusAnnotation,
                        lowerCaseNGrams, minN, maxN);
            }
        }
        for (String topNgram : topKSet) {
            if (documentNgrams.getKeys().contains(topNgram)) {
                // features.add(new Feature(prefix + "_" + topNgram,
                // documentNgrams.getCount(topNgram)));
                features.add(new Feature(prefix + "_" + topNgram, 1));
            }
            else {
                features.add(new Feature(prefix + "_" + topNgram, 0));
            }
        }

        // documentNgrams.clear();

        return features;
    }

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        topKSet = getTopNgrams();

        prefix = "ngrams_";

        return true;
    }

    private Set<String> getTopNgrams()
        throws ResourceInitializationException
    {
        return getTopNgrams(fdFile);
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

            // FIXME - maybe something better should directly go into FrequencyDistribution
            // FIXME - this is a really bad hack

            Map<String, Long> map = new HashMap<String, Long>();

            for (String key : trainingFD.getKeys()) {
                map.put(key, trainingFD.getCount(key));
            }

            Map<String, Long> sorted_map = new TreeMap<String, Long>(new ValueComparator(map));
            sorted_map.putAll(map);

            int i = 0;
            for (String key : sorted_map.keySet()) {
                if (i > topK) {
                    break;
                }

                if (key.length() >= minTokenLengthThreshold) {
                    topNGrams.add(key);
                    i++;
                }
            }
        }

        System.out.println("+++ TAKING " + topNGrams.size() + " NGRAMS");

        return topNGrams;
    }

    class ValueComparator
        implements Comparator<String>
    {

        Map<String, Long> base;

        public ValueComparator(Map<String, Long> base)
        {
            this.base = base;
        }

        @Override
        public int compare(String a, String b)
        {

            if (base.get(a) < base.get(b)) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }
}