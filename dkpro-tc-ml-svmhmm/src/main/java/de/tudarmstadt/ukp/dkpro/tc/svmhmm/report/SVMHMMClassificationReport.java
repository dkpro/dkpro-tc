/*
 * Copyright 2014
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
 */

package de.tudarmstadt.ukp.dkpro.tc.svmhmm.report;

import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.ConfusionMatrix;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.SVMHMMUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ivan Habernal
 */
public class SVMHMMClassificationReport
        extends SVMHMMOutcomeIDReport
        implements Constants
{
    static Log log = LogFactory.getLog(SVMHMMClassificationReport.class);

    @Override
    public void execute()
            throws Exception
    {
        loadGoldAndPredictedLabels();

        ConfusionMatrix cm = new ConfusionMatrix();

        for (int i = 0; i < this.goldLabels.size(); i++) {
            cm.increaseValue(this.goldLabels.get(i), this.predictedLabels.get(i));
        }

        // write to the output
        SVMHMMUtils.writeOutputResults(getContext(), cm);

        // and print detailed results
        log.info(cm.printNiceResults());
        log.info(cm.printLabelPrecRecFm());
    }
}
