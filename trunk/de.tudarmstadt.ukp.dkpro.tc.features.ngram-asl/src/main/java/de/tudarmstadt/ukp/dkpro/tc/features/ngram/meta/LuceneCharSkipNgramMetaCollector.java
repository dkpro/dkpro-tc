package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LuceneCharacterSkipNgramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;

public class LuceneCharSkipNgramMetaCollector
    extends LuceneBasedMetaCollector
{    
    @ConfigurationParameter(name = LuceneCharacterSkipNgramFeatureExtractorBase.PARAM_CHAR_SKIP_NGRAM_MIN_N, mandatory = true, defaultValue = "2")
    private int minN;

    @ConfigurationParameter(name = LuceneCharacterSkipNgramFeatureExtractorBase.PARAM_CHAR_SKIP_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int maxN;
    
    @ConfigurationParameter(name = LuceneCharacterSkipNgramFeatureExtractorBase.PARAM_CHAR_SKIP_SIZE, mandatory = true, defaultValue = "2")
    private int skipSize;
    
    @ConfigurationParameter(name = LuceneCharacterSkipNgramFeatureExtractorBase.PARAM_CHAR_SKIP_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    private boolean ngramLowerCase;

    
    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas){
        return NGramUtils.getCharacterSkipNgrams(
                jcas, ngramLowerCase, minN, maxN, skipSize);
    }
    
    @Override
    protected String getFieldName(){
        return LuceneCharacterSkipNgramFeatureExtractorBase.LUCENE_CHAR_SKIP_NGRAM_FIELD;
    }
}