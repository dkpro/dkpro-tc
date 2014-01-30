package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneField;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.LuceneNGramPairFeatureExtractor;

public class LuceneNGramPairMetaCollector
	extends LuceneBasedPairMetaCollector
{
    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
    private int ngramMinN;

    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int ngramMaxN;

    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    private String ngramStopwordsFile;

    @ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_LOWER_CASE, mandatory = false, defaultValue = "true")
    private boolean ngramLowerCase;

    private  Set<String> stopwords;
    // end repeat
	
    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW1, mandatory = true, defaultValue = "1")
    private int ngramView1MinN;

    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW1, mandatory = true, defaultValue = "3")
    private int ngramView1MaxN;
    
    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MIN_N_VIEW2, mandatory = true, defaultValue = "1")
    private int ngramView2MinN;

    @ConfigurationParameter(name = LuceneNGramPairFeatureExtractor.PARAM_NGRAM_MAX_N_VIEW2, mandatory = true, defaultValue = "3")
    private int ngramView2MaxN;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        stopwords = new HashSet<String>();

        if (ngramStopwordsFile != null && !ngramStopwordsFile.isEmpty()) {
            try {
                URL stopUrl = ResourceUtils.resolveLocation(ngramStopwordsFile, null);
                InputStream is = stopUrl.openStream();
                for(String stopword: IOUtils.readLines(is, "UTF-8")){
                    if(ngramLowerCase){
                        stopwords.add(stopword.toLowerCase());
                    }else{
                        stopwords.add(stopword);
                    }
                }
            }
            catch (Exception e) {
                throw new ResourceInitializationException(e);
            }
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
        FrequencyDistribution<String> documentNGrams = NGramUtils.getDocumentNgrams(
                jcas, ngramLowerCase, ngramMinN, ngramMaxN, stopwords);
        FrequencyDistribution<String> view1NGrams = NGramUtils.getDocumentNgrams(
                view1, ngramLowerCase, ngramView1MinN, ngramView1MaxN, stopwords);
        FrequencyDistribution<String> view2NGrams = NGramUtils.getDocumentNgrams(
                view2, ngramLowerCase, ngramView2MinN, ngramView2MaxN, stopwords);

        Document doc = new Document();
        doc.add(new StringField(
                "id",
                DocumentMetaData.get(jcas).getDocumentTitle(),
                Field.Store.YES
        ));
        
        for (String ngram : documentNGrams.getKeys()) {
            Field field = new LuceneField(
                    LuceneNGramFeatureExtractor.LUCENE_NGRAM_FIELD,
                    ngram, 
                    Field.Store.YES
            );
            doc.add(field);
        }
        for (String ngram : view1NGrams.getKeys()) {
            Field field = new LuceneField(
                    LuceneNGramPairFeatureExtractor.LUCENE_NGRAM_FIELD1,
                    ngram, 
                    Field.Store.YES
            );
            doc.add(field);
        }
        for (String ngram : view2NGrams.getKeys()) {
            Field field = new LuceneField(
                    LuceneNGramPairFeatureExtractor.LUCENE_NGRAM_FIELD2,
                    ngram, 
                    Field.Store.YES
            );
            doc.add(field);
        }
        
        try {
            indexWriter.addDocument(doc);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }   
}