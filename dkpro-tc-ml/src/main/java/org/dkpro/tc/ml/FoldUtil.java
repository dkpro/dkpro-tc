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
package org.dkpro.tc.ml;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;

public class FoldUtil
{
    /**
     * Takes the available CAS and creates more cases from them to conform to the minimal requested
     * amount of CAS objects to have sufficient for running a cross-validation. Computes a
     * rule-of-thumb value to split each of the found cas into N sub-cases and the end the total
     * created number is compared to the requested number of CAS and an exception thrown if too few
     * CAS were created.
	 * 
	 * @param inputFolder
	 * 			the input folder
	 * @param numFolds
	 * 			number of folds to create
	 * @param numAvailableJCas
	 * 			number available cas'
	 * @param isSequence
	 * 			is sequence model
	 * @return	returns folder with sufficient folds
	 * @throws Exception
	 * 			if not enough data is available for creating the required number of folds 
	 */
    public static File createMinimalSplit(String inputFolder, int numFolds, int numAvailableJCas,
            boolean isSequence)
        throws Exception
    {
        File outputFolder = new File(inputFolder, "output");
        int splitNum = (int) Math.ceil(numFolds / (double) numAvailableJCas);

        CollectionReaderDescription createReader = CollectionReaderFactory.createReaderDescription(
                BinaryCasReader.class, BinaryCasReader.PARAM_SOURCE_LOCATION, inputFolder,
                BinaryCasReader.PARAM_PATTERNS, "*.bin",
                BinaryCasReader.PARAM_ADD_DOCUMENT_METADATA, false);

        AnalysisEngineDescription multiplier = AnalysisEngineFactory.createEngineDescription(
                FoldClassificationUnitCasMultiplier.class,
                FoldClassificationUnitCasMultiplier.PARAM_REQUESTED_SPLITS, splitNum,
                FoldClassificationUnitCasMultiplier.PARAM_USE_SEQUENCES, isSequence);

        AnalysisEngineDescription xmiWriter = AnalysisEngineFactory.createEngineDescription(
                BinaryCasWriter.class, BinaryCasWriter.PARAM_TARGET_LOCATION,
                outputFolder.getAbsolutePath(), BinaryCasWriter.PARAM_FORMAT, "6+");

        AnalysisEngineDescription both = AnalysisEngineFactory.createEngineDescription(multiplier,
                xmiWriter);

        SimplePipeline.runPipeline(createReader, both);

        // final check - do we have at least as many folds as requested by "numFolds"?
        isNumberOfCasCreatedLargerEqualNumFolds(outputFolder, numFolds);

        return outputFolder;
    }

    /**
     * test condition if data has to be split further
     * @param outputFolder
     * 			the output folder
     * @param numFolds
     * 			number of folds requested
     * @throws Exception
     * 			in case of an error
     */
    private static void isNumberOfCasCreatedLargerEqualNumFolds(File outputFolder, int numFolds)
        throws Exception
    {
        File[] listFiles = outputFolder.listFiles(new FilenameFilter()
        {

            @Override
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".bin");
            }
        });
        if (listFiles.length < numFolds) {
            throw new IllegalStateException("Failed to create at least [" + numFolds + "] CAS");
        }
    }

}
