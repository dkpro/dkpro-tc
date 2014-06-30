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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.tc.examples.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

public class PairTwentyNewsgroupsReaderTest
{
    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
    }

    @Test
    public void testPairTwentyNewsgroupsCorpusReader()
        throws Exception
    {
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                PairTwentyNewsgroupsReader.class,
                PairTwentyNewsgroupsReader.PARAM_LISTFILE,
                "src/test/resources/data/pairs/pairslist",
                PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE, "en");

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData md = DocumentMetaData.get(jcas);
            dumpMetaData(md);

            assertNotNull("CollectionID should not be null", md.getCollectionId());
            assertNotNull("Base URI should not be null", md.getDocumentBaseUri());
            assertNotNull("URI should not be null", md.getDocumentUri());

            JCas view1 = jcas.getView("PART_ONE");
            DocumentMetaData mdView1 = DocumentMetaData.get(view1);
            JCas view2 = jcas.getView("PART_TWO");
            DocumentMetaData mdView2 = DocumentMetaData.get(view2);

            if (i == 0) {
                assertTrue("Pair1Text1 should be 11891 char long", mdView1.getCoveredText()
                        .length() == 11891);
                assertTrue("Pair1Text2 should be 32056 char long", mdView2.getCoveredText()
                        .length() == 32056);
            }

            for (TextClassificationOutcome outcome : JCasUtil.select(jcas,
                    TextClassificationOutcome.class)) {
                assertTrue("Outcomes should be set", outcome.getOutcome().equals("y"));
                System.out.println(outcome);
            }
            assertEquals("Incorrect count of outcomes", 1, 1);
            i++;
        }
        assertEquals(2, i);
    }

    private void dumpMetaData(final DocumentMetaData aMetaData)
    {
        System.out.println("Collection ID: " + aMetaData.getCollectionId());
        System.out.println("ID           : " + aMetaData.getDocumentId());
        System.out.println("Base URI     : " + aMetaData.getDocumentBaseUri());
        System.out.println("URI          : " + aMetaData.getDocumentUri());
    }
}
