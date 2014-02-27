package de.tudarmstadt.ukp.dkpro.tc.features.ngram.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneSkipNgramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LuceneSkipNgramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
{
    public static final String LUCENE_SKIP_NGRAM_FIELD = "skipngram";

    public static final String PARAM_SKIP_NGRAM_MIN_N = "skipNgramMinN";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_MIN_N, mandatory = true, defaultValue = "2")
    protected int skipMinN;

    public static final String PARAM_SKIP_NGRAM_MAX_N = "skipNgramMaxN";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    protected int skipMaxN;

    public static final String PARAM_SKIP_SIZE = "skipSize";
    @ConfigurationParameter(name = PARAM_SKIP_SIZE, mandatory = true, defaultValue = "2")
    protected int skipSize;

    public static final String PARAM_SKIP_NGRAM_USE_TOP_K = "skipNgramUseTopK";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int skipNgramUseTopK;

    public static final String PARAM_SKIP_NGRAM_LOWER_CASE = "skipNgramLowercase";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    protected boolean skipToLowerCase;

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LuceneSkipNgramMetaCollector.class);

        return metaCollectorClasses;
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_SKIP_NGRAM_FIELD;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "skipngram";
    }

    @Override
    protected int getTopN()
    {
        return skipNgramUseTopK;
    }
}