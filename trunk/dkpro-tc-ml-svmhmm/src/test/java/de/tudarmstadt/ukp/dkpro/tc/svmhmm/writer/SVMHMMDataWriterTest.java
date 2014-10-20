/*
 * Copyright 2014
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
 */

package de.tudarmstadt.ukp.dkpro.tc.svmhmm.writer;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SparseFeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.OriginalTokenHolderFeatureExtractor;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class SVMHMMDataWriterTest
{
    private static final int TESTING_INSTANCES = 50000;
    private static final int TESTING_FEATURES_PER_INSTANCE = 50;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Random random = new Random(System.currentTimeMillis());

    private FeatureStore featureStore;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    @Before
    public void setUp()
            throws Exception
    {
        Set<String> randomFeatureNames = new HashSet<>();
        int maxFeatureVectorSize = 100000;
        for (int i = 0; i < maxFeatureVectorSize; i++) {
            randomFeatureNames.add(String.valueOf(i));
        }
        List<String> allFeatureNames = new ArrayList<>(randomFeatureNames);

        featureStore = new SparseFeatureStore();

        // add 100.000 instances
        for (int i = 0; i < TESTING_INSTANCES; i++) {
            Instance instance = new Instance();
            instance.setOutcomes("outcome");

            // add 10 random features
            int offset = random.nextInt(maxFeatureVectorSize - TESTING_FEATURES_PER_INSTANCE);
            List<String> featureNames = allFeatureNames.subList(offset, offset + TESTING_FEATURES_PER_INSTANCE);

            for (String featureName : featureNames) {
                instance.addFeature(new Feature(featureName, 1));
            }

            instance.addFeature(
                    new Feature(OriginalTokenHolderFeatureExtractor.ORIGINAL_TOKEN, "token"));

            featureStore.addInstance(instance);
        }
    }

    @Test
    public void testWrite()
            throws Exception
    {
        SVMHMMDataWriter svmhmmDataWriter = new SVMHMMDataWriter();
        System.out.println(featureStore.size());
        svmhmmDataWriter.write(temporaryFolder.getRoot(), featureStore, false, null);

        List<String> lines = IOUtils.readLines(
                new FileInputStream(new File(temporaryFolder.getRoot(), "feature-vectors.txt")));
        System.out.println(lines.subList(0, 5));
    }
}