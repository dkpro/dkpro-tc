/**
 * Copyright 2018
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.examples.shallow.crfsuite.sequence;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.shallow.crfsuite.sequence.filter.FilterLuceneCharacterNgramStartingWithLetter;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter;
import org.junit.Before;
import org.junit.Test;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class CRFSuiteBrownPosDemoTest
    extends TestCaseSuperClass implements Constants
{
    CRFSuiteBrownPosDemo javaExperiment;

    @Before
    public void setup() throws Exception
    {
        super.setup();
        javaExperiment = new CRFSuiteBrownPosDemo();
    }

    @Test
    public void runTrainTestNoFilter() throws Exception
    {
        Map<String, Object> config = new HashMap<>();
        config.put(DIM_CLASSIFICATION_ARGS,
                new Object[] {new CrfSuiteAdapter(),
                        CrfSuiteAdapter.ALGORITHM_LBFGS, "-p", "max_iterations=5" });
        config.put(DIM_DATA_WRITER, new CrfSuiteAdapter().getDataWriterClass().getName());
        config.put(DIM_FEATURE_USE_SPARSE, new CrfSuiteAdapter().useSparseFeatures());
        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);
        
        ParameterSpace pSpace = CRFSuiteBrownPosDemo.getParameterSpace(
                Constants.FM_SEQUENCE, Constants.LM_SINGLE_LABEL, mlas, null);

        javaExperiment.runTrainTest(pSpace);

        assertEquals(1, ContextMemoryReport.id2outcomeFiles.size());

        List<String> lines = FileUtils.readLines(ContextMemoryReport.id2outcomeFiles.get(0),
                "utf-8");
        assertEquals(34, lines.size());

        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals(
                "#labels 0=NN 1=JJ 2=NP 3=DTS 4=BEDZ 5=HV 6=PPO 7=DT 8=NNS 9=PPS 10=JJT 11=ABX 12=MD 13=DOD 14=VBD 15=VBG 16=QL 32=%28null%29 17=pct 18=CC 19=VBN 20=NPg 21=IN 22=WDT 23=BEN 24=VB 25=BER 26=AP 27=RB 28=CS 29=AT 30=HVD 31=TO",
                lines.get(1));
        // 2nd line time stamp

        // Crfsuite results are sensitive to some extend to the platform, to
        // account for this sensitivity we check only that the "prediction"
        // field is filled with any number but do not test for a specific value
        assertTrue(lines.get(3).matches("0000_0000_0000_The=[0-9]+;29;-1"));
        assertTrue(lines.get(4).matches("0000_0000_0001_bill=[0-9]+;0;-1"));
        assertTrue(lines.get(5).matches("0000_0000_0002_,=[0-9]+;17;-1"));
        assertTrue(lines.get(6).matches("0000_0000_0003_which=[0-9]+;22;-1"));
        assertTrue(lines.get(7).matches("0000_0000_0004_Daniel=[0-9]+;2;-1"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void runTrainTestFilter() throws Exception
    {
        Map<String, Object> config = new HashMap<>();
        config.put(DIM_CLASSIFICATION_ARGS,
                new Object[] {new CrfSuiteAdapter(),
                        CrfSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR });
        config.put(DIM_DATA_WRITER, new CrfSuiteAdapter().getDataWriterClass().getName());
        config.put(DIM_FEATURE_USE_SPARSE, new CrfSuiteAdapter().useSparseFeatures());
        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);

        Dimension<List<String>> dimFilter = Dimension.create(Constants.DIM_FEATURE_FILTERS,
                asList(FilterLuceneCharacterNgramStartingWithLetter.class.getName()));

        ParameterSpace pSpace = CRFSuiteBrownPosDemo.getParameterSpace(
                Constants.FM_SEQUENCE, Constants.LM_SINGLE_LABEL, mlas, dimFilter);

        javaExperiment.runTrainTest(pSpace);

    }
}
