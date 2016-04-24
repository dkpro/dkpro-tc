/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.ml;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;

public class TestFoldUtil
{
    TemporaryFolder tmpFold;
    private JCas jcas;

    @Before
    public void setUp()
        throws Exception
    {
        tmpFold = new TemporaryFolder();
        tmpFold.create();

        jcas = JCasFactory.createJCas();

        jcas.setDocumentText("Mr. Hawksley said yesterday he would be willing to go before the city .");

        set(jcas, 0, 2);
        set(jcas, 4, 12);
        set(jcas, 13, 18);
        set(jcas, 18, 28);
        set(jcas, 31, 36);
        set(jcas, 37, 39);
        set(jcas, 40, 47);
        set(jcas, 48, 50);
        set(jcas, 51, 53);
        set(jcas, 54, 60);
        set(jcas, 61, 64);
        set(jcas, 65, 69);
        set(jcas, 70, 71);

        DocumentMetaData dmd = new DocumentMetaData(jcas);
        dmd.setDocumentId("id");
        dmd.addToIndexes();

        AnalysisEngine xmiWriter = AnalysisEngineFactory.createEngine(BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION, tmpFold.getRoot(),
                BinaryCasWriter.PARAM_FORMAT, "6+");

        xmiWriter.process(jcas);
    }

    private void set(JCas jcas, int beg, int end)
    {
        TextClassificationUnit tcu = new TextClassificationUnit(jcas, beg, end);
        tcu.addToIndexes();
    }

    @Test
    public void doit()
        throws Exception
    {
        File output = FoldUtil.createMinimalSplit(tmpFold.getRoot().getAbsolutePath(), 4, 1);

        List<File> writtenBins = getWrittenBins(output);

        List<Integer> numTcusCas = new ArrayList<Integer>();
        for (File f : writtenBins) {
            JCas jcas = JCasFactory.createJCas();
            CollectionReader createReader = createReader(jcas, f);
            createReader.getNext(jcas.getCas());

            Collection<TextClassificationUnit> units = JCasUtil.select(jcas,
                    TextClassificationUnit.class);
            numTcusCas.add(units.size());
        }

        assertEquals(5, writtenBins.size());
        assertEquals(new Integer(3), numTcusCas.get(0));
        assertEquals(new Integer(3), numTcusCas.get(1));
        assertEquals(new Integer(3), numTcusCas.get(2));
        assertEquals(new Integer(3), numTcusCas.get(3));
        assertEquals(new Integer(1), numTcusCas.get(4));
    }

    private CollectionReader createReader(JCas jcas, File f)
        throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReader(BinaryCasReader.class,
                BinaryCasReader.PARAM_SOURCE_LOCATION, f);
    }

    private List<File> getWrittenBins(File output)
    {
        List<File> bins = new ArrayList<File>();
        for (File f : output.listFiles()) {
            if (f.getName().endsWith(".bin")) {
                bins.add(f);
            }
        }
        return bins;
    }
}
