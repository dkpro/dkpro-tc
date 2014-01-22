package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneNGramMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LuceneNGramFeatureExtractor
    extends NGramFeatureExtractorBase
    implements MetaDependent, ClassificationUnitFeatureExtractor
{
    
    public static final String LUCENE_NGRAM_FIELD = "ngram";

    public static final String PARAM_LUCENE_DIR = "luceneDir";
    @ConfigurationParameter(name = PARAM_LUCENE_DIR, mandatory = true)
    private File luceneDir;
    
    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LuceneNGramMetaCollector.class);

        return metaCollectorClasses;
    }

    @Override
    protected Set<String> getTopNgrams()
        throws ResourceInitializationException
    {       
        Set<String> topNGrams = new HashSet<String>();

        IndexReader reader;
        try {
            reader = DirectoryReader.open(FSDirectory.open(luceneDir));
            Fields fields = MultiFields.getFields(reader);
            if (fields != null) {
                Terms terms = fields.terms(LUCENE_NGRAM_FIELD);
                if (terms != null) {
                    TermsEnum termsEnum = terms.iterator(null);
                    BytesRef text = null;
                    while ((text = termsEnum.next()) != null) {
                        String term = text.utf8ToString();
                        long freq = termsEnum.totalTermFreq();

                        if (freq > 1) {
                            System.out.println(term + " - " + freq);
                        }
                        
//                        TermStats termStat = HighFreqTerms.getTotalTermFreq(reader, termsEnum

                    }
                }
            }
            
            for (TermStats termStat : HighFreqTerms.getHighFreqTerms(reader, ngramUseTopK, LUCENE_NGRAM_FIELD)) {
                topNGrams.add(termStat.termtext.utf8ToString());
            }

        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        
        getLogger().log(Level.INFO, "+++ TAKING " + topNGrams.size() + " NGRAMS");

        return topNGrams;
    }
}