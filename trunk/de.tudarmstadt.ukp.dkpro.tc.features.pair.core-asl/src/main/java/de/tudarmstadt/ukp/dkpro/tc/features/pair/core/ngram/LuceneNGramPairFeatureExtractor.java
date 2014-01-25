package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.TermFreqQueue;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.TermFreqTuple;

public class LuceneNGramPairFeatureExtractor
	extends NGramPairFeatureExtractorBase
{

    public static final String PARAM_LUCENE_DIR = "luceneDir";
    @ConfigurationParameter(name = LuceneNGramFeatureExtractor.PARAM_LUCENE_DIR, mandatory = true)
    private File luceneDir;
    


	@Override
	public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LuceneNGramPairMetaCollector.class);

        return metaCollectorClasses;
    }
	
	
    @Override
    protected Set<String> getTopNgrams(float ngramFreqThreshold, int ngramUseTopK, String field)
        throws ResourceInitializationException
    {       

        Set<String> topNGrams = new HashSet<String>();
        
        PriorityQueue<TermFreqTuple> topN = new TermFreqQueue(ngramUseTopK);

        IndexReader reader;
        try {
            reader = DirectoryReader.open(FSDirectory.open(luceneDir));
            Fields fields = MultiFields.getFields(reader);
            if (fields != null) {
                Terms terms = fields.terms(field);
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
    
  protected Set<String> getTopNgramsCombo(Set<String> topKSetAll, Set<String> topKSetView1, 
			Set<String> topKSetView2, float ngramFreqThresholdCombo, 
			int ngramUseTopKCombo)
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
	      for (int i = 0; i < hits.length; i++) {
	      		int docId = hits[i].doc;
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
      }catch(IOException e){
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
    
    class ValueComparator
    implements Comparator<String>
{

    Map<String, Long> base;

    public ValueComparator(Map<String, Long> base)
    {
        this.base = base;
    }

    @Override
    public int compare(String a, String b)
    {

        if (base.get(a) < base.get(b)) {
            return 1;
        }
        else {
            return -1;
        }
    }
}

}
