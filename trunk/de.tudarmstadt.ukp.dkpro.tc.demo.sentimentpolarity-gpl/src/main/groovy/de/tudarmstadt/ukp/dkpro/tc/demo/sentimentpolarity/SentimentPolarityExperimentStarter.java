package de.tudarmstadt.ukp.dkpro.tc.demo.sentimentpolarity;

import java.io.IOException;

import de.tudarmstadt.ukp.dkpro.tc.core.ExperimentStarter;

public class SentimentPolarityExperimentStarter
    extends ExperimentStarter
{

    public static void main(String[] args)
        throws InstantiationException, IllegalAccessException, IOException
    {
        start("scripts/SentimentPolarityGroovyExperiment.groovy");
    }
}
