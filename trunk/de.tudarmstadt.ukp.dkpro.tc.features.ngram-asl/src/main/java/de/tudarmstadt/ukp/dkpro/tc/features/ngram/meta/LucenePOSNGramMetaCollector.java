package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import static de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePOSNGramFeatureExtractorBase.LUCENE_POS_NGRAM_FIELD;

import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePOSNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;

public class LucenePOSNGramMetaCollector
    extends LuceneBasedMetaCollector
{
    @ConfigurationParameter(name = LucenePOSNGramFeatureExtractorBase.PARAM_POS_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int posNgramMinN;

    @ConfigurationParameter(name = LucenePOSNGramFeatureExtractorBase.PARAM_POS_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int posNgramMaxN;

    @ConfigurationParameter(name = LucenePOSNGramFeatureExtractorBase.PARAM_USE_CANONICAL_POS, mandatory = true, defaultValue = "true")
    private boolean useCanonical;

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        FrequencyDistribution<String> documentPOSNGrams = NGramUtils.getDocumentPosNgrams(jcas,
                posNgramMinN, posNgramMaxN, useCanonical);

        for (String ngram : documentPOSNGrams.getKeys()) {
            addField(jcas, LUCENE_POS_NGRAM_FIELD, ngram); 
        }
       
        try {
            writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}