package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import static de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePhoneticNGramFeatureExtractorBase.LUCENE_PHONETIC_NGRAM_FIELD;

import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
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
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        FrequencyDistribution<String> documentPhoneticNGrams;
		try {
			documentPhoneticNGrams = NGramUtils.getDocumentPhoneticNgrams(jcas,
			        phoneticNgramMinN, phoneticNgramMaxN);
		} catch (TextClassificationException e) {
			throw new AnalysisEngineProcessException(e);
		}

        for (String ngram : documentPhoneticNGrams.getKeys()) {
            addField(jcas, LUCENE_PHONETIC_NGRAM_FIELD, ngram); 
        }
       
        try {
            writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}