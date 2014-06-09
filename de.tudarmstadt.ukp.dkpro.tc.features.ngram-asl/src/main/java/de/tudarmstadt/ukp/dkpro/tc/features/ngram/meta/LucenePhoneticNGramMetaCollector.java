package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import static de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePOSNGramFeatureExtractorBase.LUCENE_POS_NGRAM_FIELD;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePhoneticNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;

public class LucenePhoneticNGramMetaCollector
    extends LuceneBasedMetaCollector
{

    @ConfigurationParameter(name = LucenePhoneticNGramFeatureExtractorBase.PARAM_PHONETIC_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int phoneticNgramMinN;

    @ConfigurationParameter(name = LucenePhoneticNGramFeatureExtractorBase.PARAM_PHONETIC_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int phoneticNgramMaxN;
    
    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas) 
            throws TextClassificationException{
        return NGramUtils.getDocumentPhoneticNgrams(jcas,
                phoneticNgramMinN, phoneticNgramMaxN);
    }
    
    @Override
    protected String getFieldName(){
        return LUCENE_POS_NGRAM_FIELD;
    }
}