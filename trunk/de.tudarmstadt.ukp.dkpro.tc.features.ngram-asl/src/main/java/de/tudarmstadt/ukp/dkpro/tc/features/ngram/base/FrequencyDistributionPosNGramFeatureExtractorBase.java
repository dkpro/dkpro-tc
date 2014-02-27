package de.tudarmstadt.ukp.dkpro.tc.features.ngram.base;

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
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.POSNGramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class FrequencyDistributionPosNGramFeatureExtractorBase
    extends FeatureExtractorResource_ImplBase
    implements MetaDependent
{

    public static final String PARAM_POS_NGRAM_FD_FILE = "posNgramFdFile";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_FD_FILE, mandatory = true)
    private String posNgramFdFile;

    public static final String PARAM_POS_NGRAM_MIN_N = "posNgramMinN";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int posNgramMinN;

    public static final String PARAM_POS_NGRAM_MAX_N = "posNgramMaxN";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int posNgramMaxN;

    public static final String PARAM_POS_NGRAM_USE_TOP_K = "posNgramUseTopK";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    private int posNgramUseTopK;

    public static final String PARAM_POS_NGRAM_FREQ_THRESHOLD = "posNgramFreqThreshold";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_FREQ_THRESHOLD, mandatory = true, defaultValue = "0.01f")
    private float posNgramFreqThreshold;

    public static final String PARAM_USE_CANONICAL_POS = "useCanonicalPos";
    @ConfigurationParameter(name = PARAM_USE_CANONICAL_POS, mandatory = true, defaultValue = "true")
    private boolean useCanonicalTags;

    protected Set<String> topKSet;
    protected String prefix;
    private FrequencyDistribution<String> trainingFD;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        topKSet = getTopNgrams(posNgramFdFile);

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

        Map<String, Long> map = new HashMap<String, Long>();

        for (String key : trainingFD.getKeys()) {
            map.put(key, trainingFD.getCount(key));
        }

        Map<String, Long> sorted_map = new TreeMap<String, Long>(
                new FrequencyDistributionNGramFeatureExtractorBase().new ValueComparator(map));
        sorted_map.putAll(map);

        int i = 0;
        for (String key : sorted_map.keySet()) {
            if (i >= posNgramUseTopK) {
                break;
            }
            topNGrams.add(key);
            i++;
        }

        getLogger().log(Level.INFO, "+++ TAKING " + topNGrams.size() + " POS NGRAMS");

        return topNGrams;
    }

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(POSNGramMetaCollector.class);

        return metaCollectorClasses;
    }
}