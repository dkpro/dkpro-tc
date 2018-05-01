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
package org.dkpro.tc.examples.shallow.res;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.shallow.io.PairTwentyNewsgroupsReader;
import org.dkpro.tc.examples.util.ContextMemoryReport;
import org.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor;
import org.dkpro.tc.features.pair.similarity.SimilarityPairFeatureExtractor;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchTrainTestReport;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import dkpro.similarity.algorithms.lexical.string.CosineSimilarity.NormalizationMode;
import dkpro.similarity.algorithms.lexical.uima.string.CosineSimilarityResource;
import weka.classifiers.functions.SMO;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class ExternalResourcesTest
    extends TestCaseSuperClass
    implements Constants
{

    public static final String experimentName = "PairTwentyNewsgroupsExperiment";
    public static final String languageCode = "en";
    public static final String listFilePathTrain = "src/main/resources/data/twentynewsgroups/pairs/pairslist.train";
    public static final String listFilePathTest = "src/main/resources/data/twentynewsgroups/pairs/pairslist.test";

    ContextMemoryReport contextReport;
    
    @Test
    public void testJavaTrainTest() throws Exception
    {

        runTrainTest();
        
        List<String> lines = FileUtils.readLines(contextReport.id2outcomeFiles.get(0), "utf-8");
        assertEquals(5, lines.size());

        assertEquals("#ID=PREDICTION;GOLDSTANDARD;THRESHOLD", lines.get(0));
        assertEquals(
                "#labels 0=n 1=y",
                lines.get(1));
        // 2nd line time stamp

        assertTrue(lines.get(3).matches("alt.atheism/51120.txt_alt.atheism/49960.txt=1;1;-1"));
        assertTrue(lines.get(4).matches("alt.atheism/51120.txt_alt.atheism/51060.txt=1;1;-1"));
        
        
    }

    private void runTrainTest() throws Exception
    {
        contextReport = new ContextMemoryReport();
        
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, getTrainReader());
        dimReaders.put(DIM_READER_TEST, getTestReader());

        Map<String, Object> weka = new HashMap<>();
        weka.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new WekaAdapter(), SMO.class.getName() });
        weka.put(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass());
        weka.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());

        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", weka);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
                TcFeatureFactory.create(DiffNrOfTokensPairFeatureExtractor.class)));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, Constants.FM_PAIR), dimFeatureSets, mlas);

        ExperimentTrainTest experiment = new ExperimentTrainTest(
                "ExternalResourceFeatureDemo");
        experiment.setPreprocessing(getPreprocessing());
        experiment.setParameterSpace(pSpace);
        experiment.addReport(BatchTrainTestReport.class);
        experiment.addReport(ContextMemoryReport.class);
        experiment.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        Lab.getInstance().run(experiment);        
    }

    public CollectionReaderDescription getTrainReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(PairTwentyNewsgroupsReader.class,
                PairTwentyNewsgroupsReader.PARAM_LISTFILE, listFilePathTrain,
                PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE, languageCode);
    }

    public CollectionReaderDescription getTestReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(PairTwentyNewsgroupsReader.class,
                PairTwentyNewsgroupsReader.PARAM_LISTFILE, listFilePathTest,
                PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE, languageCode);
    }

    public TcFeatureSet getFeatureSet()
    {

        // Create the External Resource here:
        ExternalResourceDescription gstResource = ExternalResourceFactory
                .createExternalResourceDescription(CosineSimilarityResource.class,
                        CosineSimilarityResource.PARAM_NORMALIZATION,
                        NormalizationMode.L2.toString());

        return new TcFeatureSet(TcFeatureFactory.create(SimilarityPairFeatureExtractor.class,
                SimilarityPairFeatureExtractor.PARAM_TEXT_SIMILARITY_RESOURCE, gstResource));
    }

    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
