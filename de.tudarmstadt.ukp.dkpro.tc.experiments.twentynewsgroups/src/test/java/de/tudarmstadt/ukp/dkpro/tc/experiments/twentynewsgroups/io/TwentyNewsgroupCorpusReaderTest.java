package de.tudarmstadt.ukp.dkpro.tc.experiments.twentynewsgroups.io;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class TwentyNewsgroupCorpusReaderTest
{

    @Test
    public void testTwentyNewsgroupsCorpusReader()
        throws Exception
    {
        CollectionReader reader = createCollectionReader(
                TwentyNewsgroupCorpusReader.class,
                TwentyNewsgroupCorpusReader.PARAM_PATH, "classpath:/data/",
                TwentyNewsgroupCorpusReader.PARAM_LANGUAGE, "en",
                TwentyNewsgroupCorpusReader.PARAM_PATTERNS, new String[] {
                    TwentyNewsgroupCorpusReader.INCLUDE_PREFIX + "*/*.txt" });

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData md = DocumentMetaData.get(jcas);
            dumpMetaData(md);
            i++;

            for (TextClassificationOutcome outcome : JCasUtil.select(jcas, TextClassificationOutcome.class)) {
                System.out.println(outcome);
            }
        }
        assertEquals(12, i);
    }

    private void dumpMetaData(final DocumentMetaData aMetaData)
    {
        System.out.println("Collection ID: "+aMetaData.getCollectionId());
        System.out.println("ID           : "+aMetaData.getDocumentId());
        System.out.println("Base URI     : "+aMetaData.getDocumentBaseUri());
        System.out.println("URI          : "+aMetaData.getDocumentUri());
    }
}
