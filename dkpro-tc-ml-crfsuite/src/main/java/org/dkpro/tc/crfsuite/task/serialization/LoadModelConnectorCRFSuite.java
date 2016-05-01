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

package org.dkpro.tc.crfsuite.task.serialization;

import static org.dkpro.tc.core.Constants.MODEL_CLASSIFIER;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.util.SaveModelUtils;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.crfsuite.task.CRFSuiteTestTask;
import org.dkpro.tc.crfsuite.writer.CRFSuiteFeatureStoreSequenceIterator;
import org.dkpro.tc.ml.uima.TcAnnotator;

public class LoadModelConnectorCRFSuite
    extends ModelSerialization_ImplBase
{

    @ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
    private File tcModelLocation;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    @ConfigurationParameter(name = PARAM_FEATURE_STORE_CLASS, mandatory = true)
    private String featureStoreImpl;

    private File model = null;

    private String executablePath;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            executablePath = CRFSuiteTestTask.getExecutablePath();
            model = new File(tcModelLocation, MODEL_CLASSIFIER);
            SaveModelUtils.verifyTcVersion(tcModelLocation, getClass());
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        try {
            FeatureStore featureStore = (FeatureStore) Class.forName(featureStoreImpl)
                    .newInstance();
            int sequenceId = 0;
            for (TextClassificationSequence seq : JCasUtil.select(jcas,
                    TextClassificationSequence.class)) {

                List<Instance> instances = TaskUtils.getInstancesInSequence(featureExtractors,
                        jcas, seq, true, sequenceId++);

                for (Instance instance : instances) {
                    featureStore.addInstance(instance);
                }

            }

            CRFSuiteFeatureStoreSequenceIterator iterator = new CRFSuiteFeatureStoreSequenceIterator(
                    featureStore);

            //takes N sequences and classifies them - all results are hold in memory
            StringBuilder output = new StringBuilder();
            while (iterator.hasNext()) {

                StringBuilder buffer = new StringBuilder();
                int limit = 5000;
                int idx = 0;
                while (iterator.hasNext()) {
                    StringBuilder seqInfo = iterator.next();
                    buffer.append(seqInfo);
                    idx++;
                    if (idx == limit) {
                        break;
                    }
                }

                List<String> command = buildCommand();
                StringBuilder out = runCommand(command, buffer.toString());
                output.append(out);
            }

            setPredictedOutcome(jcas, output.toString());
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private StringBuilder runCommand(List<String> command, String buffer) throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectError(Redirect.INHERIT);
        pb.command(command);
        Process process = pb.start();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        writer.write(buffer.toString());
        writer.close();
        return CRFSuiteTestTask.captureProcessOutput(process);
    }

    private List<String> buildCommand() throws Exception
    {
        List<String> command = new ArrayList<String>();
        command.add(executablePath);
        command.add("tag");
        command.add("-m");
        command.add(model.getAbsolutePath());
        command.add("-"); //Read from STDIN

        return command;
    }

    private void setPredictedOutcome(JCas jcas, String aLabels)
    {
        List<TextClassificationOutcome> outcomes = new ArrayList<TextClassificationOutcome>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
        String[] labels = aLabels.split("\n");

        for (int i = 0, labelIdx = 0; i < outcomes.size(); i++) {
            if (labels[labelIdx].isEmpty()) {
                // empty lines mark end of sequence
                // shift label index +1 to begin of next sequence
                labelIdx++;
            }
            TextClassificationOutcome o = outcomes.get(i);
            o.setOutcome(labels[labelIdx++]);
        }

    }

}