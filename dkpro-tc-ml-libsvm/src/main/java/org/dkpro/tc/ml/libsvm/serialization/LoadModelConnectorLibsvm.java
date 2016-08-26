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

package org.dkpro.tc.ml.libsvm.serialization;

import static org.dkpro.tc.core.Constants.MODEL_CLASSIFIER;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.util.SaveModelUtils;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.libsvm.api.LibsvmPredict;
import org.dkpro.tc.ml.uima.TcAnnotator;

import libsvm.svm;
import libsvm.svm_model;

public class LoadModelConnectorLibsvm
    extends ModelSerialization_ImplBase
{

    private static final String OUTCOME_PLACEHOLDER = "-1";

    @ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
    private File tcModelLocation;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    @ConfigurationParameter(name = PARAM_FEATURE_STORE_CLASS, mandatory = true)
    private String featureStoreImpl;

    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
    private String featureMode;

    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
    private String learningMode;

    private svm_model model;

    private Map<String, String> integer2OutcomeMapping;
    private Map<String, Integer> featName2id;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            model = svm
                    .svm_load_model(new File(tcModelLocation, MODEL_CLASSIFIER).getAbsolutePath());
            integer2OutcomeMapping = loadInteger2OutcomeMapping(tcModelLocation);
            featName2id = loadFeatureName2IntegerMapping(tcModelLocation);
            SaveModelUtils.verifyTcVersion(tcModelLocation, getClass());
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    private Map<String, String> loadInteger2OutcomeMapping(File tcModelLocation)
        throws IOException
    {
        Map<String, String> map = new HashMap<>();
        List<String> readLines = FileUtils
                .readLines(new File(tcModelLocation, LibsvmAdapter.getOutcomeMappingFilename()));
        for (String l : readLines) {
            String[] split = l.split("\t");
            map.put(split[1], split[0]);
        }
        return map;
    }

    private Map<String, Integer> loadFeatureName2IntegerMapping(File tcModelLocation)
        throws IOException
    {
        Map<String, Integer> map = new HashMap<>();
        List<String> readLines = FileUtils.readLines(
                new File(tcModelLocation, LibsvmAdapter.getFeaturenameMappingFilename()));
        for (String l : readLines) {
            String[] split = l.split("\t");
            map.put(split[0], Integer.valueOf(split[1]));
        }
        return map;
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        try {
            FeatureStore featureStore = (FeatureStore) Class.forName(featureStoreImpl)
                    .newInstance();

            File tempFile = createInputFileFromFeatureStore(jcas, featureStore);

            File prediction = runPrediction(tempFile);

            List<TextClassificationOutcome> outcomes = getOutcomeAnnotations(jcas);
            List<String> writtenPredictions = FileUtils.readLines(prediction);

            checkErrorConditionNumberOfOutcomesEqualsNumberOfPredictions(outcomes,
                    writtenPredictions);

            for (int i = 0; i < outcomes.size(); i++) {

                if (learningMode.equals(Constants.LM_REGRESSION)) {
                    String val = writtenPredictions.get(i);
                    outcomes.get(i).setOutcome(val);
                }
                else {
                    String val = writtenPredictions.get(i).replaceAll("\\.0", "");
                    String pred = integer2OutcomeMapping.get(val);
                    outcomes.get(i).setOutcome(pred);
                }

            }

        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private List<TextClassificationOutcome> getOutcomeAnnotations(JCas jcas)
    {
        return new ArrayList<>(JCasUtil.select(jcas, TextClassificationOutcome.class));
    }

    private void checkErrorConditionNumberOfOutcomesEqualsNumberOfPredictions(
            List<TextClassificationOutcome> outcomes, List<String> readLines)
    {
        if (outcomes.size() != readLines.size()) {
            throw new IllegalStateException("Expected [" + outcomes.size()
                    + "] predictions but were [" + readLines.size() + "]");
        }
    }

    private File runPrediction(File tempFile)
        throws Exception
    {
        File prediction = FileUtil.createTempFile("libsvmPrediction", "libsvm");
        LibsvmPredict predictor = new LibsvmPredict();
        BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(tempFile), "utf-8"));

        DataOutputStream output = new DataOutputStream(new FileOutputStream(prediction));
        predictor.predict(r, output, model, 0);
        output.close();

        return prediction;
    }

    private File createInputFileFromFeatureStore(JCas jcas, FeatureStore featureStore)
        throws Exception
    {
        File tempFile = FileUtil.createTempFile("libsvm", ".tmp_libsvm");
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile), "utf-8"));

        List<Instance> inst = TaskUtils.getMultipleInstancesUnitMode(featureExtractors, jcas, true,
                featureStore.supportsSparseFeatures());
        for (Instance i : inst) {
            featureStore.addInstance(i);
        }

        for (Instance i : featureStore.getInstances()) {
            bw.write(OUTCOME_PLACEHOLDER);
            for (Feature f : i.getFeatures()) {
                if (!sanityCheckValue(f)) {
                    continue;
                }
                bw.write("\t");
                bw.write(featName2id.get(f.getName()) + ":" + f.getValue());
            }
            bw.write("\n");
        }
        bw.close();

        return tempFile;
    }

    private boolean sanityCheckValue(Feature f)
    {
        if (f.getValue() instanceof Number) {
            return true;
        }
        if (f.getName().equals(Constants.ID_FEATURE_NAME)) {
            return false;
        }

        try {
            Double.valueOf((String) f.getValue());
        }
        catch (Exception e) {
            throw new IllegalArgumentException(
                    "Feature [" + f.getName() + "] has a non-numeric value [" + f.getValue() + "]",
                    e);
        }
        return false;
    }

}