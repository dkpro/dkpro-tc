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
package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.single.document

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.SMO
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentCrossValidation
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentTrainTest
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchCrossValidationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchOutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchTrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaClassificationReport

/**
 * Groovy-Version of the TwentyNewsgroupsExperiment
 *
 * The TwentyNewsgroupsGroovyExperiment does the same as TwentyNewsgroupsGroovyExtendedExperiment,
 * but it uses the {@link CrossValidationExperiment} and {@link TrainTestExperiment} to automatically wire the standard tasks for
 * a basic CV and TrainTest setup. This is more convenient, but less flexible.
 *
 * If you need to define a more complex experiment setup, look at TwentyNewsgroupsGroovyExtendedExperiment
 *
 * @author Oliver Ferschke
 */
public class TwentyNewsgroupsDemo implements Constants {

    // === PARAMETERS===========================================================

    def experimentName = "TwentyNewsgroups"
    def corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train"
    def corpusFilePathTest  ="src/main/resources/data/twentynewsgroups/bydate-test"
    def languageCode = "en"
    def numFolds = 2

    // === DIMENSIONS===========================================================

    def dimReaders = Dimension.createBundle("readers", [
        readerTest: TwentyNewsgroupsCorpusReader.class,
        readerTestParams: [
            TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
            corpusFilePathTest,
            TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE,
            languageCode,
            TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
            TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"
        ],
        readerTrain: TwentyNewsgroupsCorpusReader.class,
        readerTrainParams: [
            TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION,
            corpusFilePathTrain,
            TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE,
            languageCode,
            TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
            TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"
        ]
    ])

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT)

    def dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
    [NaiveBayes.class.name],
    [SMO.class.name])

    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
    [
        NrOfTokensDFE.class.name,
        LuceneNGramDFE.class.name
        //        NGramFeatureExtractor.class.name

    ]
    )

    //    def dimPipelineParameters = Dimension.create(
    //    DIM_PIPELINE_PARAMS,
    //    [
    //        NGramFeatureExtractor.PARAM_NGRAM_USE_TOP_K,
    //        "50",
    //        NGramFeatureExtractor.PARAM_NGRAM_MIN_N,
    //        1,
    //        NGramFeatureExtractor.PARAM_NGRAM_MAX_N,
    //        3
    //    ],
    //    [
    //        NGramFeatureExtractor.PARAM_NGRAM_USE_TOP_K,
    //        "100",
    //        NGramFeatureExtractor.PARAM_NGRAM_MIN_N,
    //        1,
    //        NGramFeatureExtractor.PARAM_NGRAM_MAX_N,
    //        3
    //    ]
    //    );

    def dimPipelineParameters = Dimension.create(
    DIM_PIPELINE_PARAMS,
    [
        LuceneNGramDFE.PARAM_NGRAM_USE_TOP_K,
        "50",
        LuceneNGramDFE.PARAM_NGRAM_MIN_N,
        1,
        LuceneNGramDFE.PARAM_NGRAM_MAX_N,
        3
    ],
    [
        LuceneNGramDFE.PARAM_NGRAM_USE_TOP_K,
        "100",
        LuceneNGramDFE.PARAM_NGRAM_MIN_N,
        1,
        LuceneNGramDFE.PARAM_NGRAM_MAX_N,
        3
    ]
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
            innerReports: [
                WekaClassificationReport.class
            ],
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimClassificationArgs,
                dimFeatureSets,
                dimPipelineParameters
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                WekaBatchCrossValidationReport
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
            innerReports: [
                WekaClassificationReport.class
            ],
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimClassificationArgs,
                dimFeatureSets,
                dimPipelineParameters
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                WekaBatchTrainTestReport,
                WekaBatchOutcomeIDReport]
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
        new TwentyNewsgroupsDemo().runTrainTest()
        new TwentyNewsgroupsDemo().runCrossValidation()
    }

}
