package de.tudarmstadt.ukp.dkpro.tc.features.ngram.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneCharSkipNgramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LuceneCharacterSkipNgramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
{
    public static final String LUCENE_CHAR_SKIP_NGRAM_FIELD = "charskipngram";

    public static final String PARAM_CHAR_SKIP_NGRAM_MIN_N = "charSkipNgramMinN";
    @ConfigurationParameter(name = PARAM_CHAR_SKIP_NGRAM_MIN_N, mandatory = true, defaultValue = "2")
    protected int charSkipMinN;

    public static final String PARAM_CHAR_SKIP_NGRAM_MAX_N = "charSkipNgramMaxN";
    @ConfigurationParameter(name = PARAM_CHAR_SKIP_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    protected int charSkipMaxN;

    public static final String PARAM_CHAR_SKIP_SIZE = "charSkipSize";
    @ConfigurationParameter(name = PARAM_CHAR_SKIP_SIZE, mandatory = true, defaultValue = "2")
    protected int charSkipSize;

    public static final String PARAM_CHAR_SKIP_NGRAM_USE_TOP_K = "charSkipNgramUseTopK";
    @ConfigurationParameter(name = PARAM_CHAR_SKIP_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int charSkipNgramUseTopK;

    public static final String PARAM_CHAR_SKIP_NGRAM_LOWER_CASE = "charSkipNgramLowercase";
    @ConfigurationParameter(name = PARAM_CHAR_SKIP_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    protected boolean charSkipToLowerCase;

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LuceneCharSkipNgramMetaCollector.class);

        return metaCollectorClasses;
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_CHAR_SKIP_NGRAM_FIELD;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return LUCENE_CHAR_SKIP_NGRAM_FIELD;
    }

    @Override
    protected int getTopN()
    {
        return charSkipNgramUseTopK;
    }
}