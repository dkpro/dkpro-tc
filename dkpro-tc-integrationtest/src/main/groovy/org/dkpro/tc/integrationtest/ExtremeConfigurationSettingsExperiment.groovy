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
package org.dkpro.tc.integrationtest

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.tc.core.Constants
import org.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader
import org.dkpro.tc.features.length.NrOfTokens
import org.dkpro.tc.features.ngram.LuceneNGram
import org.dkpro.tc.ml.ExperimentCrossValidation
import org.dkpro.tc.ml.ExperimentTrainTest
import org.dkpro.tc.ml.report.BatchCrossValidationReport
import org.dkpro.tc.ml.report.BatchTrainTestReport
import org.dkpro.tc.ml.weka.WekaClassificationAdapter
import org.apache.uima.collection.CollectionReaderDescription;

import weka.classifiers.bayes.NaiveBayes
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet

/**
 * Experiment setup used to test extreme configuration settings like empty feature extractors etc.
 * The experiment setup was adapted from the TwentyNewsgroups example.
 */
public class ExtremeConfigurationSettingsExperiment implements Constants {

    def experimentName = "ExtremeConfigurationSettingsTest"

    // === PARAMETERS===========================================================

    def corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/train"
    def corpusFilePathTest  ="src/main/resources/data/twentynewsgroups/test"
    def languageCode = "en"
    def numFolds = 2
    def manyFolds = 10

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
        readerTrain: trainreader,
    ])

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT)

    def dimClassificationArgs =
    Dimension.create(
    DIM_CLASSIFICATION_ARGS,
    [
        [NaiveBayes.class.name].toArray()
    ] as Object[]
    )

    def dimClassificationArgsEmpty =
    Dimension.create(DIM_CLASSIFICATION_ARGS, [] as Object[])

    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
        new TcFeatureSet(
            TcFeatureFactory.create(NrOfTokens.class),
            TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 500, LuceneNGram.PARAM_NGRAM_MIN_N, 1, LuceneNGram.PARAM_NGRAM_MAX_N, 3)
        )
    )

    def dimFeatureSetsEmpty = Dimension.create(DIM_FEATURE_SET, [] as Object[])

    // === Test =========================================================

    public void runEmptyPipelineParameters() throws Exception
    {

        ExperimentCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessing: getPreprocessing(),
            machineLearningAdapter: WekaClassificationAdapter,
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimClassificationArgs,
                dimFeatureSets
            ],
            reports:         [
                BatchCrossValidationReport.newInstance()
            ],
            numFolds: numFolds]

        Lab.getInstance().run(batchTask)

        ExperimentTrainTest TrainTestExperiment = [
            experimentName: experimentName + "-TrainTest-Groovy",
            type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
            preprocessing: getPreprocessing(),
            machineLearningAdapter: WekaClassificationAdapter,
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimClassificationArgs,
                dimFeatureSets
            ],
            reports:         [
                BatchTrainTestReport.newInstance()
                ]
        ]

        // Run
        Lab.getInstance().run(TrainTestExperiment)
    }

    public void runEmptyFeatureExtractorSet() throws Exception
    {

        ExperimentCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessing: getPreprocessing(),
            machineLearningAdapter: WekaClassificationAdapter,
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimClassificationArgs,
                dimFeatureSetsEmpty
            ],
            reports:         [
                BatchCrossValidationReport.newInstance()
            ],
            numFolds: numFolds]

        Lab.getInstance().run(batchTask)

        ExperimentTrainTest TrainTestExperiment = [
            experimentName: experimentName + "-TrainTest-Groovy",
            type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
            preprocessing: getPreprocessing(),
            machineLearningAdapter: WekaClassificationAdapter,
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimClassificationArgs,
                dimFeatureSetsEmpty
            ],
            reports:         [
                BatchTrainTestReport.newInstance()]
        ]

        // Run
        Lab.getInstance().run(TrainTestExperiment)
    }

    private AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(
        createEngineDescription(BreakIteratorSegmenter.class),
        createEngineDescription(OpenNlpPosTagger.class)
        )
    }
}