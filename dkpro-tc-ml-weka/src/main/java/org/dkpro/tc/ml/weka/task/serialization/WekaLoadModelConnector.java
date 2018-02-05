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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
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
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.util.SaveModelUtils;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.dkpro.tc.ml.weka.util.WekaUtils;
import org.dkpro.tc.ml.weka.writer.WekaDataWriter;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class WekaLoadModelConnector
    extends ModelSerialization_ImplBase implements Constants
{

    @ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
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
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            TcShallowLearningAdapter initMachineLearningAdapter = SaveModelUtils
                    .initMachineLearningAdapter(tcModelLocation);
            bipartitionThreshold = initBipartitionThreshold(tcModelLocation);
            useSparse = initMachineLearningAdapter.useSparseFeatures();

            loadClassifier();
            loadTrainingData();
            if (!learningMode.equals(Constants.LM_REGRESSION)) {
                loadClassLabels();
            }

            SaveModelUtils.verifyTcVersion(tcModelLocation, getClass());
            SaveModelUtils.writeFeatureMode(tcModelLocation, featureMode);
            SaveModelUtils.writeLearningMode(tcModelLocation, learningMode);
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

            FileInputStream fis = new FileInputStream(file);
            prop.load(fis);
            fis.close();

            return prop.getProperty(DIM_BIPARTITION_THRESHOLD);
        }

    private void loadClassLabels()
        throws IOException
    {
        classLabels = new ArrayList<>();
        for (String classLabel : FileUtils
                .readLines(new File(tcModelLocation, MODEL_CLASS_LABELS), "utf-8")) {
            classLabels.add(classLabel);
        }
    }

    private void loadTrainingData()
        throws IOException, ClassNotFoundException
    {
        ObjectInputStream inT = new ObjectInputStream(
                new FileInputStream(new File(tcModelLocation, "training_data")));
        trainingData = (Instances) inT.readObject();
        inT.close();
    }

    private void loadClassifier()
        throws Exception
    {
        cls = (Classifier) weka.core.SerializationHelper
                .read(new File(tcModelLocation, MODEL_CLASSIFIER).getAbsolutePath());
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {

        Instance instance = null;
        try {
            instance = TaskUtils.getSingleInstance(featureMode, featureExtractors, jcas, false,
                    false, useSparse);
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
                wekaInstance = WekaUtils.tcInstanceToWekaInstance(instance, trainingData,
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
                mekaInstance = WekaUtils.tcInstanceToMekaInstance(instance, trainingData,
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
}