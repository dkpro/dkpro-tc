package de.tudarmstadt.ukp.dkpro.tc.examples.io;

import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class STSReaderTest
{
    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
    }
    
    @Test
    public void stsReaderTest()
            throws Exception
    {
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                STSReader.class,
                STSReader.PARAM_INPUT_FILE, "src/test/resources/data/sts/STS.input.MSRpar.txt",
                STSReader.PARAM_GOLD_FILE, "src/test/resources/data/sts/STS.gs.MSRpar.txt"
        );

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {

// FIXME should test not write to console
//            System.out.println(jcas.getView(AbstractPairReader.PART_ONE).getDocumentText());
//            System.out.println(jcas.getView(AbstractPairReader.PART_TWO).getDocumentText());
            i++;
        }
        assertEquals(10, i);
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}