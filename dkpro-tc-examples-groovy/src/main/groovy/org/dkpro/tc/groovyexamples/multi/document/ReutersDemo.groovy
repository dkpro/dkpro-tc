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
package org.dkpro.tc.groovyexamples.multi.document

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription
import meka.classifiers.multilabel.BR

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.CollectionReaderFactory
import org.apache.uima.resource.ResourceInitializationException
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.lab.task.BatchTask.ExecutionPolicy
import org.dkpro.tc.api.features.TcFeatureFactory
import org.dkpro.tc.api.features.TcFeatureSet
import org.dkpro.tc.core.Constants
import org.dkpro.tc.examples.io.ReutersCorpusReader
import org.dkpro.tc.examples.util.DemoUtils
import org.dkpro.tc.ml.ExperimentCrossValidation
import org.dkpro.tc.ml.ExperimentTrainTest
import org.dkpro.tc.ml.report.BatchCrossValidationReport
import org.dkpro.tc.ml.report.BatchTrainTestReport
import org.dkpro.tc.ml.weka.MekaClassificationAdapter

import weka.attributeSelection.InfoGainAttributeEval
import weka.classifiers.bayes.NaiveBayes
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import org.dkpro.tc.features.length.*
import org.dkpro.tc.features.ngram.*

/**
 * Groovy-Version of the ReutersTextClassificationExperiment
 *
 * Base class for running for Train-Test and CrossValidation experiments using the pre-configured BatchTask setups.
 */
public class ReutersDemo implements Constants {

    // === PARAMETERS===========================================================

    def experimentName = "ReutersTextClassification"
    def corpusFilePathTrain = "src/main/resources/data/reuters/training"
    def corpusFilePathTest = "src/main/resources/data/reuters/test"
    def goldLabelFilePath = "src/main/resources/data/reuters/cats.txt"
    def languageCode = "en"
    def numFolds = 2
    def threshold = "0.5"

    // === DIMENSIONS===========================================================

    def testreader = CollectionReaderFactory.createReaderDescription(ReutersCorpusReader.class,
    ReutersCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
    ReutersCorpusReader.PARAM_GOLD_LABEL_FILE, goldLabelFilePath,
    ReutersCorpusReader.PARAM_LANGUAGE, languageCode,
    ReutersCorpusReader.PARAM_PATTERNS, ReutersCorpusReader.INCLUDE_PREFIX + "*.txt");

    def trainreader = CollectionReaderFactory.createReaderDescription(ReutersCorpusReader.class,
    ReutersCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
    ReutersCorpusReader.PARAM_GOLD_LABEL_FILE, goldLabelFilePath,
    ReutersCorpusReader.PARAM_LANGUAGE, languageCode,
    ReutersCorpusReader.PARAM_PATTERNS, ReutersCorpusReader.INCLUDE_PREFIX + "*.txt");

    def dimReaders = Dimension.createBundle("readers", [
        readerTest: testreader,
        readerTrain: trainreader,
    ])

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_MULTI_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT)
    def dimThreshold = Dimension.create(DIM_BIPARTITION_THRESHOLD, threshold)

    def dimClassificationArgs =
    Dimension.create(
    DIM_CLASSIFICATION_ARGS,
     [new MekaClassificationAdapter(), BR.name, "-W", NaiveBayes.name])

    def dimFeatureSelection = Dimension.createBundle("featureSelection", [
        labelTransformationMethod: "BinaryRelevanceAttributeEvaluator",
        attributeEvaluator: [InfoGainAttributeEval.name],
        numLabelsToKeep: 100,
        applySelection: true
    ])


    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
        new TcFeatureSet(
            TcFeatureFactory.create(NrOfTokens.class),
            TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 50, LuceneNGram.PARAM_NGRAM_MIN_N, 1,LuceneNGram.PARAM_NGRAM_MIN_N, 2 )
        )
    ,
        new TcFeatureSet(
            TcFeatureFactory.create(NrOfTokens.class),
            TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 10, LuceneNGram.PARAM_NGRAM_MIN_N, 1,LuceneNGram.PARAM_NGRAM_MIN_N, 2 )
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
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimThreshold,
                dimClassificationArgs,
                dimFeatureSelection,
                dimFeatureSets,
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [BatchCrossValidationReport.newInstance()],
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
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimThreshold,
                dimClassificationArgs,
                dimFeatureSelection,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [BatchTrainTestReport.newInstance()]]

        // Run
        Lab.getInstance().run(batchTask)
    }

    private AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter),
                createEngineDescription(OpenNlpPosTagger)
                )
    }

    public static void main(String[] args)
    {
        DemoUtils.setDkproHome(ReutersDemo.name);
        new ReutersDemo().runTrainTest()
        //new ReutersDemo().runCrossValidation()
    }

}
