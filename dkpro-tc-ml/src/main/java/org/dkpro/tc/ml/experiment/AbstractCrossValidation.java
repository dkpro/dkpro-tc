/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.ml.experiment;

import java.io.File;
import java.util.Comparator;

import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.FoldUtil;
import org.dkpro.tc.ml.base.Experiment_ImplBase;

public abstract class AbstractCrossValidation
    extends Experiment_ImplBase
    implements Constants
{
    protected int numFolds = 10;
    protected Comparator<String> comparator;

    /**
     * Sets the number of folds
     * 
     * @param numFolds
     *            folds
     */
    public void setNumFolds(int numFolds)
    {
        this.numFolds = numFolds;
    }
    
    /**
     * Sets a comparator
     * 
     * @param aComparator
     *            the comparator
     */
    public void setComparator(Comparator<String> aComparator)
    {
        comparator = aComparator;
    }

    /**
     * creates required number of CAS
     * 
     * @param xmiPathRoot
     *            input path
     * @param numAvailableJCas
     *            all CAS
     * @param featureMode
     *            the feature mode
     * @return a file
     */
    protected File createRequestedNumberOfCas(File xmiPathRoot, int numAvailableJCas,
            String featureMode)
    {

        try {
            File outputFolder = FoldUtil.createMinimalSplit(xmiPathRoot.getAbsolutePath(), numFolds,
                    numAvailableJCas, FM_SEQUENCE.equals(featureMode));

            verfiyThatNeededNumberOfCasWasCreated(outputFolder);

            return outputFolder;
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected void verfiyThatNeededNumberOfCasWasCreated(File outputFolder)
    {
        int numCas = 0;

        if (outputFolder != null) {

            File[] listFiles = outputFolder.listFiles();
            if (listFiles != null) {

                for (File f : listFiles) {
                    if (f.getName().contains(".bin")) {
                        numCas++;
                    }
                }

                if (numCas < numFolds) {
                    throw new IllegalStateException(
                            "Not enough " + TextClassificationTarget.class.getSimpleName()
                                    + " found to create at least [" + numFolds + "] folds");
                }
            }
            else {
                throw new NullPointerException();
            }
        }
        else {
            throw new NullPointerException("Output folder is null");
        }
    }
}