package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.PriorityQueue;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.TermFreqQueue;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.TermFreqTuple;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramPairMetaCollector;

public class LuceneNGramPairFeatureExtractor
	extends LucenePairFeatureExtractorBase
{
    public static final String LUCENE_NGRAM_FIELD = "ngram";
    public static final String LUCENE_NGRAM_FIELD1 = "ngram1";
    public static final String LUCENE_NGRAM_FIELD2 = "ngram2";

	@Override
	public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LuceneNGramPairMetaCollector.class);

        return metaCollectorClasses;
    }
	
    @Override
    protected Set<String> getTopNgrams()
        throws ResourceInitializationException
    {       
        return getTopNgrams(ngramUseTopKAll, LUCENE_NGRAM_FIELD);
    }
    
    @Override
    protected Set<String> getTopNgramsView1()
        throws ResourceInitializationException
    {
        return getTopNgrams(ngramUseTopK1, LUCENE_NGRAM_FIELD1);
    }

    @Override
    protected Set<String> getTopNgramsView2()
        throws ResourceInitializationException
    {
        return getTopNgrams(ngramUseTopK2, LUCENE_NGRAM_FIELD2);
    }
    
    @Override
    protected Set<String> getTopNgramsCombo()
		throws ResourceInitializationException
	{

      Set<String> topNGramsCombo = new HashSet<String>();
      
      PriorityQueue<TermFreqTuple> topN = new TermFreqQueue(ngramUseTopKCombo);
      try{
	      IndexReader reader = DirectoryReader.open(FSDirectory.open(luceneDir));
	      
	      IndexSearcher is = new IndexSearcher(reader);
	      Query query = new MatchAllDocsQuery();
	      
	      TopDocs topDocs = is.search(query, reader.maxDoc());
	      ScoreDoc[] hits = topDocs.scoreDocs;
	      for (ScoreDoc hit : hits) {
	      		int docId = hit.doc;
	      		Document d = is.doc(docId);
	      		String[] ngramArray1 = d.getValues(LUCENE_NGRAM_FIELD1);
	      		String[] ngramArray2 = d.getValues(LUCENE_NGRAM_FIELD2);
	      		for(String ngram1: ngramArray1){
	      			if (topKSetView1.contains(ngram1) && topKSetAll.contains(ngram1)){
	      				for(String ngram2: ngramArray2){
	      					if (topKSetView2.contains(ngram2) && topKSetAll.contains(ngram2)){
	      						int combinedSize = ngram1.split("_").length + ngram2.split("_").length;
	      						if(combinedSize <= ngramMaxNCombo && combinedSize >= ngramMaxNCombo){
	      							topN.insertWithOverflow(new TermFreqTuple(combo(ngram1, ngram2), 1));
	      						}
	      					}
	      				}
	      			}
	      		}
	      }
      }
      catch(IOException e){
    	  throw new ResourceInitializationException();
      }

      for (int i=0; i < topN.size(); i++) {
          TermFreqTuple tuple = topN.pop();
//          System.out.println(tuple.getTerm() + " - " + tuple.getFreq());
          topNGramsCombo.add(tuple.getTerm());
      }
      
      getLogger().log(Level.INFO, "+++ TAKING " + topNGramsCombo.size() + " NGRAMS");

      return topNGramsCombo;
	}

    private Set<String> getTopNgrams(int topNgramThreshold, String fieldName)
        throws ResourceInitializationException
    {       

        Set<String> topNGrams = new HashSet<String>();
        
        PriorityQueue<TermFreqTuple> topN = new TermFreqQueue(topNgramThreshold);

        IndexReader reader;
        try {
            reader = DirectoryReader.open(FSDirectory.open(luceneDir));
            Fields fields = MultiFields.getFields(reader);
            if (fields != null) {
                Terms terms = fields.terms(fieldName);
                if (terms != null) {
                    TermsEnum termsEnum = terms.iterator(null);
                    BytesRef text = null;
                    while ((text = termsEnum.next()) != null) {
                        String term = text.utf8ToString();
                        long freq = termsEnum.totalTermFreq();
                        topN.insertWithOverflow(new TermFreqTuple(term, freq));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        
        for (int i=0; i < topN.size(); i++) {
            TermFreqTuple tuple = topN.pop();
//            System.out.println(tuple.getTerm() + " - " + tuple.getFreq());
            topNGrams.add(tuple.getTerm());
        }
        
        getLogger().log(Level.INFO, "+++ TAKING " + topNGrams.size() + " NGRAMS");

        return topNGrams;
    }
}