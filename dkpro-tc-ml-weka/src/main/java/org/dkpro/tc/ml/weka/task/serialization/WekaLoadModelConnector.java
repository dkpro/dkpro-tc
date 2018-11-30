/**
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.ml.weka.task.serialization;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.uima.InstanceExtractor;
import org.dkpro.tc.ml.model.PreTrainedModelProviderAbstract;
import org.dkpro.tc.ml.weka.core._eka;
import org.dkpro.tc.ml.weka.util.AttributeStore;
import org.dkpro.tc.ml.weka.writer.WekaDataWriter;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;
public class WekaLoadModelConnector
    extends ModelSerialization_ImplBase
    implements Constants
{

    @ConfigurationParameter(name = PreTrainedModelProviderAbstract.PARAM_TC_MODEL_LOCATION, mandatory = true)
    private File tcModelLocation;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
    private String learningMode;

    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
    private String featureMode;

    private Classifier cls;
    private Instances trainingData;
    private List<String> classLabels;

    boolean useSparse = false;

    private String bipartitionThreshold;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            TcShallowLearningAdapter initMachineLearningAdapter = initMachineLearningAdapter(
                    tcModelLocation);
            bipartitionThreshold = initBipartitionThreshold(tcModelLocation);
            useSparse = initMachineLearningAdapter.useSparseFeatures();

            loadClassifier();
            loadTrainingData();
            if (!learningMode.equals(Constants.LM_REGRESSION)) {
                loadClassLabels();
            }

            verifyTcVersion(tcModelLocation, getClass());
            writeFeatureMode(tcModelLocation, featureMode);
            writeLearningMode(tcModelLocation, learningMode);
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    private String initBipartitionThreshold(File tcModelLocation)
        throws FileNotFoundException, IOException
    {
        File file = new File(tcModelLocation, MODEL_BIPARTITION_THRESHOLD);
        Properties prop = new Properties();

        try(FileInputStream fis = new FileInputStream(file)){
            prop.load(fis);
        }

        return prop.getProperty(DIM_BIPARTITION_THRESHOLD);
    }

    private void loadClassLabels() throws IOException
    {
        classLabels = new ArrayList<>();
        for (String classLabel : FileUtils.readLines(new File(tcModelLocation, MODEL_CLASS_LABELS),
                UTF_8)) {
            classLabels.add(classLabel);
        }
    }

    private void loadTrainingData() throws IOException, ClassNotFoundException
    {
        ObjectInputStream inT = new ObjectInputStream(
                new FileInputStream(new File(tcModelLocation, "training_data")));
        trainingData = (Instances) inT.readObject();
        inT.close();
    }

    private void loadClassifier() throws Exception
    {
        cls = (Classifier) weka.core.SerializationHelper
                .read(new File(tcModelLocation, MODEL_CLASSIFIER).getAbsolutePath());
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException
    {

        Instance instance = null;
        try {

            InstanceExtractor extractor = new InstanceExtractor(featureMode, featureExtractors,
                    false);
            List<Instance> instances = extractor.getInstances(jcas, useSparse);
            instance = instances.get(0);
        }
        catch (Exception e1) {
            throw new AnalysisEngineProcessException(e1);
        }

        boolean isMultiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
        boolean isRegression = learningMode.equals(Constants.LM_REGRESSION);

        if (!isMultiLabel) {
            // single-label
            weka.core.Instance wekaInstance = null;
            try {
                wekaInstance = new _eka().tcInstanceToWekaInstance(instance, trainingData,
                        classLabels, isRegression);
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }

            Object val = null;
            try {
                if (!isRegression) {
                    val = classLabels.get((int) cls.classifyInstance(wekaInstance));
                }
                else {
                    val = cls.classifyInstance(wekaInstance);
                }
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }

            TextClassificationOutcome outcome = getOutcome(jcas);
            outcome.setOutcome(val.toString());
        }
        else {
            // multi-label
            weka.core.Instance mekaInstance = null;
            try {
                mekaInstance = tcInstanceToMekaInstance(instance, trainingData,
                        classLabels);
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }

            double[] vals = null;
            try {
                vals = cls.distributionForInstance(mekaInstance);
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
            List<String> outcomes = new ArrayList<String>();
            for (int i = 0; i < vals.length; i++) {
                if (vals[i] >= Double.valueOf(bipartitionThreshold)) {
                    String label = mekaInstance.attribute(i).name()
                            .split(WekaDataWriter.CLASS_ATTRIBUTE_PREFIX)[1];
                    outcomes.add(label);
                }
            }

            // TextClassificationFocus focus = null;
            if (FM_DOCUMENT.equals(featureMode) || FM_PAIR.equals(featureMode)) {
                Collection<TextClassificationOutcome> oldOutcomes = JCasUtil.select(jcas,
                        TextClassificationOutcome.class);
                List<Annotation> annotationsList = new ArrayList<Annotation>();
                for (TextClassificationOutcome oldOutcome : oldOutcomes) {
                    annotationsList.add(oldOutcome);
                }
                for (Annotation annotation : annotationsList) {
                    annotation.removeFromIndexes();
                }
            }
            else {
                TextClassificationOutcome annotation = getOutcome(jcas);
                annotation.removeFromIndexes();
                // focus = JCasUtil.selectSingle(jcas, TextClassificationFocus.class);
            }
            if (outcomes.size() > 0) {
                TextClassificationOutcome newOutcome = new TextClassificationOutcome(jcas);
                newOutcome.setOutcome(outcomes.get(0));
                newOutcome.addToIndexes();
            }
            if (outcomes.size() > 1) {
                // add more outcome annotations
                try {

                    for (int i = 1; i < outcomes.size(); i++) {
                        TextClassificationOutcome newOutcome = new TextClassificationOutcome(jcas);
                        newOutcome.setOutcome(outcomes.get(i));
                        newOutcome.addToIndexes();
                    }
                }
                catch (Exception ex) {
                    String msg = "Error while trying to retrieve TC focus from CAS. Details: "
                            + ex.getMessage();
                    Logger.getLogger(getClass()).error(msg, ex);
                    throw new RuntimeException(msg, ex);
                }
            }
        }
    }

    private TextClassificationOutcome getOutcome(JCas jcas)
    {
        List<TextClassificationOutcome> outcomes = new ArrayList<>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
        if (outcomes.size() != 1) {
            throw new IllegalStateException("There should be exactly one TC outcome");
        }

        return outcomes.get(0);
    }
    
    private weka.core.Instance tcInstanceToMekaInstance(Instance instance,
          Instances trainingData, List<String> allClassLabels)
      throws Exception
  {
      AttributeStore attributeStore = new AttributeStore();
      List<Attribute> outcomeAttributes = createOutcomeAttributes(allClassLabels);

      // in Meka, class label attributes have to go on top
      for (Attribute attribute : outcomeAttributes) {
          attributeStore.addAttributeAtBegin(attribute.name(), attribute);
      }

      for (int i = outcomeAttributes.size(); i < trainingData.numAttributes(); i++) {
          attributeStore.addAttribute(trainingData.attribute(i).name(),
                  trainingData.attribute(i));
      }

      double[] featureValues = getFeatureValues(attributeStore, instance);

      SparseInstance sparseInstance = new SparseInstance(1.0, featureValues);
      trainingData.setClassIndex(outcomeAttributes.size());
      sparseInstance.setDataset(trainingData);
      return sparseInstance;
  }
    
   
    private   List<Attribute> createOutcomeAttributes(List<String> outcomeValues)
    {
        // make the order of the attributes predictable
        Collections.sort(outcomeValues);
        List<Attribute> atts = new ArrayList<Attribute>();

        for (String outcome : outcomeValues) {
            String name = outcome.contains(CLASS_ATTRIBUTE_PREFIX) ? outcome
                    : CLASS_ATTRIBUTE_PREFIX + outcome;
            atts.add(new Attribute(name, Arrays.asList(new String[] { "0", "1" })));
        }
        return atts;
    }

    private  double[] getFeatureValues(AttributeStore attributeStore, Instance instance)
    {
        double[] featureValues = new double[attributeStore.getAttributes().size()];

        for (Feature feature : instance.getFeatures()) {

            try {
                Attribute attribute = attributeStore.getAttribute(feature.getName());
                Object featureValue = feature.getValue();

                double attributeValue;
                if (featureValue instanceof Number) {
                    // numeric attribute
                    attributeValue = ((Number) feature.getValue()).doubleValue();
                }
                else if (featureValue instanceof Boolean) {
                    // boolean attribute
                    attributeValue = (Boolean) featureValue ? 1.0d : 0.0d;
                }
                else if (featureValue == null) {
                    // null
                    throw new IllegalArgumentException(
                            "You have an instance which doesn't specify a value for the feature "
                                    + feature.getName());
                }
                else {
                    // nominal or string
                    Object stringValue = feature.getValue();
                    if (!attribute.isNominal() && !attribute.isString()) {
                        throw new IllegalArgumentException(
                                "Attribute neither nominal nor string: " + stringValue);
                    }

                    int valIndex = attribute.indexOfValue(stringValue.toString());
                    if (valIndex == -1) {
                        if (attribute.isNominal()) {
                            throw new IllegalArgumentException(
                                    "Value not defined for given nominal attribute!");
                        }
                        else {
                            attribute.addStringValue(stringValue.toString());
                            valIndex = attribute.indexOfValue(stringValue.toString());
                        }
                    }
                    attributeValue = valIndex;
                }
                int offset = attributeStore.getAttributeOffset(attribute.name());

                if (offset != -1) {
                    featureValues[offset] = attributeValue;
                }
            }
            catch (NullPointerException e) {
                // ignore unseen attributes
            }
        }
        return featureValues;
    }
}