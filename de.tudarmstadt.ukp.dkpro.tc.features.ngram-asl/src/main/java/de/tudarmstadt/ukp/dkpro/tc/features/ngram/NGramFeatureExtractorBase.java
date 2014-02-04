package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public abstract class NGramFeatureExtractorBase
    extends FeatureExtractorResource_ImplBase
    implements MetaDependent, ClassificationUnitFeatureExtractor
{
    public static final String PARAM_NGRAM_MIN_N = "ngramMinN";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    protected int ngramMinN;

    public static final String PARAM_NGRAM_MAX_N = "ngramMaxN";
    @ConfigurationParameter(name = PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    protected int ngramMaxN;

    public static final String PARAM_NGRAM_USE_TOP_K = "ngramUseTopK";
    @ConfigurationParameter(name = PARAM_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int ngramUseTopK;

    public static final String PARAM_NGRAM_STOPWORDS_FILE = "ngramStopwordsFile";
    @ConfigurationParameter(name = PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    protected String ngramStopwordsFile;

    public static final String PARAM_FILTER_PARTIAL_STOPWORD_MATCHES = "filterPartialStopwordMatches";
    @ConfigurationParameter(name = PARAM_FILTER_PARTIAL_STOPWORD_MATCHES, mandatory = true, defaultValue="false")
    protected boolean filterPartialStopwordMatches;

    public static final String PARAM_NGRAM_FREQ_THRESHOLD = "ngramFreqThreshold";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD, mandatory = true, defaultValue = "0.01")
    protected float ngramFreqThreshold;

    public static final String PARAM_NGRAM_LOWER_CASE = "ngramLowerCase";
    @ConfigurationParameter(name = PARAM_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    protected boolean ngramLowerCase;
    /**
     * This is currently the number of characters in the ngram, i.e., ngram.length(), 
     * including a token-separating character between each token.
     */
    public static final String PARAM_NGRAM_MIN_TOKEN_LENGTH_THRESHOLD = "ngramMinTokenLengthThreshold";
    @ConfigurationParameter(name = PARAM_NGRAM_MIN_TOKEN_LENGTH_THRESHOLD, mandatory = true, defaultValue = "1")
    protected int ngramMinTokenLengthThreshold;

    public Set<String> stopwords;
    public FrequencyDistribution<String> topKSet;
    protected String prefix;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        
        try {
            stopwords = FeatureUtil.getStopwords(ngramStopwordsFile, ngramLowerCase);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        topKSet = getTopNgrams();

        prefix = getFeaturePrefix();

        return true;
    }

    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {
        List<Feature> features = new ArrayList<Feature>();
        FrequencyDistribution<String> documentNgrams = null;
                    
        if (classificationUnit == null) {
            documentNgrams = getDocumentNgrams(jcas);
        }
        else {
            documentNgrams = getAnnotationNgrams(jcas, classificationUnit);
        }
         
        for (String topNgram : topKSet.getKeys()) {
            if (documentNgrams.getKeys().contains(topNgram)) {
                features.add(new Feature(prefix + "_" + topNgram, 1));
            }
            else {
                features.add(new Feature(prefix + "_" + topNgram, 0));
            }
        }

        return features;
    }

    protected abstract FrequencyDistribution<String> getTopNgrams()
        throws ResourceInitializationException;
    
    protected abstract String getFeaturePrefix();
    
    protected abstract FrequencyDistribution<String> getDocumentNgrams(JCas jcas);
    
    protected abstract FrequencyDistribution<String> getAnnotationNgrams(JCas jcas, Annotation anno);
}