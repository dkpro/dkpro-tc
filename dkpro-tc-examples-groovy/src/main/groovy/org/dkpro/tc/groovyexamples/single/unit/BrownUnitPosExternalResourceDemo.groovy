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
package org.dkpro.tc.groovyexamples.single.unit

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.component.NoOpAnnotator
import org.apache.uima.fit.factory.CollectionReaderFactory
import org.apache.uima.resource.ExternalResourceDescription
import org.apache.uima.resource.ResourceInitializationException
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.lab.task.BatchTask.ExecutionPolicy
import org.dkpro.tc.core.Constants
import org.dkpro.tc.examples.io.BrownCorpusReader
import org.dkpro.tc.features.length.NrOfTokens
import org.dkpro.tc.ml.ExperimentCrossValidation
import org.dkpro.tc.ml.report.BatchCrossValidationReport
import org.dkpro.tc.ml.weka.WekaAdapter
import org.dkpro.tc.examples.util.DemoUtils
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.SMO
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet
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
    
    def trainreader = CollectionReaderFactory.createReaderDescription(BrownCorpusReader.class,
        BrownCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
        BrownCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
        BrownCorpusReader.PARAM_PATTERNS, [ INCLUDE_PREFIX + "*.xml", INCLUDE_PREFIX + "*.xml.gz"]
    );

    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: trainreader,
        ])
    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_UNIT)
    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET, new TcFeatureSet(
        TcFeatureFactory.create(NrOfTokens.class, NrOfTokensExternalResource.PARAM_DUMMY_RESOURCE, dummyResource)
    ))
    def dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
    [new WekaAdapter(), NaiveBayes.name],
	[new WekaAdapter(), SMO.name])

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
                dimClassificationArgs
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchCrossValidationReport.newInstance()
            ],
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
