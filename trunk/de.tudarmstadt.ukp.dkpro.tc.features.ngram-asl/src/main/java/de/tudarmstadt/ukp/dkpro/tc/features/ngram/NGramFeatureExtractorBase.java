package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public abstract class NGramFeatureExtractorBase
    extends FeatureExtractorResource_ImplBase
    implements MetaDependent
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
    @ConfigurationParameter(name = PARAM_FILTER_PARTIAL_STOPWORD_MATCHES, mandatory = true, defaultValue = "false")
    protected boolean filterPartialStopwordMatches;

    public static final String PARAM_NGRAM_FREQ_THRESHOLD = "ngramFreqThreshold";
    @ConfigurationParameter(name = PARAM_NGRAM_FREQ_THRESHOLD, mandatory = true, defaultValue = "0.01")
    protected float ngramFreqThreshold;

    public static final String PARAM_NGRAM_LOWER_CASE = "ngramLowerCase";
    @ConfigurationParameter(name = PARAM_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    protected boolean ngramLowerCase;

    protected Set<String> stopwords;
    protected FrequencyDistribution<String> topKSet;
    protected String prefix;

    /**
     * @return Name of the field
     */
    protected abstract String getFieldName();

    /**
     * @return Prefix which will be used to name to feature
     */
    protected abstract String getFeaturePrefix();

    /**
     * @return How many of the most frequent ngrams should be returned.
     */
    protected abstract int getTopN();

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

    protected abstract FrequencyDistribution<String> getTopNgrams()
        throws ResourceInitializationException;
}