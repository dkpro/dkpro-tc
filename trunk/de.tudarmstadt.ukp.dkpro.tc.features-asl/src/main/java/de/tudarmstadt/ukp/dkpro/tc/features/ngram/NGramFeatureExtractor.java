package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NGramFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements MetaDependent, ClassificationUnitFeatureExtractor
{
    public static final String PARAM_NGRAM_MIN_N = "ngramMinN";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N, mandatory = false)
    private int ngramMinN = 1;

    public static final String PARAM_NGRAM_MAX_N = "ngramMaxN";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N, mandatory = false)
    private int ngramMaxN = 3;

    public static final String PARAM_NGRAM_FD_FILE = "ngramFdFile";
    @ConfigurationParameter(name = PARAM_NGRAM_FD_FILE, mandatory = true)
    private String ngramFdFile;

    public static final String PARAM_NGRAM_USE_TOP_K = "ngramUseTopK";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K, mandatory = false)
    private int ngramUseTopK = 500;

    public static final String PARAM_NGRAM_STOPWORDS_FILE = "ngramStopwordsFile";
    @ConfigurationParameter(name = PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    private String ngramStopwordsFile;

    public static final String PARAM_NGRAM_FREQ_THRESHOLD = "ngramFreqThreshold";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD, mandatory = false)
    private float ngramFreqThreshold = 0.01f;

    public static final String PARAM_NGRAM_LOWER_CASE = "ngramLowerCase";
    @ConfigurationParameter(name = PARAM_NGRAM_LOWER_CASE, mandatory = false)
    private boolean ngramLowerCase = true;

    public static final String PARAM_NGRAM_MIN_TOKEN_LENGTH_THRESHOLD = "ngramMinTokenLengthThreshold";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_TOKEN_LENGTH_THRESHOLD, mandatory = false)
    private int ngramMinTokenLengthThreshold = 1;

    private boolean useFreqThreshold = false;
    protected Set<String> topKSet;
    protected String prefix;
    private FrequencyDistribution<String> trainingFD;

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(NGramMetaCollector.class);

        return metaCollectorClasses;
    }

    protected List<Feature> extractFromAnnotation(JCas jcas, Annotation annotation)
    {
        List<Feature> features = new ArrayList<Feature>();
        FrequencyDistribution<String> documentNgrams = null;
        // try to check if stopword list is defined, if so, use it
        if (ngramStopwordsFile != null && !ngramStopwordsFile.isEmpty()) {
            try {
                // each line of the file contains one stopword
                URL stopUrl = ResourceUtils.resolveLocation(ngramStopwordsFile, null);
                InputStream is = stopUrl.openStream();
                Set<String> stopwords = new HashSet<String>();
                stopwords.addAll(IOUtils.readLines(is, "UTF-8"));

                if (annotation == null) {
                    documentNgrams = NGramUtils.getDocumentNgrams(jcas, ngramLowerCase, ngramMinN,
                            ngramMaxN, stopwords);
                }
                else {
                    documentNgrams = NGramUtils.getAnnotationNgrams(jcas, annotation,
                            ngramLowerCase, ngramMinN, ngramMaxN, stopwords);
                }
            }
            catch (Exception e) {
                System.out.println("Problems with file at " + ngramStopwordsFile);
            }
        }
        else {
            if (annotation == null) {
                documentNgrams = NGramUtils.getDocumentNgrams(jcas, ngramLowerCase, ngramMinN,
                        ngramMaxN);
            }
            else {
                documentNgrams = NGramUtils.getAnnotationNgrams(jcas, annotation, ngramLowerCase,
                        ngramMinN, ngramMaxN);
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
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
        return this.extractFromAnnotation(jcas, classificationUnit);
    }

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
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
        return getTopNgrams(ngramFdFile);
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
                if (freq >= ngramFreqThreshold) {
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
                if (i >= ngramUseTopK) {
                    break;
                }

                if (key.length() >= ngramMinTokenLengthThreshold) {
                    topNGrams.add(key);
                    i++;
                }
            }
        }

        getLogger().log(Level.INFO, "+++ TAKING " + topNGrams.size() + " NGRAMS");

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