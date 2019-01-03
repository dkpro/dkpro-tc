/**
 * Copyright 2019
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
package org.dkpro.tc.groovyexamples.single.sequence

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.component.NoOpAnnotator
import org.apache.uima.fit.factory.CollectionReaderFactory
import org.apache.uima.resource.ResourceInitializationException
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.lab.task.BatchTask.ExecutionPolicy
import org.dkpro.tc.api.features.TcFeatureFactory
import org.dkpro.tc.api.features.TcFeatureSet
import org.dkpro.tc.core.Constants
import org.dkpro.tc.examples.shallow.annotators.SequenceOutcomeAnnotator
import org.dkpro.tc.examples.util.DemoUtils
import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter
import org.dkpro.tc.ml.experiment.ExperimentCrossValidation
import org.dkpro.tc.ml.report.CrossValidationReport
/**
 * This a Groovy experiment setup of POS tagging as sequence tagging.
 */
class BrownPosDemo
implements Constants {

    def String LANGUAGE_CODE = "en"
    def NUM_FOLDS = 2
    def String corpusFilePathTrain = "src/main/resources/data/brown_tei/"
    def experimentName = "BrownPosDemo"

    def trainreader = CollectionReaderFactory.createReaderDescription(TeiReader.class,
    TeiReader.PARAM_LANGUAGE, LANGUAGE_CODE,
    TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
    TeiReader.PARAM_PATTERNS, [
        "*.xml",
        "*.xml.gz"]
    );

    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: trainreader,
    ])
    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_SEQUENCE)
    def dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                         new TcFeatureSet(
							 TcFeatureFactory.create(TokenRatioPerDocument.class),
            TcFeatureFactory.create(CharacterNGram.class, 
									CharacterNGram.PARAM_NGRAM_USE_TOP_K, 50, 
									CharacterNGram.PARAM_NGRAM_MIN_N, 1,
									CharacterNGram.PARAM_NGRAM_MAX_N, 3 )
							 ))
	
    def config  = [
        (DIM_CLASSIFICATION_ARGS) : [new CrfSuiteAdapter(), CrfSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR],
        (DIM_DATA_WRITER) : new CrfSuiteAdapter().getDataWriterClass(),
        (DIM_FEATURE_USE_SPARSE) : new CrfSuiteAdapter().useSparseFeatures()
    ]
    def mlas = Dimension.createBundle("mlas", config)

    // ##### CV #####
    protected void runCrossValidation()
    throws Exception
    {
        ExperimentCrossValidation experiment = [
            experimentName: experimentName + "-CV-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessing:  getPreprocessing(),
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimFeatureSets,
				mlas
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                CrossValidationReport.newInstance()
            ],
            numFolds: NUM_FOLDS]

        // Run
        Lab.getInstance().run(experiment)
    }

    protected AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(SequenceOutcomeAnnotator.class)
    }

    public static void main(String[] args)
    {
        DemoUtils.setDkproHome(BrownPosDemo.getSimpleName());
        new BrownPosDemo().runCrossValidation()
    }
}
