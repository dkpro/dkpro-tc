/*
 * Copyright 2017
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

package org.dkpro.tc.ml.svmhmm.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.fstore.simple.SparseFeatureStore;
import org.dkpro.tc.ml.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.ml.svmhmm.util.OriginalTextHolderFeatureExtractor;
import org.dkpro.tc.ml.svmhmm.util.SVMHMMUtils;

/**
 * Converts features to the internal format for SVM HMM
 */
public class SVMHMMDataWriter
    implements DataWriter
{

    // random prefix for all meta-data features
    public static final String META_DATA_FEATURE_PREFIX = "ed64ffc5d412c3d3430e0d42d6a668110d1ce8ee";

    private static final double EPS = 0.00000000001;

    static Log log = LogFactory.getLog(SVMHMMDataWriter.class);

    // a consecutive single number counter to identify a sequence over all CAS
    Map<String, Integer> uniqueId = new HashMap<String, Integer>();
    int consequtiveUniqueDocSeqId = 0;

    @Override
    public void write(File aOutputDirectory, FeatureStore featureStore, boolean aUseDenseInstances,
            String aLearningMode, boolean applyWeighting)
                throws Exception
    {
        // map features to feature numbers
        BidiMap featureNameToFeatureNumberMapping = SVMHMMUtils
                .mapVocabularyToIntegers(featureStore.getFeatureNames());

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
            log.debug("Non-null feature sparsity ratio: "
                    + sparseFeatureStore.getFeatureSparsityRatio());
        }

        for (int i = 0; i < featureStore.getNumberOfInstances(); i++) {
            Instance instance;

            instance = featureStore.getInstance(i);

            // placeholder for original token
            String originalToken = null;

            // other "features" - meta data features that will be stored in the comment
            SortedMap<String, String> metaDataFeatures = new TreeMap<>();

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
                if (OriginalTextHolderFeatureExtractor.ORIGINAL_TEXT.equals(featureName)) {
                    // if original token/text was multi line, join it to a single line
                    // originalToken = ((String) featureValue).replaceAll("\\n", " ");
                    originalToken = (String) featureValue;
                    continue;
                }

                // handle other possible features as metadata?
                if (isMetaDataFeature(featureName)) {
                    metaDataFeatures.put(featureName, (String) featureValue);
                    continue;
                }

                // not allow other non-number features
                if (!(featureValue instanceof Number)) {
                    log.debug("Only features with number values are allowed, but was " + f);
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
                    getUniqueSequenceId(instance));

            // print sorted features
            for (Map.Entry<Integer, Number> entry : featureValues.entrySet()) {
                if (entry.getValue() instanceof Double) {
                    // format double on 8 decimal places
                    pw.printf(Locale.ENGLISH, "%d:%.8f ", entry.getKey(),
                            entry.getValue().doubleValue());
                }
                else {
                    // format as integer
                    pw.printf(Locale.ENGLISH, "%d:%d ", entry.getKey(),
                            entry.getValue().intValue());
                }
            }

            // print original token and label as comment
            pw.printf(Locale.ENGLISH, "# %s %d %s ", instance.getOutcome(),
                    instance.getSequenceId(),
                    (originalToken != null) ? (URLEncoder.encode(originalToken, "utf-8")) : "");

            // print meta-data features at the end
            for (Map.Entry<String, String> entry : metaDataFeatures.entrySet()) {
                pw.printf(" %s:%s", URLEncoder.encode(entry.getKey(), "utf-8"),
                        URLEncoder.encode(entry.getValue(), "utf-8"));
            }
            // new line at the end
            pw.println();
        }

        IOUtils.closeQuietly(pw);

        // writing feature mapping
        File mappingFile = new File(aOutputDirectory, "featuresIntToNames_forDebug.txt");
        SVMHMMUtils.saveMappingTextFormat(featureNameToFeatureNumberMapping, mappingFile);

        log.info("Finished writing features to file " + outputFile.getAbsolutePath());
    }

    private Integer getUniqueSequenceId(Instance instance)
    {
        String key = instance.getJcasId() + "-" + instance.getSequenceId();
        Integer consecSeqId = uniqueId.get(key);
        if (consecSeqId == null) {
            consecSeqId = consequtiveUniqueDocSeqId++;
            uniqueId.put(key, consecSeqId);
        }
        return consecSeqId;
    }

    protected boolean isMetaDataFeature(String featureName)
    {
        return featureName.startsWith(META_DATA_FEATURE_PREFIX);
    }
}
