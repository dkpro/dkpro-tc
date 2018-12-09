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

package org.dkpro.tc.ml.vowpalwabbit.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Properties;

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
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.task.uima.InstanceExtractor;
import org.dkpro.tc.ml.model.PreTrainedModelProviderAbstract;
import org.dkpro.tc.ml.vowpalwabbit.core.VowpalWabbitPredictor;
import org.dkpro.tc.ml.vowpalwabbit.writer.VowpalWabbitDataWriter;
import static java.nio.charset.StandardCharsets.UTF_8;

public class VowpalWabbitLoadModelConnector
    extends ModelSerialization_ImplBase
{

    @ConfigurationParameter(name = PreTrainedModelProviderAbstract.PARAM_TC_MODEL_LOCATION, mandatory = true)
    private File tcModelLocation;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    private File model = null;
    private String featureMode;
    protected Map<String, String> integer2OutcomeMapping;
    protected Map<String, String> stringValue2IntegerMapping;

    Integer maxStringId = -1;

    private String learningMode;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            model = new File(tcModelLocation, MODEL_CLASSIFIER);
            featureMode = loadProperty(new File(tcModelLocation, MODEL_FEATURE_MODE),
                    DIM_FEATURE_MODE);
            learningMode = loadProperty(new File(tcModelLocation, MODEL_LEARNING_MODE),
                    DIM_LEARNING_MODE);
            integer2OutcomeMapping = loadMapping(tcModelLocation,
                    VowpalWabbitDataWriter.OUTCOME_MAPPING);
            stringValue2IntegerMapping = loadMapping(tcModelLocation,
                    VowpalWabbitDataWriter.STRING_MAPPING);
            determineTheMaxStringsIntIdValue();

            verifyTcVersion(tcModelLocation, getClass());
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    private void determineTheMaxStringsIntIdValue()
    {
        OptionalInt max = stringValue2IntegerMapping.keySet().stream().mapToInt(Integer::parseInt)
                .max();
        if (max.isPresent()) {
            maxStringId = max.getAsInt();
        }
    }

    private String loadProperty(File file, String key) throws IOException
    {
        String value = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties p = new Properties();
            p.load(fis);

            value = p.getProperty(key);
        }
        if (value == null) {
            throw new IllegalStateException(
                    "Could not load [" + key + "] from file [" + file + "]");
        }
        return value;
    }

    private Map<String, String> loadMapping(File tcModelLocation, String key) throws IOException
    {
        if (isRegression()) {
            return new HashMap<>();
        }

        Map<String, String> map = new HashMap<>();
        List<String> readLines = FileUtils.readLines(new File(tcModelLocation, key), UTF_8);
        for (String l : readLines) {
            String[] split = l.split("\t");
            map.put(split[1], split[0]);
        }
        return map;
    }

    private boolean isRegression()
    {
        return learningMode.equals(Constants.LM_REGRESSION);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        File file = null;
        if (isSequence()) {
            file = createInputFile(aJCas, true);
        }
        else {
            file = createInputFile(aJCas, false);
        }

        List<String> prediction = runPrediction(file, isSequence());

        List<TextClassificationOutcome> outcomes = getOutcomeAnnotations(aJCas);

        for (int i = 0; i < outcomes.size(); i++) {

            if (isRegression()) {
                String val = prediction.get(i);
                outcomes.get(i).setOutcome(val);
            }
            else {
                String val = prediction.get(i);
                String pred = integer2OutcomeMapping.get(val);
                outcomes.get(i).setOutcome(pred);
            }

        }

    }

    private List<TextClassificationOutcome> getOutcomeAnnotations(JCas aJCas)
    {
        return new ArrayList<TextClassificationOutcome>(
                JCasUtil.select(aJCas, TextClassificationOutcome.class));
    }

    protected List<String> runPrediction(File tempFile, boolean isSequence)
        throws AnalysisEngineProcessException
    {
        List<String> predict = null;
        try {
            File prediction = FileUtil
                    .createTempFile("vowpalWabbitPrediction" + System.currentTimeMillis(), ".txt");
            prediction.deleteOnExit();

            VowpalWabbitPredictor predictor = new VowpalWabbitPredictor();
            predict = predictor.predict(tempFile, model);

            if (isSequence) {
                List<String> seqPred = new ArrayList<>();
                for (String p : predict) {
                    seqPred.addAll(Arrays.asList(p.split(" ")));
                }
                predict = seqPred;
            }

        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

        return predict;
    }

    private File createInputFile(JCas aJCas, boolean isSequenceMod)
        throws AnalysisEngineProcessException
    {
        File tempFile = null;
        try {
            tempFile = FileUtil.createTempFile("vowpalWabbit" + System.currentTimeMillis(), ".txt");
            tempFile.deleteOnExit();

            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(tempFile), UTF_8))) {

                InstanceExtractor extractor = new InstanceExtractor(featureMode, featureExtractors,
                        false);
                List<Instance> instances = extractor.getInstances(aJCas, true);

                if (isSequenceMod) {
                    Collections.sort(instances, new Comparator<Instance>()
                    {

                        @Override
                        public int compare(Instance o1, Instance o2)
                        {
                            return Integer.compare(o1.getSequenceId(), o2.getSequenceId());
                        }
                    });
                }

                int prevSeqId = -1;
                for (Instance instance : instances) {

                    if (instance.getSequenceId() != prevSeqId && prevSeqId != -1) {
                        bw.write("\n");
                    }

                    bw.write("|");

                    for (Feature f : instance.getFeatures()) {
                        bw.write(" ");
                        bw.write(f.getName() + ":"
                                + mapStringValues(f.getType(), f.getValue().toString()));
                    }
                    bw.write("\n");
                }
            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

        return tempFile;
    }

    private String mapStringValues(FeatureType featureType, String value)
    {
        if (featureType == FeatureType.STRING || featureType == FeatureType.NOMINAL) {
            String string = stringValue2IntegerMapping.get(value);
            if (string == null) {
                return (++maxStringId).toString();
            }
        }

        return value;
    }

    private boolean isSequence()
    {
        return featureMode.equals(FM_SEQUENCE);
    }
}