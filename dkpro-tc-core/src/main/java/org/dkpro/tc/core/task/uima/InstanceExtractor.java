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
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureSet;
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
        try {
            if (isSequenceMode()) {
                return getSequenceInstances(aJCas, extractSparse);
            }
            else if (isUnitMode()) {
                return getUnitInstances(aJCas, extractSparse);
            }
            else {
                Instance instance = getSingleInstance(aJCas, extractSparse);
                List<Instance> instances = new ArrayList<Instance>();
                instances.add(instance);
                return instances;
            }

        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private boolean isUnitMode()
    {
        return featureMode.equals(Constants.FM_UNIT);
    }

    private boolean isSequenceMode()
    {
        return featureMode.equals(Constants.FM_SEQUENCE);
    }

    public List<Instance> getSequenceInstances(JCas aJCas, boolean useSparse)
        throws TextClassificationException
    {
        int jcasId = JCasUtil.selectSingle(aJCas, JCasId.class).getId();
        int sequenceId = 0;
        int targetId = 0;

        Collection<TextClassificationSequence> sequences = JCasUtil.select(aJCas,
                TextClassificationSequence.class);
        
        int initialSize = JCasUtil.select(aJCas, TextClassificationTarget.class).size();
        List<Instance> instances = new ArrayList<Instance>(initialSize);
        
        for (TextClassificationSequence seq : sequences) {
            targetId = 0;
            
            List<TextClassificationTarget> seqTargets = JCasUtil.selectCovered(aJCas,
                    TextClassificationTarget.class, seq);
            for (TextClassificationTarget aTarget : seqTargets) {

                aTarget.setId(targetId++);

                Instance instance = new Instance();

                if (addInstanceId) {
                    instance.addFeature(InstanceIdFeature.retrieve(aJCas, aTarget, sequenceId));
                }

                // execute feature extractors and add features to instance

                for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                    if (useSparse) {
                        instance.addFeatures(getSparse(aJCas, aTarget, featExt));
                    }
                    else {
                        instance.addFeatures(getDense(aJCas, aTarget, featExt));
                    }
                }

                // set and write outcome label(s)
                instance.setOutcomes(getOutcomes(aJCas, aTarget));
                instance.setWeight(getWeight(aJCas, aTarget));
                instance.setJcasId(jcasId);
                instance.setSequenceId(sequenceId);
                instance.setSequencePosition(aTarget.getId());

                instances.add(instance);
            }
            sequenceId++;
        }

        return instances;
    }

    public List<Instance> getUnitInstances(JCas aJCas, boolean supportSparseFeatures)
        throws TextClassificationException
    {
        List<Instance> instances = new ArrayList<Instance>();
        int jcasId = JCasUtil.selectSingle(aJCas, JCasId.class).getId();

        Collection<TextClassificationTarget> targets = JCasUtil.select(aJCas,
                TextClassificationTarget.class);
        for (TextClassificationTarget aTarget : targets) {

            Instance instance = new Instance();

            if (addInstanceId) {
                Feature feat = InstanceIdFeature.retrieve(aJCas, aTarget);
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
                    instance.addFeatures(getSparse(aJCas, aTarget, featExt));
                }
                else {
                    instance.addFeatures(getDense(aJCas, aTarget, featExt));
                }
            }

            // set and write outcome label(s)
            instance.setOutcomes(getOutcomes(aJCas, aTarget));
            instance.setWeight(getWeight(aJCas, aTarget));
            instance.setJcasId(jcasId);
            // instance.setSequenceId(sequenceId);
            instance.setSequencePosition(aTarget.getId());

            instances.add(instance);
        }

        return instances;
    }

    public Instance getSingleInstance(JCas aJCas, boolean supportSparseFeatures) throws Exception
    {

        Instance instance = new Instance();

        if (isDocumentMode()) {
            instance = getSingleInstanceDocument(instance, aJCas, supportSparseFeatures);
        }
        else if (isPairMode()) {
            instance = getSingleInstancePair(instance, aJCas);
        }
        else if (isUnitMode()) {
            instance = getSingleInstanceUnit(instance, aJCas, supportSparseFeatures);
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

    private Instance getSingleInstanceUnit(Instance anInstance, JCas aJCas,
            boolean supportsSparseFeature)
        throws Exception
    {
        int jcasId = JCasUtil.selectSingle(aJCas, JCasId.class).getId();
        TextClassificationTarget unit = JCasUtil.selectSingle(aJCas, TextClassificationTarget.class);

        if (addInstanceId) {
            anInstance.addFeature(InstanceIdFeature.retrieve(aJCas, unit));
        }

        for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {

            if (supportsSparseFeature) {
                anInstance.addFeatures(getSparse(aJCas, unit, featExt));
            }
            else {
                anInstance.addFeatures(getDense(aJCas, unit, featExt));
            }

            anInstance.setOutcomes(getOutcomes(aJCas, unit));
            anInstance.setWeight(getWeight(aJCas, unit));
            anInstance.setJcasId(jcasId);
        }
        return anInstance;
    }

    private Instance getSingleInstancePair(Instance anInstance, JCas aJCas)
        throws TextClassificationException
    {
        try {
            int jcasId = JCasUtil.selectSingle(aJCas, JCasId.class).getId();
            if (addInstanceId) {
                anInstance.addFeature(InstanceIdFeature.retrieve(aJCas));
            }

            for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                if (!(featExt instanceof PairFeatureExtractor)) {
                    throw new TextClassificationException(
                            "Using non-pair FE in pair mode: " + featExt.getResourceName());
                }
                JCas view1 = aJCas.getView(Constants.PART_ONE);
                JCas view2 = aJCas.getView(Constants.PART_TWO);

                anInstance.setOutcomes(getOutcomes(aJCas, null));
                anInstance.setWeight(getWeight(aJCas, null));
                anInstance.setJcasId(jcasId);
                anInstance.addFeatures(((PairFeatureExtractor) featExt).extract(view1, view2));
            }
        }
        catch (CASException e) {
            throw new TextClassificationException(e);
        }
        return anInstance;
    }

    private Instance getSingleInstanceDocument(Instance anInstance, JCas aJCas,
            boolean supportSparseFeatures)
        throws TextClassificationException
    {
        int jcasId = JCasUtil.selectSingle(aJCas, JCasId.class).getId();

        TextClassificationTarget documentTcu = JCasUtil.selectSingle(aJCas,
                TextClassificationTarget.class);

        if (addInstanceId) {
            anInstance.addFeature(InstanceIdFeature.retrieve(aJCas));
        }

        for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
            if (!(featExt instanceof FeatureExtractor)) {
                throw new TextClassificationException(
                        "Using incompatible feature in document mode: "
                                + featExt.getResourceName());
            }

            if (supportSparseFeatures) {
                anInstance.addFeatures(getSparse(aJCas, documentTcu, featExt));
            }
            else {
                anInstance.addFeatures(getDense(aJCas, documentTcu, featExt));
            }

            anInstance.setOutcomes(getOutcomes(aJCas, null));
            anInstance.setWeight(getWeight(aJCas, null));
            anInstance.setJcasId(jcasId);
        }

        return anInstance;
    }

    public List<String> getOutcomes(JCas aJCas, AnnotationFS anAnnotation) throws TextClassificationException
    {
        Collection<TextClassificationOutcome> outcomes;
        if (anAnnotation == null) {
            outcomes = JCasUtil.select(aJCas, TextClassificationOutcome.class);
        }
        else {
            outcomes = JCasUtil.selectCovered(aJCas, TextClassificationOutcome.class, anAnnotation);
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

    private double getWeight(JCas aJCas, AnnotationFS anAnnotation) throws TextClassificationException
    {
        Collection<TextClassificationOutcome> outcomes;
        if (anAnnotation == null) {
            outcomes = JCasUtil.select(aJCas, TextClassificationOutcome.class);
        }
        else {
            outcomes = JCasUtil.selectCovered(aJCas, TextClassificationOutcome.class, anAnnotation);
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

    private Set<Feature> getDense(JCas aJCas, TextClassificationTarget aTarget,
            FeatureExtractorResource_ImplBase aFeatExtractor)
        throws TextClassificationException
    {
        FeatureSet featureSet = ((FeatureExtractor) aFeatExtractor).extract(aJCas, aTarget);
        return featureSet.getAllFeatures();
    }

    private Set<Feature> getSparse(JCas aJCas, TextClassificationTarget aTarget,
            FeatureExtractorResource_ImplBase aFeatExtractor)
        throws TextClassificationException
    {
        FeatureSet features = ((FeatureExtractor) aFeatExtractor).extract(aJCas, aTarget);
        return features.getNonDefaultFeatures();
    }

}