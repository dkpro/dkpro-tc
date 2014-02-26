package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneSkipNgramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramUtils;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LuceneSkipNgramFeatureExtractor
    extends LuceneFeatureExtractorBase
{
    public static final String LUCENE_SKIP_NGRAM_FIELD = "skipngram";

    public static final String PARAM_SKIP_NGRAM_MIN_N = "skipNgramMinN";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_MIN_N, mandatory = true, defaultValue = "2")
    private int skipMinN;

    public static final String PARAM_SKIP_NGRAM_MAX_N = "skipNgramMaxN";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int skipMaxN;

    public static final String PARAM_SKIP_SIZE = "skipSize";
    @ConfigurationParameter(name = PARAM_SKIP_SIZE, mandatory = true, defaultValue = "2")
    private int skipSize;

    public static final String PARAM_SKIP_NGRAM_USE_TOP_K = "skipNgramUseTopK";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_USE_TOP_K, mandatory = true, defaultValue = "500")
    private int skipNgramUseTopK;

    public static final String PARAM_SKIP_NGRAM_LOWER_CASE = "skipNgramLowercase";
    @ConfigurationParameter(name = PARAM_SKIP_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    private boolean skipToLowerCase;
        
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
    protected FrequencyDistribution<String> getDocumentNgrams(JCas jcas)
    {
        return NGramUtils.getDocumentSkipNgrams(jcas, skipToLowerCase, filterPartialStopwordMatches, skipMinN, skipMaxN, skipSize, stopwords);
    }

    // FIXME didn't implement, as I think this should be removed anyway
    @Override
    protected FrequencyDistribution<String> getAnnotationNgrams(JCas jcas, Annotation anno)
    {
        return null;
    }

    @Override
    protected int getTopN()
    {
        return skipNgramUseTopK;
    }
}