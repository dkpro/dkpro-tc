/*******************************************************************************
 * Copyright 2017
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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.util.ValidityCheckUtils;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * UIMA analysis engine that is used in the {@link InitTask} to test error conditions on the CAS.
 * 
 */
public class ValidityCheckConnector
    extends ConnectorBase
{

    /**
     * Bipartition threshold used in multi-label experiments
     */
    public static final String PARAM_BIPARTITION_THRESHOLD = "bipartitionThreshold";
    @ConfigurationParameter(name = PARAM_BIPARTITION_THRESHOLD, mandatory = false)
    private String bipartitionThreshold;

    @ConfigurationParameter(name = PARAM_DATA_WRITER_CLASS, mandatory = true)
    private String dataWriter;

    @ConfigurationParameter(name = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected String[] featureExtractors;

    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true, defaultValue = Constants.LM_SINGLE_LABEL)
    private String learningMode;

    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true, defaultValue = Constants.FM_DOCUMENT)
    private String featureMode;

    @ConfigurationParameter(name = PARAM_DEVELOPER_MODE, mandatory = true, defaultValue = "false")
    private boolean developerMode;

    private boolean firstCall;
    private int featureModeI;
    private int learningModeI;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        firstCall = true;
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        // make sure this class is only called once per pipeline
        if (firstCall) {
            firstCall = false;

            if (DocumentMetaData.get(jcas).getDocumentId() == null) {
                throw new AnalysisEngineProcessException(new TextClassificationException(
                        "Please set a Document ID for all of your input files."));
            }

            if (featureModeI == 0) {
                featureModeI = ValidityCheckUtils.featureModeLabel2int(featureMode);
            }

            if (learningModeI == 0) {
                learningModeI = ValidityCheckUtils.learningModeLabel2int(learningMode);
            }

            // getLogger().log(Level.INFO, "--- checking validity of experiment setup ---");

            // iff multi-label classification is active, no single-label data writer may be used
            if (isMultiLabel()) {
                checkErrorConditionDataWriterAndThreshold();
            }

            // iff pair classification is set, 2 views need to be present
            if (isPairMode()) {
                checkErrorConditionCasHasTwoVies(jcas);
            }

            // iff sequence classification is enabled, we currently only support single-label
            // classification
            if (isSingleLabelSequenceMode()) {
                throw new AnalysisEngineProcessException(new TextClassificationException(
                        "In sequence mode, only single-label learning is possible. Please set the learning mode to single-label."));
            }

            // verify feature extractors are valid within the specified mode
            try {
                switch (featureModeI) {
                case 1:
                case 2:
                case 4:
                    verifyNonPairFeatureExtractors(featureExtractors);
                    break;
                case 3:
                    verifyPairMode(featureExtractors);
                    break;
                default:
                    throw new AnalysisEngineProcessException("Please set a valid learning mode",
                            null);

                }
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    private boolean isSingleLabelSequenceMode()
    {
        return featureModeI == 4 && learningModeI != 1;
    }

    private void checkErrorConditionDataWriterAndThreshold() throws AnalysisEngineProcessException
    {
        if (dataWriter.equals("org.dkpro.tc.weka.WekaDataWriter")) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "Your experiment is configured to be multi-label. Please use a DataWriter, which is able to handle multi-label data."));
        }
        if (bipartitionThreshold == null) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "Your experiment is configured to be multi-label. Please set a bipartition threshold."));
        }        
    }

    private boolean isMultiLabel()
    {
        return learningModeI == 2;
    }

    private boolean isPairMode()
    {
        return featureModeI == 3;
    }

    private void checkErrorConditionCasHasTwoVies(JCas jcas) throws AnalysisEngineProcessException
    {
        try {
            jcas.getView(Constants.PART_ONE);
            jcas.getView(Constants.PART_TWO);
        }
        catch (CASException e) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "Your experiment is configured to be pair classification, but I could not find the two views "
                            + Constants.PART_ONE + " and " + Constants.PART_TWO
                            + ". Please use a reader that inhereits from "
                            + Constants.class.getName()));
        }        
    }

    private void verifyPairMode(String[] featureExtractors2)
        throws Exception
    {
        for (String featExt : featureExtractors) {
            FeatureExtractorResource_ImplBase featExtC = (FeatureExtractorResource_ImplBase) Class
                    .forName(featExt).newInstance();
            checkIfIsPairFeatureExtractor(featExt, featExtC);
            checkErrorConditionImplementsConflictingFeatureExtractorInterfaces(featExt, featExtC);
        }
    }

    private void checkIfIsPairFeatureExtractor(String featExt,
            FeatureExtractorResource_ImplBase featExtC)
                throws AnalysisEngineProcessException
    {
        if (!(featExtC instanceof PairFeatureExtractor)) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    featExt + " is not a valid Pair Feature Extractor."));
        }
    }

    private void verifyNonPairFeatureExtractors(String[] featureExtractors)
        throws Exception
    {
        for (String featExt : featureExtractors) {
            FeatureExtractorResource_ImplBase featExtC = (FeatureExtractorResource_ImplBase) Class
                    .forName(featExt).newInstance();
            implementsFeatureExtractorInterface(featExt, featExtC);
            checkErrorConditionImplementsConflictingFeatureExtractorInterfaces(featExt, featExtC);
        }
    }

    private void implementsFeatureExtractorInterface(String featExt,
            FeatureExtractorResource_ImplBase featExtC)
                throws AnalysisEngineProcessException
    {
        if (!(featExtC instanceof FeatureExtractor)) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    featExt + " is not a valid feature extractor."));
        }
    }

    private void checkErrorConditionImplementsConflictingFeatureExtractorInterfaces(String featExt,
            FeatureExtractorResource_ImplBase featExtC)
                throws AnalysisEngineProcessException
    {
        if (featExtC instanceof FeatureExtractor && (featExtC instanceof PairFeatureExtractor)) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    featExt + ": Feature Extractors need to define a unique type."));
        }
    }
}