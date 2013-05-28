package de.tudarmstadt.ukp.dkpro.tc.experiments.regression.io;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;

public class STSReaderTest
{

    @Test
    public void stsReaderTest()
            throws Exception
    {
        CollectionReader reader = createCollectionReader(
                STSReader.class,
                STSReader.PARAM_INPUT_FILE, "src/main/resources/sts2012/STS.input.MSRpar.txt",
                STSReader.PARAM_GOLD_FILE, "src/main/resources/sts2012/STS.gs.MSRpar.txt"
        );

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            
            System.out.println(jcas.getView(AbstractPairReader.PART_ONE).getDocumentText());
            System.out.println(jcas.getView(AbstractPairReader.PART_TWO).getDocumentText());
            i++;
        }
        assertEquals(750, i);
    }
}