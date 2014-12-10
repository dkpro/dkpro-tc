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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.examples.model;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.serialization.SaveModelCRFSuiteTask;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.BrownCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.examples.util.DemoUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsUFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.ml.uima.TcAnnotatorSequence;

public class CRFSuiteSaveAndLoadModelTest
    implements Constants
{

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @After
    public void cleanUp(){
        folder.delete();
    }
    
    @Test
    public void saveModel()
        throws Exception
    {
        File modelFolder = folder.newFolder();
        ParameterSpace pSpace = getParameterSpace();
        executeSaveModelIntoTemporyFolder(pSpace, modelFolder);

        File classifierFile = new File(modelFolder.getAbsolutePath() + "/" + MODEL_CLASSIFIER);
        assertTrue(classifierFile.exists());
        
        File usedFeaturesFile = new File(modelFolder.getAbsolutePath() + "/" + MODEL_FEATURE_EXTRACTORS);
        assertTrue(usedFeaturesFile.exists());
        
        File modelMetaFile = new File(modelFolder.getAbsolutePath() + "/" + MODEL_META);
        assertTrue(modelMetaFile.exists());
        
        modelFolder.deleteOnExit();
    }

    private void executeSaveModelIntoTemporyFolder(ParameterSpace aPSpace, File aModelFolder)
        throws Exception
    {
        SaveModelCRFSuiteTask batch = new SaveModelCRFSuiteTask("TestSaveModel", aModelFolder,
                CRFSuiteAdapter.class, createEngineDescription(NoOpAnnotator.class));
        batch.setParameterSpace(aPSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        Lab.getInstance().run(batch);

    }

    @SuppressWarnings("unchecked")
    private ParameterSpace getParameterSpace()
    {
        DemoUtils.setDkproHome(this.getClass().getName());

        String trainFolder = "src/main/resources/data/brown_tei/";

        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, BrownCorpusReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS, Arrays.asList(
                BrownCorpusReader.PARAM_SOURCE_LOCATION, trainFolder,
                BrownCorpusReader.PARAM_LANGUAGE, "en", BrownCorpusReader.PARAM_PATTERNS, "*.xml"));

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] { NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 500,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));

        Dimension<List<String>> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                Arrays.asList(new String[] { NrOfCharsUFE.class.getName(), }));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, FM_SEQUENCE), dimPipelineParameters, dimFeatureSets);
        return pSpace;
    }
    
    @Test
    public void loadModel() throws Exception {
        
        File modelFolder = folder.newFolder();
        ParameterSpace pSpace = getParameterSpace();
        executeSaveModelIntoTemporyFolder(pSpace, modelFolder);
        
        SimplePipeline.runPipeline(
                CollectionReaderFactory.createReader(
                        StringReader.class,
                        StringReader.PARAM_DOCUMENT_TEXT, "This is an example text",
                        StringReader.PARAM_LANGUAGE, "en"
                ),
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
                AnalysisEngineFactory.createEngineDescription(
                        TcAnnotatorSequence.class,
                        TcAnnotatorSequence.PARAM_TC_MODEL_LOCATION, modelFolder.getAbsolutePath(),
                        TcAnnotatorSequence.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
                        TcAnnotatorSequence.PARAM_NAME_UNIT_ANNOTATION, Token.class.getName()
                )
        );
    }
}