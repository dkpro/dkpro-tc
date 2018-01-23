/**
 * Copyright 2018
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
package org.dkpro.tc.groovyexamples.single.document

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.CollectionReaderFactory
import org.apache.uima.resource.ResourceInitializationException
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.lab.task.BatchTask.ExecutionPolicy
import org.dkpro.tc.core.Constants
import org.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader
import org.dkpro.tc.examples.util.DemoUtils
import org.dkpro.tc.ml.ExperimentCrossValidation
import org.dkpro.tc.ml.ExperimentTrainTest
import org.dkpro.tc.ml.report.BatchCrossValidationReport
import org.dkpro.tc.ml.report.BatchTrainTestReport
import org.dkpro.tc.ml.weka.WekaClassificationAdapter
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.SMO
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import org.dkpro.tc.features.length.*
import org.dkpro.tc.features.ngram.*
/**
 * Groovy-Version of the TwentyNewsgroupsExperiment
 *
 * The TwentyNewsgroupsGroovyExperiment does the same as TwentyNewsgroupsGroovyExtendedExperiment,
 * but it uses the {@link CrossValidationExperiment} and {@link TrainTestExperiment} to automatically wire the standard tasks for
 * a basic CV and TrainTest setup. This is more convenient, but less flexible.
 *
 * If you need to define a more complex experiment setup, look at TwentyNewsgroupsGroovyExtendedExperiment
 */
public class TwentyNewsgroupsDemo implements Constants {

    // === PARAMETERS===========================================================

    def experimentName = "TwentyNewsgroups"
    def corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train"
    def corpusFilePathTest  ="src/main/resources/data/twentynewsgroups/bydate-test"
    def languageCode = "en"
    def numFolds = 2

    // === DIMENSIONS===========================================================
    
    def testreader = CollectionReaderFactory.createReaderDescription(TwentyNewsgroupsCorpusReader.class,
         TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
         TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, languageCode,
         TwentyNewsgroupsCorpusReader.PARAM_PATTERNS, TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"
        );
    
    def trainreader = CollectionReaderFactory.createReaderDescription(TwentyNewsgroupsCorpusReader.class,
        TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
        TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, languageCode,
        TwentyNewsgroupsCorpusReader.PARAM_PATTERNS, TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"
       );

    def dimReaders = Dimension.createBundle("readers", [
        readerTest: testreader,
        readerTrain: trainreader
    ])

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT)

    def dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
    [NaiveBayes.class.name],
    [SMO.class.name])

    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
        new TcFeatureSet(
            TcFeatureFactory.create(NrOfTokens.class),
            TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, "50", LuceneNGram.PARAM_NGRAM_MIN_N, 1, LuceneNGram.PARAM_NGRAM_MAX_N, 3)
        )
    )

    // === Experiments =========================================================

    /**
     * Crossvalidation setting
     *
     * @throws Exception
     */
    protected void runCrossValidation() throws Exception
    {

        ExperimentCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessing:	getPreprocessing(),
            machineLearningAdapter: WekaClassificationAdapter,
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimClassificationArgs,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchCrossValidationReport.newInstance()
            ],
            numFolds: numFolds]

        Lab.getInstance().run(batchTask)
    }
    /**
     * TrainTest Setting
     *
     * @throws Exception
     */
    protected void runTrainTest() throws Exception
    {

        ExperimentTrainTest batchTask = [
            experimentName: experimentName + "-TrainTest-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
            preprocessing:	getPreprocessing(),
            machineLearningAdapter: WekaClassificationAdapter,
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimClassificationArgs,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchTrainTestReport
                ]
        ]

        // Run
        Lab.getInstance().run(batchTask)
    }

    private AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(
        createEngineDescription(BreakIteratorSegmenter.class),
        createEngineDescription(OpenNlpPosTagger.class)
        )
    }

    public static void main(String[] args)
    {
		DemoUtils.setDkproHome(TwentyNewsgroupsDemo.getSimpleName());
        new TwentyNewsgroupsDemo().runTrainTest()
      //  new TwentyNewsgroupsDemo().runCrossValidation()
    }

}
