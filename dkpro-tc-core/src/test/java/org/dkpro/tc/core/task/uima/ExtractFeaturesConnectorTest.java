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
package org.dkpro.tc.core.task.uima;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ExternalResourceDescription;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.feature.NoopFeatureExtractor;
import org.dkpro.tc.core.feature.UnitContextMetaCollector;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.io.TestReaderMultiLabel;
import org.dkpro.tc.core.io.TestReaderRegression;
import org.dkpro.tc.core.io.TestReaderSingleLabel;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.fstore.simple.DenseFeatureStore;
import org.dkpro.tc.fstore.simple.SparseFeatureStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

@RunWith(Parameterized.class)
public class ExtractFeaturesConnectorTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(
                new Object[][] { { DenseFeatureStore.class }, { SparseFeatureStore.class } });
    }

    private Class<? extends FeatureStore> featureStoreClass;

    public ExtractFeaturesConnectorTest(Class<? extends FeatureStore> featureStoreClass)
    {
        this.featureStoreClass = featureStoreClass;
    }

    @Test
    public void extractFeaturesConnectorSingleLabelTest()
        throws Exception
    {

        File outputPath = folder.newFolder();

        // we do not need parameters here, but in case we do :)
        Object[] parameters = new Object[] { NoopFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME,
                "123"
        };

        ExternalResourceDescription featureExtractor = ExternalResourceFactory
                .createExternalResourceDescription(NoopFeatureExtractor.class, parameters);
        List<ExternalResourceDescription> fes = new ArrayList<>();
        fes.add(featureExtractor);


        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
                "src/test/resources/data/*.txt");

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription doc = AnalysisEngineFactory.createEngineDescription(
                DocumentModeAnnotator.class,
                DocumentModeAnnotator.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT);

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_REGRESSION, Constants.FM_DOCUMENT, featureStoreClass.getName(),
                false, false, false, new ArrayList<>(), false, fes);

        SimplePipeline.runPipeline(reader, segmenter, doc, featExtractorConnector);

        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(
                FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)),
                featureStoreClass);
        assertEquals(2, fs.getNumberOfInstances());
        assertEquals(1, fs.getUniqueOutcomes().size());

        System.out.println(
                FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)));
    }

    @Test
    public void extractFeaturesConnectorMultiLabelTest()
        throws Exception
    {

        File outputPath = folder.newFolder();

        // we do not need parameters here, but in case we do :)
        Object[] parameters = new Object[] { NoopFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME,
                "123"
        };

        ExternalResourceDescription featureExtractor = ExternalResourceFactory
                .createExternalResourceDescription(NoopFeatureExtractor.class, parameters);
        List<ExternalResourceDescription> fes = new ArrayList<>();
        fes.add(featureExtractor);

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderMultiLabel.class, TestReaderMultiLabel.PARAM_SOURCE_LOCATION,
                "src/test/resources/data/*.txt");

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);
        
        AnalysisEngineDescription doc = AnalysisEngineFactory.createEngineDescription(
                DocumentModeAnnotator.class,
                DocumentModeAnnotator.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT);

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_REGRESSION, Constants.FM_DOCUMENT, featureStoreClass.getName(),
                false, false, false, new ArrayList<>(), false, fes);

        SimplePipeline.runPipeline(reader, segmenter, doc, featExtractorConnector);

        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(
                FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)),
                featureStoreClass);
        assertEquals(2, fs.getNumberOfInstances());
        assertEquals(3, fs.getUniqueOutcomes().size());

        System.out.println(
                FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)));
    }

    @Test
    public void extractFeaturesConnectorRegressionTest()
        throws Exception
    {

        File outputPath = folder.newFolder();

        // we do not need parameters here, but in case we do :)
        Object[] parameters = new Object[] { NoopFeatureExtractor.PARAM_UNIQUE_EXTRACTOR_NAME,
                "123",
                UnitContextMetaCollector.PARAM_CONTEXT_FILE,
                Constants.ID_CONTEXT_KEY
        };

        ExternalResourceDescription featureExtractor = ExternalResourceFactory
                .createExternalResourceDescription(NoopFeatureExtractor.class, parameters);
        List<ExternalResourceDescription> fes = new ArrayList<>();
        fes.add(featureExtractor);

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderRegression.class, TestReaderRegression.PARAM_SOURCE_LOCATION,
                "src/test/resources/data/*.txt");

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription doc = AnalysisEngineFactory.createEngineDescription(
                DocumentModeAnnotator.class, DocumentModeAnnotator.PARAM_FEATURE_MODE,
                Constants.FM_DOCUMENT);

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_REGRESSION, Constants.FM_DOCUMENT, featureStoreClass.getName(),
                false, false, false, new ArrayList<>(), false, fes);

        SimplePipeline.runPipeline(reader, segmenter, doc, featExtractorConnector);

        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(
                FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)),
                featureStoreClass);
        assertEquals(2, fs.getNumberOfInstances());
        assertEquals(1, fs.getUniqueOutcomes().size());
        assertEquals("0.45", fs.getUniqueOutcomes().first());

        System.out.println(
                FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)));
    }
}
