package de.tudarmstadt.ukp.dkpro.tc.groovyexamples;

import java.io.IOException;

import de.tudarmstadt.ukp.dkpro.tc.core.ExperimentStarter;

public class SentimentPolarityDemoStarter
    extends ExperimentStarter
{

    public static void main(String[] args)
        throws InstantiationException, IllegalAccessException, IOException
    {
        start("scripts/SentimentPolarityDemo.groovy");
    }
}
