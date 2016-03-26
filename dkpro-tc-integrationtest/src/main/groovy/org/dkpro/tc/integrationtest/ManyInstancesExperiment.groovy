/*******************************************************************************
 * Copyright 2016
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

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import org.dkpro.tc.core.Constants
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.bayes.NaiveBayes
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.tc.core.Constants
import org.dkpro.tc.features.length.NrOfTokensDFE
import org.dkpro.tc.features.ngram.LuceneNGramDFE
import org.dkpro.tc.integrationtest.io.LineInstanceReader
import org.dkpro.tc.ml.ExperimentCrossValidation
import org.dkpro.tc.ml.report.BatchCrossValidationReport
import org.dkpro.tc.weka.WekaClassificationAdapter
import org.dkpro.tc.weka.report.WekaClassificationReport

/**
 * Testing with many instances.
 *
 */
public class ManyInstancesExperiment implements Constants {

    def experimentName = "ManyInstancesTest"

    // === PARAMETERS===========================================================

    def corpusFilePathTrain = "classpath:/data/smalltexts/smallInstances.txt.gz"
    def languageCode = "en"
    def numFolds = 10

    // === DIMENSIONS===========================================================

    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: LineInstanceReader.class,
        readerTrainParams: [
            LineInstanceReader.PARAM_SOURCE_LOCATION,
            corpusFilePathTrain]
    ])

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT)

    //UIMA parameters for FE configuration
    def dimPipelineParameters = Dimension.create(
    DIM_PIPELINE_PARAMS,
    [
        "TopK",
        "500",
        LuceneNGramDFE.PARAM_NGRAM_MIN_N,
        1,
        LuceneNGramDFE.PARAM_NGRAM_MAX_N,
        3
    ]
    )

    def dimClassificationArgs = Dimension.create(
    DIM_CLASSIFICATION_ARGS,
    [NaiveBayes.class.name])

    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
    [
        NrOfTokensDFE.class.name,
        LuceneNGramDFE.class.name
    ])

    // === Test =========================================================

    public void run() throws Exception
    {

        ExperimentCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessing: getPreprocessing(),
            machineLearningAdapter: WekaClassificationAdapter,
            innerReports: [
                WekaClassificationReport
            ],
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimClassificationArgs,
                dimFeatureSets,
                dimPipelineParameters
            ],
            reports:         [
                BatchCrossValidationReport
            ],
            numFolds: numFolds]

        Lab.getInstance().run(batchTask)
    }

    private AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class)
    }

    public static void main(String[] args)
    {
        new ManyInstancesExperiment().run()
    }
}