/**
 * Copyright 2016
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
package org.dkpro.tc.groovyexamples.single.unit

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.component.NoOpAnnotator
import org.apache.uima.fit.factory.CollectionReaderFactory
import org.apache.uima.resource.ResourceInitializationException
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.lab.task.BatchTask.ExecutionPolicy
import org.dkpro.tc.core.Constants
import org.dkpro.tc.examples.io.NERDemoReader
import org.dkpro.tc.features.length.NrOfChars
import org.dkpro.tc.features.style.InitialCharacterUpperCase
import org.dkpro.tc.ml.ExperimentCrossValidation
import org.dkpro.tc.ml.report.BatchCrossValidationReport
import org.dkpro.tc.weka.WekaClassificationAdapter

import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.SMO
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeatureFactory;
/**
 * This is an example for German NER as unit classification (groovy setup). Each Entity is treated as a classification
 * unit. This is only a showcase of the concept.
 */
public class NERUnitDemo
implements Constants {

    def String LANGUAGE_CODE = "de"
    def NUM_FOLDS = 2
    def String corpusFilePathTrain = "src/main/resources/data/germ_eval2014_ner/"
    def experimentName = "NamedEntitySequenceDemoCV"

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_UNIT)
    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET, [
        TcFeatureFactory.create(NrOfChars.class),
        TcFeatureFactory.create(InitialCharacterUpperCase.class)
    ])
    
    def trainreader = CollectionReaderFactory.createReaderDescription(NERDemoReader.class,
       NERDemoReader.PARAM_LANGUAGE,  "de",
       NERDemoReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
       NERDemoReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt"
    );
    
    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: trainreader,
        ])
    def dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
    [SMO.name],[NaiveBayes.name])

    public static void main(String[] args)
    throws Exception {
        new NERUnitDemo().runCrossValidation() }

    // ##### CV #####
    protected void runCrossValidation()
    throws Exception
    {
        ExperimentCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessing:  getPreprocessing(),
            machineLearningAdapter: WekaClassificationAdapter,
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimFeatureSets,
                dimClassificationArgs
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchCrossValidationReport
            ],
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
