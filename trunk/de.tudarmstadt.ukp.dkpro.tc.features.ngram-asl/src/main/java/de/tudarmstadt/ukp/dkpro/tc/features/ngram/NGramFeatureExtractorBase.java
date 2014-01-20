package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public abstract class NGramFeatureExtractorBase
    extends FeatureExtractorResource_ImplBase
    implements MetaDependent, ClassificationUnitFeatureExtractor
{
    public static final String PARAM_NGRAM_MIN_N = "ngramMinN";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N, mandatory = false, defaultValue = "1")
    protected int ngramMinN;

    public static final String PARAM_NGRAM_MAX_N = "ngramMaxN";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N, mandatory = false, defaultValue = "3")
    protected int ngramMaxN;

    public static final String PARAM_NGRAM_USE_TOP_K = "ngramUseTopK";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K, mandatory = false, defaultValue = "500")
    protected int ngramUseTopK;

    public static final String PARAM_NGRAM_STOPWORDS_FILE = "ngramStopwordsFile";
    @ConfigurationParameter(name = PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    protected String ngramStopwordsFile;

    public static final String PARAM_NGRAM_FREQ_THRESHOLD = "ngramFreqThreshold";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD, mandatory = false, defaultValue = "0.01")
    protected float ngramFreqThreshold;

    public static final String PARAM_NGRAM_LOWER_CASE = "ngramLowerCase";
    @ConfigurationParameter(name = PARAM_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "true")
    protected boolean ngramLowerCase;

    public static final String PARAM_NGRAM_MIN_TOKEN_LENGTH_THRESHOLD = "ngramMinTokenLengthThreshold";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_TOKEN_LENGTH_THRESHOLD, mandatory = false, defaultValue = "1")
    protected int ngramMinTokenLengthThreshold;

    protected Set<String> stopwords;
    protected Set<String> topKSet;
    protected String prefix;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        
        stopwords = getStopwords();

        topKSet = getTopNgrams();

        prefix = "ngrams_";

        return true;
    }

    protected List<Feature> extractFromAnnotation(JCas jcas, Annotation annotation)
    {
        List<Feature> features = new ArrayList<Feature>();
        FrequencyDistribution<String> documentNgrams = null;
                    
        if (annotation == null) {
            documentNgrams = NGramUtils.getDocumentNgrams(jcas, ngramLowerCase, ngramMinN,
                    ngramMaxN, stopwords);
        }
        else {
            documentNgrams = NGramUtils.getAnnotationNgrams(jcas, annotation,
                    ngramLowerCase, ngramMinN, ngramMaxN, stopwords);
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

    protected abstract Set<String> getTopNgrams()
        throws ResourceInitializationException;
    
    private Set<String> getStopwords()
            throws ResourceInitializationException
    {
        Set<String> stopwords = new HashSet<String>();
        try {
            if (ngramStopwordsFile != null && !ngramStopwordsFile.isEmpty()) {
                    // each line of the file contains one stopword
                    URL stopUrl = ResourceUtils.resolveLocation(ngramStopwordsFile, null);
                    InputStream is = stopUrl.openStream();
                    stopwords.addAll(IOUtils.readLines(is, "UTF-8"));
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        return stopwords;
    }
}