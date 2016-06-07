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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;

/**
 * Tests the utility class for creating sufficiently many splits to match the number of requested
 * folds. Splitting is different for sequences
 */
public class TestFoldUtil
{
    private TemporaryFolder tmpFoldNoSeq, tmpFoldSeq;
    private JCas jcasNoSequence;
    private JCas jcasSequence;

    @Test(expected = IllegalStateException.class)
    public void testSeqExceptionOnTooFewData()
        throws Exception
    {
        FoldUtil.createMinimalSplit(tmpFoldSeq.getRoot().getAbsolutePath(), 11, 1, true);
    }
    
    @Test
    public void testSeqSplittingAllTcuIntoOneCas()
        throws Exception
    {
        File output = FoldUtil.createMinimalSplit(tmpFoldSeq.getRoot().getAbsolutePath(), 1, 1,
                true);

        List<File> writtenBins = getWrittenBins(output);

        List<List<Integer>> numTcusCas = countNumberOfTextClassificationSequencesAndUnitsPerCas(writtenBins);

        assertEquals(1, writtenBins.size());
        
        assertEquals(new Integer(10), numTcusCas.get(0).get(0));
        assertEquals(new Integer(20), numTcusCas.get(1).get(0));
    }
    

    private List<List<Integer>> countNumberOfTextClassificationSequencesAndUnitsPerCas(List<File> writtenBins) throws Exception
    {
        List<List<Integer>> arrayList = new ArrayList<>();
        List<Integer> units = new ArrayList<>();
        List<Integer> seq = new ArrayList<>();
        for (File f : writtenBins) {
            JCas jcas = JCasFactory.createJCas();
            CollectionReader createReader = createReader(jcas, f);
            createReader.getNext(jcas.getCas());

            Collection<TextClassificationTarget> colUni = JCasUtil.select(jcas,
                    TextClassificationTarget.class);
            units.add(colUni.size());
            Collection<TextClassificationSequence> colSeq = JCasUtil.select(jcas,
                    TextClassificationSequence.class);
            seq.add(colSeq.size());
        }
        arrayList.add(seq);
        arrayList.add(units);
        return arrayList;
    }
    
    @Test
    public void testSeqSplittingOneTcuIntoOneCas()
        throws Exception
    {
        File output = FoldUtil.createMinimalSplit(tmpFoldSeq.getRoot().getAbsolutePath(), 10, 1,
                true);

        List<File> writtenBins = getWrittenBins(output);

        List<List<Integer>> numTcusCas = countNumberOfTextClassificationSequencesAndUnitsPerCas(writtenBins);

        assertEquals(10, writtenBins.size());

        assertEquals(new Integer(1), numTcusCas.get(0).get(0));
        assertEquals(new Integer(2), numTcusCas.get(1).get(0));
        
        assertEquals(new Integer(1), numTcusCas.get(0).get(1));
        assertEquals(new Integer(2), numTcusCas.get(1).get(1));
        
        assertEquals(new Integer(1), numTcusCas.get(0).get(2));
        assertEquals(new Integer(2), numTcusCas.get(1).get(2));
        
        assertEquals(new Integer(1), numTcusCas.get(0).get(3));
        assertEquals(new Integer(2), numTcusCas.get(1).get(3));
        
        assertEquals(new Integer(1), numTcusCas.get(0).get(4));
        assertEquals(new Integer(2), numTcusCas.get(1).get(4));
        
        assertEquals(new Integer(1), numTcusCas.get(0).get(5));
        assertEquals(new Integer(2), numTcusCas.get(1).get(5));
        
        assertEquals(new Integer(1), numTcusCas.get(0).get(6));
        assertEquals(new Integer(2), numTcusCas.get(1).get(6));
        
        assertEquals(new Integer(1), numTcusCas.get(0).get(7));
        assertEquals(new Integer(2), numTcusCas.get(1).get(7));
        
        assertEquals(new Integer(1), numTcusCas.get(0).get(8));
        assertEquals(new Integer(2), numTcusCas.get(1).get(8));
        
        assertEquals(new Integer(1), numTcusCas.get(0).get(9));
        assertEquals(new Integer(2), numTcusCas.get(1).get(9));
    }

    @Test(expected = IllegalStateException.class)
    public void testNoSeqExceptionOnTooFewData()
        throws Exception
    {
        FoldUtil.createMinimalSplit(tmpFoldNoSeq.getRoot().getAbsolutePath(), 14, 1, false);
    }

    @Test
    public void testNoSeqSplittingAllTcuIntoOneCas()
        throws Exception
    {
        File output = FoldUtil.createMinimalSplit(tmpFoldNoSeq.getRoot().getAbsolutePath(), 1, 1,
                false);

        List<File> writtenBins = getWrittenBins(output);

        List<Integer> numTcusCas = countNumberOfTextClassificationUnitsPerCas(writtenBins);

        assertEquals(1, writtenBins.size());
        assertEquals(new Integer(13), numTcusCas.get(0));
    }

