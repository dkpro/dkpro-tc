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
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.ml.builder.ExperimentBuilder;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ExperimentBuilderTest
    implements Constants
{
    TcShallowLearningAdapter adapter;
    TcFeatureSet tcFeatureSet;
    CollectionReaderDescription readerTrain;
    CollectionReaderDescription readerTest;

    @Before
    public void setup()
    {
        adapter = Mockito.mock(TcShallowLearningAdapter.class);
        Mockito.when(adapter.getDataWriterClass()).thenReturn("some.string.value");
        Mockito.when(adapter.useSparseFeatures()).thenReturn(true);

        TcFeature featureMock = Mockito.mock(TcFeature.class);
        tcFeatureSet = new TcFeatureSet(featureMock);

        readerTrain = Mockito.mock(CollectionReaderDescription.class);
        readerTest = Mockito.mock(CollectionReaderDescription.class);
    }

    @Test
    public void testBuilder()
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.addFeatureSet(tcFeatureSet);
        builder.setLearningMode(LearningMode.SINGLE_LABEL);
        builder.setFeatureMode(FeatureMode.DOCUMENT);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);

        ParameterSpace build = builder.buildParameterSpace();

        Dimension<?>[] dimensions = build.getDimensions();
        assertEquals(5, dimensions.length);

        Set<String> names = getNames(dimensions);
        assertTrue(names.contains(DIM_READERS));
        assertTrue(names.contains(DIM_FEATURE_MODE));
        assertTrue(names.contains(DIM_LEARNING_MODE));
        assertTrue(names.contains(DIM_FEATURE_SET));
        assertTrue(names.contains(DIM_MLA_CONFIGURATIONS));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBuilderWithCustomUserDimensions()
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.addFeatureSet(tcFeatureSet);
        builder.setLearningMode(LearningMode.SINGLE_LABEL);
        builder.setFeatureMode(FeatureMode.DOCUMENT);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);
        builder.addAdditionalDimension(Dimension.create("ABC", String.class));

        ParameterSpace build = builder.buildParameterSpace();

        Dimension<?>[] dimensions = build.getDimensions();
        assertEquals(6, dimensions.length);

        Set<String> names = getNames(dimensions);
        assertTrue(names.contains(DIM_READERS));
        assertTrue(names.contains(DIM_FEATURE_MODE));
        assertTrue(names.contains(DIM_LEARNING_MODE));
        assertTrue(names.contains(DIM_FEATURE_SET));
        assertTrue(names.contains(DIM_MLA_CONFIGURATIONS));
        assertTrue(names.contains("ABC"));
    }

    private Set<String> getNames(Dimension<?>[] dimensions)
    {
        Set<String> names = new HashSet<>();
        for (Dimension<?> d : dimensions) {
            names.add(d.getName());
        }

        return names;
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorMissingReader()
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.setLearningMode(LearningMode.REGRESSION);
        builder.setFeatureMode(FeatureMode.DOCUMENT);
        builder.addFeatureSet(tcFeatureSet);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);
        builder.buildParameterSpace();
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorMissingFeatureSet()
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.setLearningMode(LearningMode.SINGLE_LABEL);
        builder.setFeatureMode(FeatureMode.UNIT);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);
        builder.buildParameterSpace();
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorEmptyFeatureSet()
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.setLearningMode(LearningMode.SINGLE_LABEL);
        builder.setFeatureMode(FeatureMode.SEQUENCE);
        builder.addFeatureSet(new TcFeatureSet());
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);
        builder.buildParameterSpace();
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorMissingMachineLearningAdapter()
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.setLearningMode(LearningMode.SINGLE_LABEL);
        builder.setFeatureMode(FeatureMode.PAIR);
        builder.addFeatureSet(tcFeatureSet);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.buildParameterSpace();

    }
    
    @Test(expected = NullPointerException.class)
    public void testErrorNullLearningMode()
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.setFeatureMode(FeatureMode.PAIR);
        builder.addFeatureSet(tcFeatureSet);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);
        builder.buildParameterSpace();
    }
    
    @Test(expected = NullPointerException.class)
    public void testErrorNullFeatureMode()
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        builder.setLearningMode(LearningMode.SINGLE_LABEL);
        builder.addFeatureSet(tcFeatureSet);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);
        builder.buildParameterSpace();
    }
}
