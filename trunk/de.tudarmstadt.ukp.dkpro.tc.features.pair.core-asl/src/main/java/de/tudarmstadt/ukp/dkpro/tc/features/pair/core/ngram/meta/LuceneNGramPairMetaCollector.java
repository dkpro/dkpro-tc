package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.ComboUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.LuceneNGramPairFeatureExtractor;

public class LuceneNGramPairMetaCollector
	extends LuceneBasedMetaCollector
{

    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW1, mandatory = true, defaultValue = "1")
    private int ngramView1MinN;

    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW2, mandatory = true, defaultValue = "1")
    private int ngramView2MinN;
    
    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW1, mandatory = true, defaultValue = "3")
    private int ngramView1MaxN;
    
    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW2, mandatory = true, defaultValue = "3")
    private int ngramView2MaxN;

    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;

    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    private String ngramStopwordsFile;

    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_FILTER_PARTIAL_STOPWORD_MATCHES, mandatory = true, defaultValue="false")
    private boolean filterPartialStopwordMatches;

    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "true")
    private boolean ngramLowerCase;
	

    private Set<String> stopwords;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        try {
            stopwords = FeatureUtil.getStopwords(ngramStopwordsFile, ngramLowerCase);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
    	JCas view1;
    	JCas view2;
    	try{
			view1 = jcas.getView(AbstractPairReader.PART_ONE);
			view2 = jcas.getView(AbstractPairReader.PART_TWO);
    	}
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    	List<JCas> jcases = new ArrayList<JCas>();
		jcases.add(view1);
		jcases.add(view2);
        FrequencyDistribution<String> view1NGrams = NGramUtils.getDocumentNgrams(
                view1, ngramLowerCase, filterPartialStopwordMatches, ngramView1MinN, ngramView1MaxN, stopwords);
        FrequencyDistribution<String> view2NGrams = NGramUtils.getDocumentNgrams(
                view2, ngramLowerCase, filterPartialStopwordMatches, ngramView2MinN, ngramView2MaxN, stopwords);
        FrequencyDistribution<String> documentNGrams = ComboUtils.getMultipleViewNgrams(
                jcases, null, ngramLowerCase, filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords);


        for (String ngram : documentNGrams.getKeys()) {
            for (int i=0;i<documentNGrams.getCount(ngram);i++){
                addField(jcas, LuceneNGramFeatureExtractor.LUCENE_NGRAM_FIELD, ngram); 
            }
        }
        for (String ngram : view1NGrams.getKeys()) {
            for (int i=0;i<view1NGrams.getCount(ngram);i++){
                addField(jcas, LuceneNGramPairFeatureExtractor.LUCENE_NGRAM_FIELD1, ngram); 
            }
        }
        for (String ngram : view2NGrams.getKeys()) {
            for (int i=0;i<view2NGrams.getCount(ngram);i++){
                addField(jcas, LuceneNGramPairFeatureExtractor.LUCENE_NGRAM_FIELD2, ngram); 
            }
        }
        
        try {
        	writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }   
}