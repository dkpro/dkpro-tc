package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
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
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
    	initializeDocument(jcas);
    	
        FrequencyDistribution<String> documentCharNGrams = NGramUtils.getDocumentCharacterNgrams(jcas, lowerCase,
                charNgramMinN, charNgramMaxN);

        for (String ngram : documentCharNGrams.getKeys()) {
            if (lowerCase) {
                ngram = ngram.toLowerCase();
            }
            
			addField(
			    jcas,
			    LuceneCharacterNGramFeatureExtractorBase.LUCENE_CHAR_NGRAM_FIELD,
			    ngram
			);
			
        }
       
        try {
            writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}