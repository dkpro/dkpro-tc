package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

public class LuceneComboNGramPairMetaCollector
	extends LuceneNGramPairMetaCollector
{

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        // this will close the lucene writer 
        super.collectionProcessComplete();

        // combination goes here
    
    }   
}