package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LuceneCharacterNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;

public class LuceneCharacterNGramMetaCollector
    extends LuceneBasedMetaCollector
{

    @ConfigurationParameter(name = LuceneCharacterNGramFeatureExtractorBase.PARAM_CHAR_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int charNgramMinN;

    @ConfigurationParameter(name = LuceneCharacterNGramFeatureExtractorBase.PARAM_CHAR_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int charNgramMaxN;
    
    @ConfigurationParameter(name = LuceneCharacterNGramFeatureExtractorBase.PARAM_CHAR_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "false")
    private boolean lowerCase;
    
    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas){
        return NGramUtils.getDocumentCharacterNgrams(jcas, lowerCase,
                charNgramMinN, charNgramMaxN);
    }
    
    @Override
    protected String getFieldName(){
        return LuceneCharacterNGramFeatureExtractorBase.LUCENE_CHAR_NGRAM_FIELD;
    }
}