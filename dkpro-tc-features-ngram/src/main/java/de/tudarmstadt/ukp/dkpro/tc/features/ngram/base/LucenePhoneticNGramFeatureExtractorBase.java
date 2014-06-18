package de.tudarmstadt.ukp.dkpro.tc.features.ngram.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LucenePhoneticNGramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.Token" })
public class LucenePhoneticNGramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
    implements MetaDependent
{
    public static final String LUCENE_PHONETIC_NGRAM_FIELD = "phoneticngram";

    public static final String PARAM_PHONETIC_NGRAM_MIN_N = "phoneticNgramMinN";
    @ConfigurationParameter(name = PARAM_PHONETIC_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	protected int phoneticNgramMinN;

    public static final String PARAM_PHONETIC_NGRAM_MAX_N = "phoneticNgramMaxN";
    @ConfigurationParameter(name = PARAM_PHONETIC_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
	protected int phoneticNgramMaxN;

    public static final String PARAM_PHONETIC_NGRAM_USE_TOP_K = "phoneticNgramUseTopK";
    @ConfigurationParameter(name = PARAM_PHONETIC_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int phoneticNgramUseTopK;

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LucenePhoneticNGramMetaCollector.class);
        
        return metaCollectorClasses;
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_PHONETIC_NGRAM_FIELD;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return LUCENE_PHONETIC_NGRAM_FIELD;
    }

    @Override
    protected int getTopN()
    {
        return phoneticNgramUseTopK;
    }
}