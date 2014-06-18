package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import static de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePOSNGramFeatureExtractorBase.LUCENE_POS_NGRAM_FIELD;

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
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas){
        return NGramUtils.getDocumentPosNgrams(jcas,
                posNgramMinN, posNgramMaxN, useCanonical);
    }
    
    @Override
    protected String getFieldName(){
        return LUCENE_POS_NGRAM_FIELD;
    }

//    @Override
//    public void process(JCas jcas)
//        throws AnalysisEngineProcessException
//    {
//    	initializeDocument(jcas);
//    	
//        FrequencyDistribution<String> documentPOSNGrams = getNgramsFD(jcas);
//
//        for (String ngram : documentPOSNGrams.getKeys()) {
//			addField(jcas, getFieldName(), ngram);// under discussion: binary records per doc
//        }
//       
//        try {
//            writeToIndex();
//        }
//        catch (IOException e) {
//            throw new AnalysisEngineProcessException(e);
//        }
//    }
}