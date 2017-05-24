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

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import org.dkpro.tc.core.Constants
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription


import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.CollectionReaderFactory
import org.apache.uima.resource.ResourceInitializationException
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.tc.core.Constants
import org.dkpro.tc.features.length.NrOfTokens
import org.dkpro.tc.features.ngram.LuceneNGram
import org.dkpro.tc.integrationtest.io.LineInstanceReader
import org.dkpro.tc.ml.ExperimentCrossValidation
import org.dkpro.tc.ml.report.BatchCrossValidationReport
import org.dkpro.tc.ml.weka.WekaClassificationAdapter

import weka.classifiers.bayes.NaiveBayes
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet
/**
 * Testing with many instances.
 *
 */
public class ManyInstancesExperiment implements Constants {

    def experimentName = "ManyInstancesTest"

    // === PARAMETERS===========================================================

    def corpusFilePathTrain = "classpath:/data/smalltexts/smallInstances.txt.gz"
    def languageCode = "en"
    def numFolds = 2

    // === DIMENSIONS===========================================================

    def trainreader = CollectionReaderFactory.createReaderDescription(LineInstanceReader.class,
       LineInstanceReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain
         );
    
    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: trainreader,
    ])

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT)

    def dimClassificationArgs = Dimension.create(
    DIM_CLASSIFICATION_ARGS,
    [NaiveBayes.class.name])

    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET,
    new TcFeatureSet(
        TcFeatureFactory.create(NrOfTokens.class),
        TcFeatureFactory.create(LuceneNGram.class, LuceneNGram.PARAM_NGRAM_USE_TOP_K, 500, LuceneNGram.PARAM_NGRAM_MIN_N, 1, LuceneNGram.PARAM_NGRAM_MAX_N, 3 )
    ))

    // === Test =========================================================

    public void run() throws Exception
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