package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import static org.apache.commons.io.FileUtils.deleteQuietly;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramFeatureExtractor;

public abstract class LuceneBasedMetaCollector
    extends MetaCollector
{
    
    public final static String LUCENE_DIR = "lucence";
    
    @ConfigurationParameter(name = LuceneNGramFeatureExtractor.PARAM_LUCENE_DIR, mandatory = true)
    private File luceneDir;

    protected IndexWriter indexWriter;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        deleteQuietly(luceneDir);
        if (!luceneDir.mkdirs()) {
            throw new ResourceInitializationException(new IOException("Cannot create folder: " + luceneDir));
        }
        
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44, null);
        
        try {
            indexWriter = new IndexWriter(FSDirectory.open(luceneDir), config);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }
    
    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            indexWriter.commit();
            indexWriter.close();
        } catch (CorruptIndexException e) {
            throw new AnalysisEngineProcessException(e);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
    
    @Override
    public Map<String, String> getParameterKeyPairs()
    {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(LuceneNGramFeatureExtractor.PARAM_LUCENE_DIR, LUCENE_DIR);
        return mapping;
    }
}