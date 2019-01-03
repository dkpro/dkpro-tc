/*******************************************************************************
 * Copyright 2019
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

package org.dkpro.tc.ml.crfsuite.writer;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CrfSuiteDataWriterTest
{

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    CrfSuiteDataWriter writer;
    File outputDirectory;
    List<Instance> instances;
    List<String> outcomes;

    @Before
    public void setUp() throws Exception
    {
        outputDirectory = folder.newFolder();
        writer = new CrfSuiteDataWriter();
        outcomes = new ArrayList<>();
        outcomes.add("1");
        outcomes.add("2");
        outcomes.add("3");
        outcomes.add("4");
        writer.init(outputDirectory, true, Constants.LM_SINGLE_LABEL, Constants.FM_SEQUENCE, false,
                outcomes.toArray(new String[0]));

        instances = new ArrayList<>();
        prepareFeatures();
    }

    private void prepareFeatures() throws Exception
    {
        List<Feature> features1 = new ArrayList<Feature>();
        features1.add(new Feature("feature1", 1.0, FeatureType.NUMERIC));
        features1.add(new Feature("feature2", 0.0, FeatureType.NUMERIC));
        features1.add(new Feature("feature3", "Water", FeatureType.STRING));

        List<Feature> features2 = new ArrayList<Feature>();
        features2.add(new Feature("feature2", 0.5, FeatureType.NUMERIC));
        features2.add(new Feature("feature1", 0.5, FeatureType.NUMERIC));
        features2.add(new Feature("feature3", "Fanta", FeatureType.STRING));

        Instance instance1 = new Instance(features1, "1");
        instance1.setSequenceId(0);
        instance1.setSequencePosition(0);
        Instance instance2 = new Instance(features2, "2");
        instance2.setSequenceId(0);
        instance2.setSequencePosition(1);
        Instance instance3 = new Instance(features1, "3");
        instance3.setSequenceId(0);
        instance3.setSequencePosition(2);

        Instance instance4 = new Instance(features1, "4");
        instance4.setSequenceId(1);
        instance4.setSequencePosition(0);
        Instance instance5 = new Instance(features2, "4");
        instance5.setSequenceId(1);
        instance5.setSequencePosition(1);

        instances.add(instance1);
        instances.add(instance2);
        instances.add(instance3);
        instances.add(instance4);
        instances.add(instance5);
    }

    @Test
    public void dataWriterTest() throws Exception
    {
        writeFeaturesWithDataWriter();
        List<String> fileContent = readData();

        assertEquals(7, fileContent.size());
        assertEquals("1\tfeature1=1.0\tfeature2=0.0\tfeature3=Water\t__BOS__", fileContent.get(0));
        assertEquals("2\tfeature1=0.5\tfeature2=0.5\tfeature3=Fanta", fileContent.get(1));
        assertEquals("3\tfeature1=1.0\tfeature2=0.0\tfeature3=Water\t__EOS__", fileContent.get(2));
        assertEquals("", fileContent.get(3));
        assertEquals("4\tfeature1=1.0\tfeature2=0.0\tfeature3=Water\t__BOS__", fileContent.get(4));
        assertEquals("4\tfeature1=0.5\tfeature2=0.5\tfeature3=Fanta\t__EOS__", fileContent.get(5));
        assertEquals("", fileContent.get(6));

    }

    private List<String> readData() throws Exception
    {
        File outputFile = new File(outputDirectory, Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);
        BufferedReader br = new BufferedReader(new FileReader(outputFile));

        List<String> lines = new ArrayList<String>();
        String currentLine = null;
        while ((currentLine = br.readLine()) != null) {
            lines.add(currentLine);
        }
        br.close();
        return lines;
    }

    private void writeFeaturesWithDataWriter() throws Exception
    {
        writer.init(outputDirectory, true, Constants.LM_SINGLE_LABEL, Constants.FM_SEQUENCE, false,
                outcomes.toArray(new String[0]));
        writer.writeClassifierFormat(instances);
    }
}
