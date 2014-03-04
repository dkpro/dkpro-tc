package de.tudarmstadt.ukp.dkpro.tc.examples.io;

import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class ReutersCorpusReaderTest
{

    @Test
    public void testReutersCorpusReader()
        throws Exception
    {
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                ReutersCorpusReader.class,
                ReutersCorpusReader.PARAM_SOURCE_LOCATION, "classpath:/data/reuters/training",
                ReutersCorpusReader.PARAM_GOLD_LABEL_FILE, "classpath:/data/reuters/cats.txt",
                ReutersCorpusReader.PARAM_LANGUAGE, "en",
                ReutersCorpusReader.PARAM_PATTERNS, new String[] {
                    ReutersCorpusReader.INCLUDE_PREFIX + "*.txt" });

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData md = DocumentMetaData.get(jcas);
            dumpMetaData(md);
            i++;

// FIXME should test not write to console
//            for (TextClassificationOutcome outcome : JCasUtil.select(jcas, TextClassificationOutcome.class)) {
//                System.out.println(outcome);
//            }
        }
        assertEquals(7, i);
    }

    private void dumpMetaData(final DocumentMetaData aMetaData)
    {
        System.out.println("Collection ID: "+aMetaData.getCollectionId());
        System.out.println("ID           : "+aMetaData.getDocumentId());
        System.out.println("Base URI     : "+aMetaData.getDocumentBaseUri());
        System.out.println("URI          : "+aMetaData.getDocumentUri());
    }
}
