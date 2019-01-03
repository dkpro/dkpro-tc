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
package org.dkpro.tc.groovyexamples.single.unit

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription

import org.apache.uima.fit.factory.AnalysisEngineFactory
import org.apache.uima.fit.factory.CollectionReaderFactory
import org.apache.uima.resource.ExternalResourceDescription
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.lab.task.BatchTask.ExecutionPolicy
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet
import org.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader

import org.dkpro.tc.examples.shallow.annotators.SequenceOutcomeAnnotator
import org.dkpro.tc.examples.util.DemoUtils
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.ml.experiment.ExperimentCrossValidation
import org.dkpro.tc.ml.report.CrossValidationReport
import org.dkpro.tc.ml.weka.WekaAdapter

import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.SMO
/**
 * This example is the same as {@link BrownUnitPosDemo}, except that it demonstrates how to
 * pass an external resource to the feature extractor.  (In this case we pass a dummy
 * external resource.)
 *
 */
public class BrownUnitPosExternalResourceDemo
implements Constants {

    def String LANGUAGE_CODE = "en"
    def NUM_FOLDS = 2
    def String corpusFilePathTrain = "src/main/resources/data/brown_tei/"
    def experimentName = "BrownPosExternalResourceDemo"

    def ExternalResourceDescription dummyResource = createExternalResourceDescription(
        DummyResource.class)
    
    def trainreader = CollectionReaderFactory.createReaderDescription(TeiReader.class,
        TeiReader.PARAM_LANGUAGE, LANGUAGE_CODE,
        TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
        TeiReader.PARAM_PATTERNS, [ INCLUDE_PREFIX + "*.xml", INCLUDE_PREFIX + "*.xml.gz"]
    );

    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: trainreader,
        ])
    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_UNIT)
    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET, new TcFeatureSet(
        TcFeatureFactory.create(TokenRatioPerDocument.class, NrOfTokensExternalResource.PARAM_DUMMY_RESOURCE, dummyResource)
    ))

    def config1  = [
        (DIM_CLASSIFICATION_ARGS) : [new WekaAdapter(), NaiveBayes.name],
        (DIM_DATA_WRITER) : new WekaAdapter().getDataWriterClass(),
        (DIM_FEATURE_USE_SPARSE) : new WekaAdapter().useSparseFeatures()
    ]
    def config2  = [
        (DIM_CLASSIFICATION_ARGS) : [new WekaAdapter(), SMO.name],
        (DIM_DATA_WRITER) : new WekaAdapter().getDataWriterClass(),
        (DIM_FEATURE_USE_SPARSE) : new WekaAdapter().useSparseFeatures()
    ]
    def mlas = Dimension.createBundle("mlas", config1, config2)

    // ##### CV #####
    protected void runCrossValidation()
    throws Exception
    {
        ExperimentCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
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
            preprocessing: AnalysisEngineFactory.createEngineDescription(SequenceOutcomeAnnotator.class),
            numFolds: NUM_FOLDS]

        // Run
        Lab.getInstance().run(batchTask)
    }

    public static void main(String[] args)
    throws Exception {
		DemoUtils.setDkproHome(BrownUnitPosExternalResourceDemo.getSimpleName());
        new BrownUnitPosExternalResourceDemo().runCrossValidation()
    }
}