    @Test
    public void testNoSeqSplittingOneTcuIntoOneCas()
        throws Exception
    {
        File output = FoldUtil.createMinimalSplit(tmpFoldNoSeq.getRoot().getAbsolutePath(), 13, 1,
                false);

        List<File> writtenBins = getWrittenBins(output);

        List<Integer> numTcusCas = countNumberOfTextClassificationUnitsPerCas(writtenBins);

        assertEquals(13, writtenBins.size());
        assertEquals(new Integer(1), numTcusCas.get(0));
        assertEquals(new Integer(1), numTcusCas.get(1));
        assertEquals(new Integer(1), numTcusCas.get(2));
        assertEquals(new Integer(1), numTcusCas.get(3));
        assertEquals(new Integer(1), numTcusCas.get(4));
        assertEquals(new Integer(1), numTcusCas.get(5));
        assertEquals(new Integer(1), numTcusCas.get(6));
        assertEquals(new Integer(1), numTcusCas.get(7));
        assertEquals(new Integer(1), numTcusCas.get(8));
        assertEquals(new Integer(1), numTcusCas.get(9));
        assertEquals(new Integer(1), numTcusCas.get(10));
        assertEquals(new Integer(1), numTcusCas.get(11));
        assertEquals(new Integer(1), numTcusCas.get(12));
    }

    private List<Integer> countNumberOfTextClassificationUnitsPerCas(List<File> writtenBins)
        throws Exception
    {
        List<Integer> arrayList = new ArrayList<Integer>();
        for (File f : writtenBins) {
            JCas jcas = JCasFactory.createJCas();
            CollectionReader createReader = createReader(jcas, f);
            createReader.getNext(jcas.getCas());

            Collection<TextClassificationTarget> units = JCasUtil.select(jcas,
                    TextClassificationTarget.class);
            arrayList.add(units.size());
        }
        return arrayList;
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

    @Before
    public void setUp()
        throws Exception
    {
        createNoSequenceCas();
        createSequenceCas();
    }

    private void createSequenceCas()
        throws IOException, UIMAException
    {
        tmpFoldSeq = new TemporaryFolder();
        tmpFoldSeq.create();

        jcasSequence = JCasFactory.createJCas();
        jcasSequence
                .setDocumentText("One A Two B Three C Four D Five E Six F Seven G Eight H Nine I Ten J");
        setSeq(jcasSequence, 0, 5);
        setSeq(jcasSequence, 6, 11);
        setSeq(jcasSequence, 12, 19);
        setSeq(jcasSequence, 20, 26);
        setSeq(jcasSequence, 27, 33);
        setSeq(jcasSequence, 34, 39);
        setSeq(jcasSequence, 40, 47);
        setSeq(jcasSequence, 48, 55);
        setSeq(jcasSequence, 56, 62);
        setSeq(jcasSequence, 63, 68);
        
        DocumentMetaData dmd = new DocumentMetaData(jcasSequence);
        dmd.setDocumentId("id");
        dmd.addToIndexes();

        AnalysisEngine xmiWriter = AnalysisEngineFactory.createEngine(BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION, tmpFoldSeq.getRoot(),
                BinaryCasWriter.PARAM_FORMAT, "6+");

        xmiWriter.process(jcasSequence);
    }

    private void setSeq(JCas cas, int beg, int end)
    {
        TextClassificationSequence seq = new TextClassificationSequence(cas, beg, end);
        seq.addToIndexes();

        String[] split = seq.getCoveredText().split(" ");
        setUnit(cas, beg, beg + split[0].length());
        setUnit(cas, beg + split[0].length() + 1, end);
    }

    private void createNoSequenceCas()
        throws Exception
    {
        tmpFoldNoSeq = new TemporaryFolder();
        tmpFoldNoSeq.create();

        jcasNoSequence = JCasFactory.createJCas();
        jcasNoSequence
                .setDocumentText("Mr. Hawksley said yesterday he would be willing to go before the city .");

        setUnit(jcasNoSequence, 0, 2);
        setUnit(jcasNoSequence, 4, 12);
        setUnit(jcasNoSequence, 13, 18);
        setUnit(jcasNoSequence, 18, 28);
        setUnit(jcasNoSequence, 31, 36);
        setUnit(jcasNoSequence, 37, 39);
        setUnit(jcasNoSequence, 40, 47);
        setUnit(jcasNoSequence, 48, 50);
        setUnit(jcasNoSequence, 51, 53);
        setUnit(jcasNoSequence, 54, 60);
        setUnit(jcasNoSequence, 61, 64);
        setUnit(jcasNoSequence, 65, 69);
        setUnit(jcasNoSequence, 70, 71);

        DocumentMetaData dmd = new DocumentMetaData(jcasNoSequence);
        dmd.setDocumentId("id");
        dmd.addToIndexes();

        AnalysisEngine xmiWriter = AnalysisEngineFactory.createEngine(BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION, tmpFoldNoSeq.getRoot(),
                BinaryCasWriter.PARAM_FORMAT, "6+");

        xmiWriter.process(jcasNoSequence);
    }

    private void setUnit(JCas jcas, int beg, int end)
    {
        TextClassificationTarget tcu = new TextClassificationTarget(jcas, beg, end);
        tcu.addToIndexes();
    }
}
