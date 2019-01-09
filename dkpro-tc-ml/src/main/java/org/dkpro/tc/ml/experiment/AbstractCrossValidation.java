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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.FoldUtil;
import org.dkpro.tc.ml.base.Experiment_ImplBase;

public abstract class AbstractCrossValidation
    extends Experiment_ImplBase
    implements Constants
{
    protected int aNumFolds = 10;
    protected Comparator<String> comparator;

    /**
     * Sets the number of folds
     * 
     * @param numFolds
     *            folds
     */
    public void setNumFolds(int numFolds)
    {
        this.aNumFolds = numFolds;
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
            File outputFolder = FoldUtil.createMinimalSplit(xmiPathRoot.getAbsolutePath(), aNumFolds,
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

                if (numCas < aNumFolds) {
                    throw new IllegalStateException(
                            "Not enough " + TextClassificationTarget.class.getSimpleName()
                                    + " found to create at least [" + aNumFolds + "] folds");
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
    
    protected String[] setupBatchTask(TaskContext aContext, File xmiPathRoot,
            boolean useCrossValidationManualFolds, String featureMode)
    {
        Collection<File> files = FileUtils.listFiles(xmiPathRoot, new String[] { "bin" }, true);
        String[] fileNames = new String[files.size()];
        int i = 0;
        for (File f : files) {
            // adding file paths, not names
            fileNames[i] = f.getAbsolutePath();
            i++;
        }
        Arrays.sort(fileNames);
        if (aNumFolds == LEAVE_ONE_OUT) {
            aNumFolds = fileNames.length;
        }

        // is executed if we have less CAS than requested folds and manual mode is turned
        // off
        if (!useCrossValidationManualFolds && fileNames.length < aNumFolds) {
            xmiPathRoot = createRequestedNumberOfCas(xmiPathRoot, fileNames.length, featureMode);
            files = FileUtils.listFiles(xmiPathRoot, new String[] { "bin" }, true);
            fileNames = new String[files.size()];
            i = 0;
            for (File f : files) {
                // adding file paths, not names
                fileNames[i] = f.getAbsolutePath();
                i++;
            }
        }
        return fileNames;
    }
    
    protected abstract DimensionBundle<Collection<String>> getFoldDim(String[] fileNames);
}