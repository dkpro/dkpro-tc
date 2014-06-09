package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
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
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
    	initializeDocument(jcas);
    	
        FrequencyDistribution<String> charNGrams = NGramUtils.getCharacterSkipNgrams(
                jcas, ngramLowerCase, minN, maxN, skipSize);

        for (String ngram : charNGrams.getKeys()) {
			addField(jcas, LuceneCharacterSkipNgramFeatureExtractorBase.LUCENE_CHAR_SKIP_NGRAM_FIELD, ngram);
        }
       
        try {
            writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}