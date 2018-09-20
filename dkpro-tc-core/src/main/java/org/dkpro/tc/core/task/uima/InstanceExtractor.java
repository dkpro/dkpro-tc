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
package org.dkpro.tc.core.task.uima;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.feature.InstanceIdFeature;

public class InstanceExtractor
    implements Constants
{

    private String featureMode;
    private FeatureExtractorResource_ImplBase[] featureExtractors;
    private boolean addInstanceId;

    public InstanceExtractor(String featureMode,
            FeatureExtractorResource_ImplBase[] featureExtractors, boolean addInstanceId)
    {
        this.featureMode = featureMode;
        this.featureExtractors = Arrays.copyOf(featureExtractors, featureExtractors.length);
        this.addInstanceId = addInstanceId;
    }

    public List<Instance> getInstances(JCas aJCas, boolean extractSparse)
        throws AnalysisEngineProcessException
    {

        List<Instance> extractedInstances = new ArrayList<>();

        try {
            if (isSequenceMode()) {
                List<Instance> instances = getSequenceInstances(aJCas, extractSparse);
                extractedInstances.addAll(instances);
            }
            else if (isUnitMode()) {
                List<Instance> instances = getUnitInstances(aJCas, extractSparse);
                extractedInstances.addAll(instances);
            }
            else {
                Instance instance = getSingleInstance(aJCas, extractSparse);
                extractedInstances.add(instance);
            }

        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

        return extractedInstances;
    }

    private boolean isUnitMode()
    {
        return featureMode.equals(Constants.FM_UNIT);
    }

    private boolean isSequenceMode()
    {
        return featureMode.equals(Constants.FM_SEQUENCE);
    }

    public List<Instance> getSequenceInstances(JCas jcas, boolean useSparse)
        throws TextClassificationException
    {
        List<Instance> instances = new ArrayList<Instance>();

        int jcasId = JCasUtil.selectSingle(jcas, JCasId.class).getId();
        int sequenceId = 0;
        int unitId = 0;

        Collection<TextClassificationSequence> sequences = JCasUtil.select(jcas,
                TextClassificationSequence.class);
        
        LogFactory.getLog(getClass()).info("--- processing [" + sequences.size() + "]" + sequences + "] ---");
        
        for (TextClassificationSequence seq : sequences) {
            unitId = 0;

            List<TextClassificationTarget> seqTargets = JCasUtil.selectCovered(jcas,
                    TextClassificationTarget.class, seq);
            for (TextClassificationTarget aTarget : seqTargets) {

                aTarget.setId(unitId++);

                Instance instance = new Instance();

                if (addInstanceId) {
                    instance.addFeature(InstanceIdFeature.retrieve(jcas, aTarget, sequenceId));
                }

                // execute feature extractors and add features to instance

                for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                    if (useSparse) {
                        instance.addFeatures(getSparse(jcas, aTarget, featExt));
                    }
                    else {
                        instance.addFeatures(getDense(jcas, aTarget, featExt));
                    }
                }

                // set and write outcome label(s)
                instance.setOutcomes(getOutcomes(jcas, aTarget));
                instance.setWeight(getWeight(jcas, aTarget));
                instance.setJcasId(jcasId);
                instance.setSequenceId(sequenceId);
                instance.setSequencePosition(aTarget.getId());

                instances.add(instance);
            }
            sequenceId++;
        }

        return instances;
    }

    public List<Instance> getUnitInstances(JCas jcas, boolean supportSparseFeatures)
        throws TextClassificationException
    {
        List<Instance> instances = new ArrayList<Instance>();
        int jcasId = JCasUtil.selectSingle(jcas, JCasId.class).getId();

        Collection<TextClassificationTarget> targets = JCasUtil.select(jcas,
                TextClassificationTarget.class);
        for (TextClassificationTarget aTarget : targets) {

            Instance instance = new Instance();

            if (addInstanceId) {
                Feature feat = InstanceIdFeature.retrieve(jcas, aTarget);
                instance.addFeature(feat);
            }

            // execute feature extractors and add features to instance

            for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                if (!(featExt instanceof FeatureExtractor)) {
                    throw new TextClassificationException(
                            "Feature extractor does not implement interface ["
                                    + FeatureExtractor.class.getName() + "]: "
                                    + featExt.getResourceName());
                }
                if (supportSparseFeatures) {
                    instance.addFeatures(getSparse(jcas, aTarget, featExt));
                }
                else {
                    instance.addFeatures(getDense(jcas, aTarget, featExt));
                }
            }

            // set and write outcome label(s)
            instance.setOutcomes(getOutcomes(jcas, aTarget));
            instance.setWeight(getWeight(jcas, aTarget));
            instance.setJcasId(jcasId);
            // instance.setSequenceId(sequenceId);
            instance.setSequencePosition(aTarget.getId());

            instances.add(instance);
        }

        return instances;
    }

    public Instance getSingleInstance(JCas jcas, boolean supportSparseFeatures) throws Exception
    {

        Instance instance = new Instance();

        if (isDocumentMode()) {
            instance = getSingleInstanceDocument(instance, jcas, supportSparseFeatures);
        }
        else if (isPairMode()) {
            instance = getSingleInstancePair(instance, jcas);
        }
        else if (isUnitMode()) {
            instance = getSingleInstanceUnit(instance, jcas, supportSparseFeatures);
        }

        return instance;
    }

    private boolean isPairMode()
    {
        return featureMode.equals(Constants.FM_PAIR);
    }

    private boolean isDocumentMode()
    {
        return featureMode.equals(Constants.FM_DOCUMENT);
    }

    private Instance getSingleInstanceUnit(Instance instance, JCas jcas,
            boolean supportsSparseFeature)
        throws Exception
    {
        int jcasId = JCasUtil.selectSingle(jcas, JCasId.class).getId();
        TextClassificationTarget unit = JCasUtil.selectSingle(jcas, TextClassificationTarget.class);

        if (addInstanceId) {
            instance.addFeature(InstanceIdFeature.retrieve(jcas, unit));
        }

        for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {

            if (supportsSparseFeature) {
                instance.addFeatures(getSparse(jcas, unit, featExt));
            }
            else {
                instance.addFeatures(getDense(jcas, unit, featExt));
            }

            instance.setOutcomes(getOutcomes(jcas, unit));
            instance.setWeight(getWeight(jcas, unit));
            instance.setJcasId(jcasId);
        }
        return instance;
    }

    private Instance getSingleInstancePair(Instance instance, JCas jcas)
        throws TextClassificationException
    {
        try {
            int jcasId = JCasUtil.selectSingle(jcas, JCasId.class).getId();
            if (addInstanceId) {
                instance.addFeature(InstanceIdFeature.retrieve(jcas));
            }

            for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                if (!(featExt instanceof PairFeatureExtractor)) {
                    throw new TextClassificationException(
                            "Using non-pair FE in pair mode: " + featExt.getResourceName());
                }
                JCas view1 = jcas.getView(Constants.PART_ONE);
                JCas view2 = jcas.getView(Constants.PART_TWO);

                instance.setOutcomes(getOutcomes(jcas, null));
                instance.setWeight(getWeight(jcas, null));
                instance.setJcasId(jcasId);
                instance.addFeatures(((PairFeatureExtractor) featExt).extract(view1, view2));
            }
        }
        catch (CASException e) {
            throw new TextClassificationException(e);
        }
        return instance;
    }

    private Instance getSingleInstanceDocument(Instance instance, JCas jcas,
            boolean supportSparseFeatures)
        throws TextClassificationException
    {
        int jcasId = JCasUtil.selectSingle(jcas, JCasId.class).getId();

        TextClassificationTarget documentTcu = JCasUtil.selectSingle(jcas,
                TextClassificationTarget.class);

        if (addInstanceId) {
            instance.addFeature(InstanceIdFeature.retrieve(jcas));
        }

        for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
            if (!(featExt instanceof FeatureExtractor)) {
                throw new TextClassificationException(
                        "Using incompatible feature in document mode: "
                                + featExt.getResourceName());
            }

            if (supportSparseFeatures) {
                instance.addFeatures(getSparse(jcas, documentTcu, featExt));
            }
            else {
                instance.addFeatures(getDense(jcas, documentTcu, featExt));
            }

            instance.setOutcomes(getOutcomes(jcas, null));
            instance.setWeight(getWeight(jcas, null));
            instance.setJcasId(jcasId);
        }

        return instance;
    }

    public List<String> getOutcomes(JCas jcas, AnnotationFS unit) throws TextClassificationException
    {
        Collection<TextClassificationOutcome> outcomes;
        if (unit == null) {
            outcomes = JCasUtil.select(jcas, TextClassificationOutcome.class);
        }
        else {
            outcomes = JCasUtil.selectCovered(jcas, TextClassificationOutcome.class, unit);
        }

        if (outcomes.size() == 0) {
            throw new TextClassificationException("No outcome annotations present in current CAS.");
        }

        List<String> stringOutcomes = new ArrayList<String>();
        for (TextClassificationOutcome outcome : outcomes) {
            stringOutcomes.add(outcome.getOutcome());
        }

        return stringOutcomes;
    }

    private double getWeight(JCas jcas, AnnotationFS unit) throws TextClassificationException
    {
        Collection<TextClassificationOutcome> outcomes;
        if (unit == null) {
            outcomes = JCasUtil.select(jcas, TextClassificationOutcome.class);
        }
        else {
            outcomes = JCasUtil.selectCovered(jcas, TextClassificationOutcome.class, unit);
        }

        if (outcomes.size() == 0) {
            throw new TextClassificationException(
                    "No instance weight annotation present in current CAS.");
        }

        double weight = -1.0;
        for (TextClassificationOutcome outcome : outcomes) {
            weight = outcome.getWeight();
        }

        return weight;
    }

    private Set<Feature> getDense(JCas jcas, TextClassificationTarget unit,
            FeatureExtractorResource_ImplBase featExt)
        throws TextClassificationException
    {
        return ((FeatureExtractor) featExt).extract(jcas, unit);
    }

    private Set<Feature> getSparse(JCas jcas, TextClassificationTarget unit,
            FeatureExtractorResource_ImplBase featExt)
        throws TextClassificationException
    {
        Set<Feature> features = ((FeatureExtractor) featExt).extract(jcas, unit);
        Set<Feature> filtered = new HashSet<>();
        for (Feature f : features) {
            if (!f.isDefaultValue()) {
                filtered.add(f);
            }
        }

        return filtered;
    }

}