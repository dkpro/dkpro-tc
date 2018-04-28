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
package org.dkpro.tc.ml.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ExperimentBuilderTest
{
    ExperimentBuilder builder;
    
    @Before
    public void setup() {
        builder = new ExperimentBuilder();
    }
    
    @Test
    public void experimentTypeCV() {
        builder.experiment(ExperimentType.CROSS_VALIDATION, "cv");
        assertEquals(builder.type.toString(), ExperimentType.CROSS_VALIDATION.toString());
    }
    
    @Test
    public void experimentTypeTrainTest() {
        builder.experiment(ExperimentType.TRAIN_TEST, "trainTest");
        assertEquals(builder.type.toString(), ExperimentType.TRAIN_TEST.toString());
    }
    
    @Test
    public void experimentSaveModelTest() {
        builder.experiment(ExperimentType.SAVE_MODEL, "saveModel");
        assertEquals(builder.type.toString(), ExperimentType.SAVE_MODEL.toString());
    }
    
    @Test
    public void outputFolder() {
        builder.outputFolder("target/");
        assertTrue(builder.outputFolder !=null);
    }
    
    @Test
    public void numFolds() {
        builder.numFolds(23);
        assertEquals(23, builder.numFolds);
    }
    
    @Test
    public void tcFeatureSet() {
        
        TcFeature mockFeature = Mockito.mock(TcFeature.class);
        
        TcFeatureSet featureSet1 = new TcFeatureSet(mockFeature);
        TcFeatureSet featureSet2 = new TcFeatureSet(mockFeature);
        TcFeatureSet featureSet3 = new TcFeatureSet(mockFeature);
        
        builder.featureSets(featureSet1,featureSet2,featureSet3);
        assertEquals(builder.featureSets.get(0), featureSet1);
        assertEquals(builder.featureSets.get(1), featureSet2);
        assertEquals(builder.featureSets.get(2), featureSet3);
        
    }
    
    @Test
    public void tcFeatures() {
        
        TcFeature mockFeature = Mockito.mock(TcFeature.class);
        
        builder.features(mockFeature);
        assertEquals(1, builder.featureSets.size());
        assertEquals(mockFeature, builder.featureSets.get(0).get(0));
        
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
    
    @Test
    public void preprocessing() throws ResourceInitializationException {
        builder.preprocessing(AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class));
        assertTrue(builder.preprocessing != null);
    }
    
    @Test
    public void machineLearningBackend() {
        
        TcShallowLearningAdapter adapterMock = Mockito.mock(TcShallowLearningAdapter.class);
        MLBackend mlBackend = new MLBackend(adapterMock);
        
        builder.machineLearningBackend(mlBackend);
        assertEquals(builder.adapter.get(0), adapterMock);
        
    }
    
    @Test
    public void parameterSpace() throws Exception {
        
        CollectionReaderDescription readerMock = Mockito.mock(CollectionReaderDescription.class);
        
        TcShallowLearningAdapter adapterMock = Mockito.mock(TcShallowLearningAdapter.class);
        MLBackend mlBackend = new MLBackend(adapterMock);
        
        TcFeature featureMock = Mockito.mock(TcFeature.class);
        TcFeatureSet set = new TcFeatureSet(featureMock);
        
        ParameterSpace parameterSpace = builder.dataReaderTrain(readerMock)
               .dataReaderTest(readerMock)
               .featureMode(FeatureMode.DOCUMENT)
               .learningMode(LearningMode.REGRESSION)
               .featureSets(set)
               .numFolds(3)
               .machineLearningBackend(mlBackend)
               .getParameterSpace();
        
        Set<String> names = new HashSet<>();
        for(@SuppressWarnings("rawtypes") Dimension d : parameterSpace.getDimensions()) {
            names.add(d.getName());
        }
        
        
        assertEquals(5, parameterSpace.getDimensions().length);
        
        assertTrue(names.contains(Constants.DIM_LEARNING_MODE));
        assertTrue(names.contains(Constants.DIM_FEATURE_MODE));
        assertTrue(names.contains(Constants.DIM_FEATURE_SET));
        assertTrue(names.contains(Constants.DIM_READERS));
        assertTrue(names.contains(Constants.DIM_MLA_CONFIGURATIONS));
        
    }
    
    @Test(expected=IllegalStateException.class)
    public void missingExperiment() throws Exception {
        CollectionReaderDescription readerMock = Mockito.mock(CollectionReaderDescription.class);
        
        TcShallowLearningAdapter adapterMock = Mockito.mock(TcShallowLearningAdapter.class);
        MLBackend mlBackend = new MLBackend(adapterMock);
        
        TcFeature featureMock = Mockito.mock(TcFeature.class);
        TcFeatureSet set = new TcFeatureSet(featureMock);
        
        builder.dataReaderTrain(readerMock)
               .dataReaderTest(readerMock)
               .featureMode(FeatureMode.DOCUMENT)
               .learningMode(LearningMode.REGRESSION)
               .featureSets(set)
               .numFolds(3)
               .machineLearningBackend(mlBackend)
               .build();
    }
    
    @Test(expected=IllegalStateException.class)
    public void missingOutputFolderModelSaving() throws Exception {
        CollectionReaderDescription readerMock = Mockito.mock(CollectionReaderDescription.class);
        
        TcShallowLearningAdapter adapterMock = Mockito.mock(TcShallowLearningAdapter.class);
        MLBackend mlBackend = new MLBackend(adapterMock);
        
        TcFeature featureMock = Mockito.mock(TcFeature.class);
        TcFeatureSet set = new TcFeatureSet(featureMock);
        
        builder.experiment(ExperimentType.SAVE_MODEL, "saveModel")
               .dataReaderTrain(readerMock)
               .featureMode(FeatureMode.DOCUMENT)
               .learningMode(LearningMode.REGRESSION)
               .featureSets(set)
               .machineLearningBackend(mlBackend)
               .build();
    }
    
    @Test
    public void validSetupTrainTest() throws Exception {
        CollectionReaderDescription readerMock = Mockito.mock(CollectionReaderDescription.class);
        
        TcShallowLearningAdapter adapterMock = Mockito.mock(TcShallowLearningAdapter.class);
        MLBackend mlBackend = new MLBackend(adapterMock);
        
        TcFeature featureMock = Mockito.mock(TcFeature.class);
        TcFeatureSet set = new TcFeatureSet(featureMock);
        
        builder.experiment(ExperimentType.TRAIN_TEST, "test")
               .dataReaderTrain(readerMock)
               .dataReaderTest(readerMock)
               .featureMode(FeatureMode.DOCUMENT)
               .learningMode(LearningMode.REGRESSION)
               .featureSets(set)
               .machineLearningBackend(mlBackend)
               .build();
    }
    
    @Test
    public void validSetupCrossValidation() throws Exception {
        CollectionReaderDescription readerMock = Mockito.mock(CollectionReaderDescription.class);
        
        TcShallowLearningAdapter adapterMock = Mockito.mock(TcShallowLearningAdapter.class);
        MLBackend mlBackend = new MLBackend(adapterMock);
        
        TcFeature featureMock = Mockito.mock(TcFeature.class);
        TcFeatureSet set = new TcFeatureSet(featureMock);
        
        builder.experiment(ExperimentType.CROSS_VALIDATION, "test")
               .dataReaderTrain(readerMock)
               .dataReaderTest(readerMock)
               .featureMode(FeatureMode.DOCUMENT)
               .learningMode(LearningMode.REGRESSION)
               .featureSets(set)
               .machineLearningBackend(mlBackend)
               .build();
    }
}
