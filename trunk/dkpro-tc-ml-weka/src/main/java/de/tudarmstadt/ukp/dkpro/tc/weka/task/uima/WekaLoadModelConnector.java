/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.weka.task.uima;

import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_CLASSIFIER;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_CLASS_LABELS;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.MODEL_FEATURE_NAMES;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.SaveModelConnector_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.ml.uima.TcAnnotator;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

public class WekaLoadModelConnector
    extends SaveModelConnector_ImplBase
{

    @ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
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
			cls = (Classifier) weka.core.SerializationHelper.read(new File(tcModelLocation, MODEL_CLASSIFIER).getAbsolutePath());
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		
        attributes = new ArrayList<>();
        try {
			for (String attributeName : FileUtils.readLines(new File(tcModelLocation, MODEL_FEATURE_NAMES))) {
				attributes.add(new Attribute(attributeName));
			}
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
        
        classLabels = new ArrayList<>();
        try {
			for (String classLabel : FileUtils.readLines(new File(tcModelLocation, MODEL_CLASS_LABELS))) {
				classLabels.add(classLabel);
			}
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}

    }

	@Override
	public void process(JCas jcas)
			throws AnalysisEngineProcessException
	{
		Instance instance = de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils
				.getSingleInstance(featureMode, featureExtractors, jcas, false,	false);

		try {
			weka.core.Instance wekaInstance = WekaUtils.tcInstanceToWekaInstance(instance, attributes,
					classLabels, false);

			String val = classLabels.get(new Double(cls.classifyInstance(wekaInstance)).intValue());

			TextClassificationOutcome outcome = JCasUtil.selectSingle(jcas, TextClassificationOutcome.class);
			outcome.setOutcome(val);
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
    }
}