/**
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.weka.task.serialization;

import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.FM_UNIT;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_CLASSIFIER;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_CLASS_LABELS;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_FEATURE_NAMES;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationFocus;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.util.SaveModelUtils;
import de.tudarmstadt.ukp.dkpro.tc.ml.uima.TcAnnotatorDocument;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

public class LoadModelConnectorWeka
    extends ModelSerialization_ImplBase
{

    @ConfigurationParameter(name = TcAnnotatorDocument.PARAM_TC_MODEL_LOCATION, mandatory = true)
    private File tcModelLocation;

    public static final String PARAM_BIPARTITION_THRESHOLD = "bipartitionThreshold";
    @ConfigurationParameter(name = PARAM_BIPARTITION_THRESHOLD, mandatory = true, defaultValue = "0.5")
    private String bipartitionThreshold;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
    private String learningMode;

    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
    private String featureMode;

    private Classifier cls;
    private List<Attribute> attributes;
    private List<String> classLabels;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            cls = (Classifier) weka.core.SerializationHelper.read(new File(tcModelLocation,
                    MODEL_CLASSIFIER).getAbsolutePath());

            attributes = new ArrayList<>();
            for (String attributeName : FileUtils.readLines(new File(tcModelLocation,
                    MODEL_FEATURE_NAMES))) {
                attributes.add(new Attribute(attributeName));
            }

            classLabels = new ArrayList<>();

            for (String classLabel : FileUtils.readLines(new File(tcModelLocation,
                    MODEL_CLASS_LABELS))) {
                classLabels.add(classLabel);
            }

            SaveModelUtils.verifyTcVersion(tcModelLocation,getClass());
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
            Logger.getLogger(getClass()).debug("START: process(JCAS) - applying Weka Model");

            Instance instance = de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils.getSingleInstance(
                    featureMode, featureExtractors, jcas, false, false);

            weka.core.Instance wekaInstance = WekaUtils.tcInstanceToWekaInstance(instance,
                    attributes, classLabels, false);

            String val = classLabels.get((int) cls.classifyInstance(wekaInstance));

            TextClassificationOutcome outcome = null;
            if (!FM_UNIT.equals(featureMode))
                outcome = JCasUtil.selectSingle(jcas, TextClassificationOutcome.class);
            else
                outcome = getOutcomeForFocus(jcas);

            outcome.setOutcome(val);

            Logger.getLogger(getClass()).debug(
                    "Found classification result \"" + val + "\" for text: \""
                            + outcome.getCoveredText() + "\"");

            Logger.getLogger(getClass()).debug("END: process(JCAS) - applying Weka Model");
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(new IllegalStateException(e.getMessage()));
        }
    }

    private TextClassificationOutcome getOutcomeForFocus(JCas jcas)
    {
        TextClassificationOutcome outcome = null;
        TextClassificationFocus focus = null;

        try {
            focus = JCasUtil.selectSingle(jcas, TextClassificationFocus.class);
        }
        catch (Exception ex) {
            String msg = "Error while trying to retrieve TC focus from CAS. Details: "
                    + ex.getMessage();
            Logger.getLogger(getClass()).error(msg, ex);
            throw new RuntimeException(msg);
        }

        Iterator<TextClassificationOutcome> outcomeIterator = JCasUtil.iterator(focus,
                TextClassificationOutcome.class, false /* ambiguous */, false /* strict */);

        if (!outcomeIterator.hasNext())
            throw new IllegalStateException(
                    "There should be exactly one TC outcome covered by the TC focus from "
                            + focus.getBegin() + " to " + focus.getEnd());

        outcome = outcomeIterator.next();

        if (outcomeIterator.hasNext())
            throw new IllegalStateException(
                    "There should be exactly one TC outcome covered by the TC focus from "
                            + focus.getBegin() + " to " + focus.getEnd());

        return outcome;
    }
}