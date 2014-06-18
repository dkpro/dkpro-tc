package de.tudarmstadt.ukp.dkpro.tc.features.ngram.base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class FrequencyDistributionNGramFeatureExtractorBase
    extends NGramFeatureExtractorBase
{
    public static final String PARAM_NGRAM_FD_FILE = "ngramFdFile";
    @ConfigurationParameter(name = PARAM_NGRAM_FD_FILE, mandatory = true)
    private String ngramFdFile;

    public static final String FD_NGRAM_FIELD = "ngram";

    private FrequencyDistribution<String> trainingFD;

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(NGramMetaCollector.class);

        return metaCollectorClasses;
    }

    @Override
    protected FrequencyDistribution<String> getTopNgrams()
        throws ResourceInitializationException
    {
        try {
            trainingFD = new FrequencyDistribution<String>();
            trainingFD.load(new File(ngramFdFile));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        FrequencyDistribution<String> topNGrams = new FrequencyDistribution<String>();

        // FIXME - this is a really bad hack, but currently no better FD method to return
        // topK samples each of size n or greater.

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
            topNGrams.addSample(key, trainingFD.getCount(key));
            i++;
        }

        getLogger().log(Level.INFO, "+++ TAKING " + topNGrams.getKeys().size() + " NGRAMS");

        return topNGrams;
    }

    public class ValueComparator
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

    @Override
    protected String getFeaturePrefix()
    {
        return "ngram";
    }

    @Override
    protected String getFieldName()
    {
        return FD_NGRAM_FIELD;
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }
}