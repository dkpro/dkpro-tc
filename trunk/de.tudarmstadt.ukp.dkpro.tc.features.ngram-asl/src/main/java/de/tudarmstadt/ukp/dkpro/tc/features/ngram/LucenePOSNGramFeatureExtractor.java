package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.POSNGramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class LucenePOSNGramFeatureExtractor
    extends LuceneFeatureExtractorBase
    implements MetaDependent
{
    public static final String LUCENE_POS_NGRAM_FIELD = "posngram";

    public static final String PARAM_POS_NGRAM_MIN_N = "posNgramMinN";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int posNgramMinN;

    public static final String PARAM_POS_NGRAM_MAX_N = "posNgramMaxN";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int posNgramMaxN;

    public static final String PARAM_POS_NGRAM_USE_TOP_K = "posNgramUseTopK";
    @ConfigurationParameter(name = PARAM_POS_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    private int posNgramUseTopK;

    public static final String PARAM_USE_CANONICAL_POS = "useCanonicalPos";
    @ConfigurationParameter(name = PARAM_USE_CANONICAL_POS, mandatory = true, defaultValue = "true")
    private boolean useCanonicalTags;

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
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
    protected FrequencyDistribution<String> getDocumentNgrams(JCas jcas)
    {
        return NGramUtils.getDocumentPosNgrams(jcas, posNgramMinN, posNgramMaxN, useCanonicalTags);
    }

    @Override
    protected FrequencyDistribution<String> getAnnotationNgrams(JCas jcas, Annotation anno)
    {
        return NGramUtils.getAnnotationPosNgrams(jcas, anno, posNgramMinN, posNgramMaxN, useCanonicalTags);
    }

    @Override
    protected int getTopN()
    {
        return posNgramUseTopK;
    }
}