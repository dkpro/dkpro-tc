/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
