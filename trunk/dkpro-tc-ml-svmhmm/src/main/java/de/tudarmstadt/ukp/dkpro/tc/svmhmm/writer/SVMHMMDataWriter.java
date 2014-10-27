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

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SparseFeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.SVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.OriginalTokenHolderFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.SVMHMMUtils;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Converts features to the internal format for SVM HMM
 *
 * @author Ivan Habernal
 */
public class SVMHMMDataWriter
        implements DataWriter
{

    private static final double EPS = 0.00000000001;

    static Log log = LogFactory.getLog(SVMHMMDataWriter.class);

    @Override
    public void write(File aOutputDirectory, FeatureStore featureStore, boolean aUseDenseInstances,
            String aLearningMode)
            throws Exception
    {
        // map features to feature numbers
        BidiMap featureNameToFeatureNumberMapping = SVMHMMUtils.mapVocabularyToIntegers(
                featureStore.getFeatureNames());

        // prepare output file
        File outputFile = new File(aOutputDirectory, new SVMHMMAdapter().getFrameworkFilename(
                TCMachineLearningAdapter.AdapterNameEntries.featureVectorsFile));

        BufferedWriter bf = new BufferedWriter(new FileWriter(outputFile));
        PrintWriter pw = new PrintWriter(bf);

        log.info("Start writing features to file " + outputFile.getAbsolutePath());

        log.debug("Total instances: " + featureStore.getNumberOfInstances());
        log.debug("Feature vector size: " + featureStore.getFeatureNames().size());

        if (featureStore instanceof SparseFeatureStore) {
            SparseFeatureStore sparseFeatureStore = (SparseFeatureStore) featureStore;
            log.debug("Non-null feature sparsity ratio: " + sparseFeatureStore
                    .getFeatureSparsityRatio());
        }

        for (int i = 0; i < featureStore.getNumberOfInstances(); i++) {
            Instance instance;

            // getInstance() is time-consuming for dense features
            // check whether can we use only sparse features
            if (featureStore instanceof SparseFeatureStore) {
                instance = ((SparseFeatureStore) featureStore).getInstanceSparseFeatures(i);
            }
            else {
                instance = featureStore.getInstance(i);
            }

            // placeholder for original token
            String originalToken = null;

            // feature values
            SortedMap<Integer, Number> featureValues = new TreeMap<>();
            for (Feature f : instance.getFeatures()) {
                String featureName = f.getName();
                Object featureValue = f.getValue();

                // we ignore null feature values
                if (featureValue == null) {
                    continue;
                }

                // get original token stored in OriginalToken feature
                if (OriginalTokenHolderFeatureExtractor.ORIGINAL_TOKEN.equals(featureName)) {
                    // if original token/text was multi line, join it to a single line
                    originalToken = ((String) featureValue).replaceAll("\\n", " ");
                    continue;
                }

                // not allow other non-number features
                if (!(featureValue instanceof Number)) {
                    log.warn("Only features with number values are allowed, but was " + f);
                    continue;
                }

                // in case the feature store produced dense feature vector with zeros for
                // non-present features, we ignore zero value features here
                Number featureValueNumber = (Number) featureValue;
                if (Math.abs(featureValueNumber.doubleValue() - 0d) < EPS) {
                    continue;
                }

                // get number and int value of the feature
                Integer featureNumber = (Integer) featureNameToFeatureNumberMapping
                        .get(featureName);

                featureValues.put(featureNumber, featureValueNumber);
            }

            // print formatted output: label name and sequence id
            pw.printf(Locale.ENGLISH, "%s qid:%d ", instance.getOutcome(),
                    instance.getSequenceId());

            // print sorted features
            for (Map.Entry<Integer, Number> entry : featureValues.entrySet()) {
                if (entry.getValue() instanceof Double) {
                    // format double on 4 decimal places
                    pw.printf(Locale.ENGLISH, "%d:%f.4 ", entry.getKey(),
                            entry.getValue().doubleValue());
                }
                else {
                    // format as integer
                    pw.printf(Locale.ENGLISH, "%d:%d ", entry.getKey(),
                            entry.getValue().intValue());
                }
            }

            // print original token and label as comment
            pw.printf(Locale.ENGLISH, "# %s %s %d%n", originalToken, instance.getOutcome(),
                    instance.getSequenceId());
        }

        IOUtils.closeQuietly(pw);

        log.info("Finished writing features to file " + outputFile.getAbsolutePath());
    }
}
