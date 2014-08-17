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
package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.sequence

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.component.NoOpAnnotator
import org.apache.uima.resource.ResourceInitializationException

import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.tc.examples.io.NERDemoReader
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsUFE
import de.tudarmstadt.ukp.dkpro.tc.features.style.InitialCharacterUpperCaseUFE
import de.tudarmstadt.ukp.dkpro.tc.mallet.MalletAdapter
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.MalletBatchCrossValidationReport
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.MalletClassificationReport
import de.tudarmstadt.ukp.dkpro.tc.mallet.writer.MalletDataWriter
import de.tudarmstadt.ukp.dkpro.tc.ml.BatchTaskCrossValidation

/**
 * Example for German NER as sequence classification (groovy setup).
 */
class NERSequenceDemo
implements Constants {

    def String LANGUAGE_CODE = "de"
    def NUM_FOLDS = 2
    def String corpusFilePathTrain = "src/main/resources/data/germ_eval2014_ner/"
    def experimentName = "NamedEntitySequenceDemoCV"

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_SEQUENCE)
    def dimDataWriter = Dimension.create(DIM_DATA_WRITER, MalletDataWriter.name)
    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET, [
        NrOfCharsUFE.name,
        InitialCharacterUpperCaseUFE.name
    ])
    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: NERDemoReader,
        readerTrainParams: [
            NERDemoReader.PARAM_LANGUAGE,
            "de",
            NERDemoReader.PARAM_SOURCE_LOCATION,
            corpusFilePathTrain,
            NERDemoReader.PARAM_PATTERNS,
            INCLUDE_PREFIX + "*.txt"
        ]])

    public static void main(String[] args){
        new NERSequenceDemo().runCrossValidation() }

    // ##### CV #####
    protected void runCrossValidation()
    throws Exception
    {
        BatchTaskCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessingPipeline:  getPreprocessing(),
			machineLearningAdapter: MalletAdapter.getInstance(),
            innerReports: [MalletClassificationReport],
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimDataWriter,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [MalletBatchCrossValidationReport],
            numFolds: NUM_FOLDS]

        // Run
        Lab.getInstance().run(batchTask)
    }

    protected AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(NoOpAnnotator.class)
    }
}
