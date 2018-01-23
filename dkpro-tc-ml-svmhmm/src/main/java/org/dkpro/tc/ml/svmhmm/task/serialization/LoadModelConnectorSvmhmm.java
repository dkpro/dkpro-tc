/**
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
 */
package org.dkpro.tc.ml.svmhmm.task.serialization;

import static org.dkpro.tc.core.Constants.MODEL_CLASSIFIER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.util.SaveModelUtils;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.ml.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.ml.svmhmm.task.SVMHMMTestTask;
import org.dkpro.tc.ml.svmhmm.util.SVMHMMUtils;
import org.dkpro.tc.ml.svmhmm.writer.SVMHMMDataWriter;
import org.dkpro.tc.ml.uima.TcAnnotator;

public class LoadModelConnectorSvmhmm
    extends ModelSerialization_ImplBase
{

    @ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
    private File tcModelLocation;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
    private String learningMode;

    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
    private String featureMode;

    private File model = null;
    private Path tmpFolderForFeatureFile = null;
    private BidiMap loadMapping;

    SVMHMMDataWriter svmhmmDataWriter;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            tmpFolderForFeatureFile = Files
                    .createTempDirectory("temp" + System.currentTimeMillis());
            model = new File(tcModelLocation, MODEL_CLASSIFIER);
            loadMapping = loadLabel2IntegerMap();
            //Copy feature-name file 
            FileUtils.copyFile(new File(tcModelLocation, Constants.FILENAME_FEATURES), new File(tmpFolderForFeatureFile.toFile(), Constants.FILENAME_FEATURES));
            SaveModelUtils.verifyTcVersion(tcModelLocation, getClass());

            String[] outcomes = (String[]) loadMapping.keySet().toArray(new String[0]);
            svmhmmDataWriter = new SVMHMMDataWriter();
            svmhmmDataWriter.init(tmpFolderForFeatureFile.toFile(), new SVMHMMAdapter().useSparseFeatures(),
                    learningMode, false, outcomes);
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }
    
    private BidiMap loadLabel2IntegerMap()
        throws IOException
    {
        File mappingFile = new File(tcModelLocation.getAbsolutePath() + "/"
                + SVMHMMUtils.LABELS_TO_INTEGERS_MAPPING_FILE_NAME);
        return SVMHMMUtils.loadMapping(mappingFile);
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        try {
            List<Instance> instances = new ArrayList<>();
            int sequenceId = 0;
            for (TextClassificationSequence seq : JCasUtil.select(jcas,
                    TextClassificationSequence.class)) {

                instances.addAll(TaskUtils.getInstancesInSequence(featureExtractors, jcas, seq,
                        true, sequenceId++));
            }

            svmhmmDataWriter.writeClassifierFormat(instances, false);

            File featureFile = new File(tmpFolderForFeatureFile.toFile() + "/"
                    + new SVMHMMAdapter().getFrameworkFilename(
                            TcShallowLearningAdapter.AdapterNameEntries.featureVectorsFile));
            File augmentedTestFile = SVMHMMUtils.replaceLabelsWithIntegers(featureFile,
                    loadMapping);

            File predictionsFile = FileUtil.createTempFile("svmhmmPrediction", ".txt");
            SVMHMMTestTask.callTestCommand(predictionsFile, model, augmentedTestFile);

            setOutcomes(jcas, predictionsFile, loadMapping);

        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private void setOutcomes(JCas jcas, File predictionsFile, BidiMap loadMapping)
        throws Exception
    {
        List<TextClassificationOutcome> outcomes = new ArrayList<TextClassificationOutcome>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
        int idx = 0;

        // avoid holding all predictions in RAM (might use a lot of RAM if a few million predictions
        // are being made)
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(predictionsFile), "utf-8"));
        String line = null;
        while ((line = br.readLine()) != null) {
            Integer i = Integer.valueOf(line);
            String outcome = (String) loadMapping.getKey(i);
            outcomes.get(idx).setOutcome(outcome);
            idx++;
        }
        br.close();
    }

}