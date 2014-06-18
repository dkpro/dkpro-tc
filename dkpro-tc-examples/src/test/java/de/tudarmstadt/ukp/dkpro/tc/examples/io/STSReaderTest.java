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