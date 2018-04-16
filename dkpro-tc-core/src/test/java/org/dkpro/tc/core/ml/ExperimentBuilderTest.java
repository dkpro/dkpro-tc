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
package org.dkpro.tc.core.ml;

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
import org.dkpro.tc.core.ml.builder.ExperimentBuilder;
import org.dkpro.tc.core.ml.builder.FeatureMode;
import org.dkpro.tc.core.ml.builder.LearningMode;
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
        ExperimentBuilder builder = new ExperimentBuilder(LearningMode.SINGLE_LABEL, FeatureMode.DOCUMENT);
        builder.addFeatureSet(tcFeatureSet);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);

        ParameterSpace build = builder.build();

        Dimension<?>[] dimensions = build.getDimensions();
        assertEquals(5, dimensions.length);

        Set<String> names = getNames(dimensions);
        assertTrue(names.contains(DIM_READERS));
        assertTrue(names.contains(DIM_FEATURE_MODE));
        assertTrue(names.contains(DIM_LEARNING_MODE));
        assertTrue(names.contains(DIM_FEATURE_SET));
        assertTrue(names.contains(DIM_MLA_CONFIGURATIONS));
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
        ExperimentBuilder builder = new ExperimentBuilder(LearningMode.REGRESSION, FeatureMode.DOCUMENT);
        builder.addFeatureSet(tcFeatureSet);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);
        builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorMissingFeatureSet()
    {
        ExperimentBuilder builder = new ExperimentBuilder(LearningMode.SINGLE_LABEL, FeatureMode.UNIT);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);
        builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorEmptyFeatureSet()
    {
        ExperimentBuilder builder = new ExperimentBuilder(LearningMode.SINGLE_LABEL, FeatureMode.SEQUENCE);
        builder.addFeatureSet(new TcFeatureSet());
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.addAdapterConfiguration(adapter);
        builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorMissingMachineLearningAdapter()
    {
        ExperimentBuilder builder = new ExperimentBuilder(LearningMode.SINGLE_LABEL, FeatureMode.PAIR);
        builder.addFeatureSet(tcFeatureSet);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.build();

    }
    
    @Test(expected = NullPointerException.class)
    public void testErrorNullLearningMode()
    {
        ExperimentBuilder builder = new ExperimentBuilder(null, FeatureMode.PAIR);
        builder.addFeatureSet(tcFeatureSet);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.build();
    }
    
    @Test(expected = NullPointerException.class)
    public void testErrorNullFeatureMode()
    {
        ExperimentBuilder builder = new ExperimentBuilder(LearningMode.SINGLE_LABEL, null);
        builder.addFeatureSet(tcFeatureSet);
        builder.addReader(readerTrain, true);
        builder.addReader(readerTest, false);
        builder.build();
    }
}
