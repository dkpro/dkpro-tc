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

package org.dkpro.tc.ml.crfsuite.task.serialization;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.feature.InstanceIdFeature;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.ml.crfsuite.core.CrfSuitePredictor;
import org.dkpro.tc.ml.crfsuite.writer.CrfSuiteFeatureFormatExtractionIterator;
import org.dkpro.tc.ml.model.PreTrainedModelProviderAbstract;

public class CrfSuiteLoadModelConnector
    extends ModelSerialization_ImplBase
{

    @ConfigurationParameter(name = PreTrainedModelProviderAbstract.PARAM_TC_MODEL_LOCATION, mandatory = true)
    private File tcModelLocation;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    private File model = null;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            model = new File(tcModelLocation, MODEL_CLASSIFIER);
            verifyTcVersion(tcModelLocation, getClass());
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException
    {
        try {
            int sequenceId = 0;

            List<Instance> instance = new ArrayList<>(2048);
            for (TextClassificationSequence seq : JCasUtil.select(jcas,
                    TextClassificationSequence.class)) {
                instance.addAll(
                        getInstancesInSequence(featureExtractors, jcas, seq, true, sequenceId++));
            }

            CrfSuiteFeatureFormatExtractionIterator iterator = new CrfSuiteFeatureFormatExtractionIterator(
                    instance);

            // takes N sequences and classifies them - all results are hold in
            // memory
            StringBuilder output = new StringBuilder(1024);
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

                CrfSuitePredictor predict = new CrfSuitePredictor();
                String out = predict.predict(buffer.toString(), model);
                output.append(out);
            }

            setPredictedOutcome(jcas, output.toString());
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private void setPredictedOutcome(JCas aJCas, String aLabels)
    {
        List<TextClassificationOutcome> outcomes = new ArrayList<TextClassificationOutcome>(
                JCasUtil.select(aJCas, TextClassificationOutcome.class));
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

    private List<Instance> getInstancesInSequence(
            FeatureExtractorResource_ImplBase[] aFeatureExtractors, JCas aJCas,
            TextClassificationSequence aSequencd, boolean addInstanceId, int aSequenceId)
        throws Exception
    {

        List<Instance> instances = new ArrayList<Instance>();
        int jcasId = JCasUtil.selectSingle(aJCas, JCasId.class).getId();
        List<TextClassificationTarget> seqTargets = JCasUtil.selectCovered(aJCas,
                TextClassificationTarget.class, aSequencd);

        for (TextClassificationTarget aTarget : seqTargets) {

            Instance instance = new Instance();
            if (addInstanceId) {
                instance.addFeature(InstanceIdFeature.retrieve(aJCas, aTarget, aSequenceId));
            }

            // execute feature extractors and add features to instance
            try {
                for (FeatureExtractorResource_ImplBase featExt : aFeatureExtractors) {
                    Set<Feature> features = ((FeatureExtractor) featExt).extract(aJCas, aTarget);
                    features.forEach(x -> {
                        if (!x.isDefaultValue()) {
                            instance.addFeature(x);
                        }
                    });
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }

            // set and write outcome label(s)
            instance.setOutcomes(getOutcomes(aJCas, aTarget));
            instance.setJcasId(jcasId);
            instance.setSequenceId(aSequenceId);
            instance.setSequencePosition(aTarget.getId());

            instances.add(instance);
        }

        return instances;
    }

    private List<String> getOutcomes(JCas aJCas, AnnotationFS aTarget)
        throws TextClassificationException
    {
        Collection<TextClassificationOutcome> outcomes;
        if (aTarget == null) {
            outcomes = JCasUtil.select(aJCas, TextClassificationOutcome.class);
        }
        else {
            outcomes = JCasUtil.selectCovered(aJCas, TextClassificationOutcome.class, aTarget);
        }

        if (outcomes.size() == 0) {
            throw new TextClassificationException(
                    "No [" + TextClassificationOutcome.class.getName()
                            + "] annotations present in current CAS.");
        }

        List<String> stringOutcomes = new ArrayList<String>();
        for (TextClassificationOutcome outcome : outcomes) {
            stringOutcomes.add(outcome.getOutcome());
        }
        return stringOutcomes;
    }
}