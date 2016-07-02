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
package org.dkpro.tc.core.task.uima;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.util.ValidityCheckUtils;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * UIMA analysis engine that is used in the {@link InitTask} to test error conditions on the CAS.
 * 
 * This is called after initialization (which sets outcome and unit annotations) and executed for
 * each CAS.
 */
public class ValidityCheckConnectorPost
    extends ConnectorBase
{

    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true, defaultValue = Constants.LM_SINGLE_LABEL)
    private String learningMode;

    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true, defaultValue = Constants.FM_DOCUMENT)
    private String featureMode;

    private int featureModeI;
    private int learningModeI;

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {

        getLogger().log(Level.INFO, "--- post-validation for CAS with id ["
                + JCasUtil.selectSingle(jcas, JCasId.class).getId() + "] ---");

        if (featureModeI == 0) {
            featureModeI = ValidityCheckUtils.featureModeLabel2int(featureMode);
        }

        if (learningModeI == 0) {
            learningModeI = ValidityCheckUtils.learningModeLabel2int(learningMode);
        }

        List<TextClassificationOutcome> outcomes = new ArrayList<>(
                JCasUtil.select(jcas, TextClassificationOutcome.class));
        List<TextClassificationTarget> classificationUnits = new ArrayList<>(
                JCasUtil.select(jcas, TextClassificationTarget.class));

        // whether outcome annotation are present at all
        checkErrorConditionZeroOutcomes(outcomes);

        checkErrorConditionMoreThanOneOutcomeInSingleLabelDocumentMode(jcas, outcomes);

        checkErrorConditionMissingOutcomeForTargetIfUnitOrSequenceMode(jcas, classificationUnits,
                outcomes);

        getLogger().log(Level.FINE, "--- post-validation for CAS with id ["
                + JCasUtil.selectSingle(jcas, JCasId.class).getId() + "] complete ---");
    }

    private void checkErrorConditionMissingOutcomeForTargetIfUnitOrSequenceMode(JCas jcas,
            List<TextClassificationTarget> targets, List<TextClassificationOutcome> outcomes)
                throws AnalysisEngineProcessException
    {
        // iff unit/sequence classification is active, there must be classificationUnit
        // annotations, each
        // labeled with an outcome annotation
        if (featureModeI == 2 || featureModeI == 4) {
            if (targets.size() == 0) {
                throw new AnalysisEngineProcessException(new TextClassificationException(
                        "Your experiment is configured to have classification units. Please add classification unit annotations to the CAS while reading your initial files."));
            }
            else {
                if (targets.size() != outcomes.size()) {
                    throwException("Number of targets [" + targets.size()
                            + "] != number of outcomes [" + outcomes.size() + "]");
                }

                for (int i = 0; i < targets.size(); i++) {
                    TextClassificationTarget t = targets.get(i);
                    TextClassificationOutcome o = outcomes.get(i);
                    if (t.getBegin() != o.getBegin() || t.getEnd() != o.getEnd()) {
                        throwException("Index of target and outcome do not match taget span: ["
                                + t.getBegin() + " - " + t.getEnd() + "] != outcome span "
                                + o.getBegin() + " - " + o.getEnd());
                    }
                }
            }
        }
    }

    private void throwException(String message)
        throws AnalysisEngineProcessException
    {
        throw new AnalysisEngineProcessException(new TextClassificationException(message));
    }

    private void checkErrorConditionMoreThanOneOutcomeInSingleLabelDocumentMode(JCas jcas,
            Collection<TextClassificationOutcome> outcomes)
                throws AnalysisEngineProcessException
    {
        // iff single-label is configured, there may not be more than one outcome annotation per
        // CAS, except the experiment is unit or sequence classification
        if (learningModeI != 2 && featureModeI != 2 && featureModeI != 4 && outcomes.size() > 2) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "Your experiment is configured to be single-label, but I found more than one outcome annotation for "
                            + DocumentMetaData.get(jcas).getDocumentUri()
                            + ". Please configure your project to be multi-label or make sure to have only one outcome per instance."));
        }
    }

    private void checkErrorConditionZeroOutcomes(Collection<TextClassificationOutcome> outcomes)
        throws AnalysisEngineProcessException
    {
        if (outcomes.size() == 0) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "No TextClassificationOutcome annotation found. "
                            + "The reader must make sure that the expected outcome of the classification is annotated accordingly."));
        }
    }
}