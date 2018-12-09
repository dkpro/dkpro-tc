/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.ml.experiment.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.dkpro.tc.ml.experiment.builder.ExperimentType.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.ExperimentLearningCurve;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DeepExperimentBuilderTest implements Constants, DeepLearningConstants
{
    DeepExperimentBuilder builder;
    
    @Before
    public void setup() {
        builder = new DeepExperimentBuilder();
    }
    
    @Test
    public void preConfiguredExperimentType() {
    	ExperimentLearningCurve preConfigured = new ExperimentLearningCurve();
    	builder.experiment(preConfigured);
    	assertEquals(preConfigured, builder.experiment);
    }
    
    @Test
    public void experimentTypeCV() {
        builder.experiment(CROSS_VALIDATION, "cv");
        assertEquals(builder.type.toString(), CROSS_VALIDATION.toString());
    }
    
    @Test
    public void experimentTypeLearningCurve() {
        builder.experiment(LEARNING_CURVE, "cv_learningCurve");
        assertEquals(builder.type.toString(), LEARNING_CURVE.toString());
    }
    
    @Test
    public void experimentTypeTrainTest() {
        builder.experiment(TRAIN_TEST, "trainTest");
        assertEquals(builder.type.toString(), TRAIN_TEST.toString());
    }
    
    @Test
    public void experimentTypeLearningCurvedFixedTest() {
        builder.experiment(LEARNING_CURVE_FIXED_TEST_SET, "fixedTest");
        assertEquals(builder.type.toString(), LEARNING_CURVE_FIXED_TEST_SET.toString());
    }
    
    @Test
    public void experimentSaveModelTest() {
        builder.experiment(SAVE_MODEL, "saveModel");
        assertEquals(builder.type.toString(), SAVE_MODEL.toString());
    }
    
    @Test
    public void experimentName() {
        builder.name("ABC");
        assertEquals("ABC", builder.experimentName);
    }
    
    @Test
    public void numFolds() {
        builder.numFolds(23);
        assertEquals(23, builder.numFolds);
    }
    
    @Test
    public void learningMode() {
        builder.learningMode(LearningMode.REGRESSION);
        assertEquals(builder.learningMode.toString(), LearningMode.REGRESSION.toString());
    }
    
    @Test
    public void featureMode() {
        builder.featureMode(FeatureMode.DOCUMENT);
        assertEquals(builder.featureMode.toString(), FeatureMode.DOCUMENT.toString());
    }
    
    @Test(expected=NullPointerException.class)
    public void featureModeNullPointer() {
        builder.featureMode(null);
    }
    
    @Test
    public void preprocessing() throws ResourceInitializationException {
        builder.preprocessing(AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class));
        assertTrue(builder.preprocessing != null);
    }
    
    @Test
    public void maximumLength() {
        builder.maximumLength(15);
        assertEquals(builder.maximumLength, 15);
    }
    
//    @Test
//    public void machineLearningBackend() {
//        
//        TcShallowLearningAdapter adapterMock = Mockito.mock(TcShallowLearningAdapter.class);
//        MLBackend mlBackend = new MLBackend(adapterMock);
//        
//        builder.machineLearningBackend(mlBackend);
//        assertEquals(builder.backends.get(0), adapterMock);
//        
//    }
    
    @Test
    public void parameterSpace() throws Exception {
        
        CollectionReaderDescription readerMock = Mockito.mock(CollectionReaderDescription.class);
        
        TcDeepLearningAdapter adapterMock = Mockito.mock(TcDeepLearningAdapter.class);
        MLBackend mlBackend = new MLBackend(adapterMock, new Object());
        
        ParameterSpace parameterSpace = builder
               .dataReaderTrain(readerMock)
               .dataReaderTest(readerMock)
               .pythonPath("dummy")
               .featureMode(FeatureMode.DOCUMENT)
               .learningMode(LearningMode.REGRESSION)
               .numFolds(3)
               .machineLearningBackend(mlBackend)
               .getParameterSpace();
        
        Set<String> names = new HashSet<>();
        for(@SuppressWarnings("rawtypes") Dimension d : parameterSpace.getDimensions()) {
            names.add(d.getName());
        }
        
        
        assertEquals(7, parameterSpace.getDimensions().length);
        
        assertTrue(names.contains(DIM_MAXIMUM_LENGTH));
        assertTrue(names.contains(DIM_PYTHON_INSTALLATION));
        assertTrue(names.contains(DIM_VECTORIZE_TO_INTEGER));
        assertTrue(names.contains(DIM_LEARNING_MODE));
        assertTrue(names.contains(DIM_FEATURE_MODE));
        assertTrue(names.contains(DIM_READERS));
        assertTrue(names.contains(DIM_MLA_CONFIGURATIONS));
        
    }
    
    @Test(expected=IllegalStateException.class)
    public void missingExperiment() throws Exception {
        CollectionReaderDescription readerMock = Mockito.mock(CollectionReaderDescription.class);
        
        TcDeepLearningAdapter adapterMock = Mockito.mock(TcDeepLearningAdapter.class);
        MLBackend mlBackend = new MLBackend(adapterMock, new Object());
        
        builder.dataReaderTrain(readerMock)
               .dataReaderTest(readerMock)
               .featureMode(FeatureMode.DOCUMENT)
               .learningMode(LearningMode.REGRESSION)
               .numFolds(3)
               .machineLearningBackend(mlBackend)
               .build();
    }
    
    @Test
    public void validSetupTrainTest() throws Exception {
        CollectionReaderDescription readerMock = Mockito.mock(CollectionReaderDescription.class);
        
        TcDeepLearningAdapter adapterMock = Mockito.mock(TcDeepLearningAdapter.class);
        MLBackend mlBackend = new MLBackend(adapterMock, new Object());
        
        builder.experiment(ExperimentType.TRAIN_TEST, "test")
               .dataReaderTrain(readerMock)
               .dataReaderTest(readerMock)
               .pythonPath("dummy")
               .featureMode(FeatureMode.DOCUMENT)
               .learningMode(LearningMode.REGRESSION)
               .machineLearningBackend(mlBackend)
               .preprocessing(AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class))
               .build();
    }
    
    @Test
    public void validSetupCrossValidation() throws Exception {
        CollectionReaderDescription readerMock = Mockito.mock(CollectionReaderDescription.class);
        
        TcDeepLearningAdapter adapterMock = Mockito.mock(TcDeepLearningAdapter.class);
        MLBackend mlBackend = new MLBackend(adapterMock, new Object());
        
        builder.experiment(ExperimentType.CROSS_VALIDATION, "test")
               .dataReaderTrain(readerMock)
               .dataReaderTest(readerMock)
               .pythonPath("dummy")
               .featureMode(FeatureMode.DOCUMENT)
               .learningMode(LearningMode.REGRESSION)
               .machineLearningBackend(mlBackend)
               .build();
    }
}
