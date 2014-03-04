package de.tudarmstadt.ukp.dkpro.tc.features.ngram.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LucenePOSNGramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.POSNGramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class LucenePOSNGramFeatureExtractorBase
    extends LuceneFeatureExtractorBase
    implements MetaDependent
{
    public static final String LUCENE_POS_NGRAM_FIELD = "posngram";

    public static final String PARAM_POS_NGRAM_MIN_N = "posNgramMinN";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	protected int posNgramMinN;

    public static final String PARAM_POS_NGRAM_MAX_N = "posNgramMaxN";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
	protected int posNgramMaxN;

    public static final String PARAM_POS_NGRAM_USE_TOP_K = "posNgramUseTopK";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    protected int posNgramUseTopK;

    public static final String PARAM_USE_CANONICAL_POS = "useCanonicalPos";
    @ConfigurationParameter(name = PARAM_USE_CANONICAL_POS, mandatory = true, defaultValue = "true")
    protected boolean useCanonicalTags;

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LucenePOSNGramMetaCollector.class);
        metaCollectorClasses.add(POSNGramMetaCollector.class);

        
        return metaCollectorClasses;
    }

    @Override
    protected String getFieldName()
    {
        return LUCENE_POS_NGRAM_FIELD;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "posngram";
    }

    @Override
    protected int getTopN()
    {
        return posNgramUseTopK;
    }
}