package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.document;

import java.io.IOException;

import de.tudarmstadt.ukp.dkpro.tc.core.ExperimentStarter;

/**
 * Java connector for the Sentiment Polarity Demo groovy script
 */
public class SentimentPolarityDemoStarter
    extends ExperimentStarter
{

    /**
     * @param args
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     */
    public static void main(String[] args)
        throws InstantiationException, IllegalAccessException, IOException
    {
        start("scripts/SentimentPolarityDemo.groovy");
    }
}
