package de.tudarmstadt.ukp.dkpro.tc.examples.io;

import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class TwentyNewsgroupsCorpusReaderTest
{

    @Test
    public void testTwentyNewsgroupsCorpusReader()
        throws Exception
    {
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TwentyNewsgroupsCorpusReader.class,
                TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION, "classpath:/data/twentynewsgroups/",
                TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, "en",
                TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                new String[] { TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt" });

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData md = DocumentMetaData.get(jcas);
            dumpMetaData(md);
            i++;

// FIXME should test not write to console
//            for (TextClassificationOutcome outcome : JCasUtil.select(jcas,
//                    TextClassificationOutcome.class)) {
//                System.out.println(outcome);
//            }
        }
        assertEquals(12, i);
    }

    private void dumpMetaData(final DocumentMetaData aMetaData)
    {
        System.out.println("Collection ID: " + aMetaData.getCollectionId());
        System.out.println("ID           : " + aMetaData.getDocumentId());
        System.out.println("Base URI     : " + aMetaData.getDocumentBaseUri());
        System.out.println("URI          : " + aMetaData.getDocumentUri());
    }
}
