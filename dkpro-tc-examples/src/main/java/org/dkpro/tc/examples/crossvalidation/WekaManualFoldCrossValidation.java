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
package org.dkpro.tc.examples.crossvalidation;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.TcFeature;
import org.dkpro.tc.core.util.TcFeatureFactory;
import org.dkpro.tc.examples.io.BrownCorpusReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.style.IsSurroundedByChars;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.weka.WekaClassificationAdapter;

import weka.classifiers.bayes.NaiveBayes;

public class WekaManualFoldCrossValidation
    implements Constants
{
    public static final String LANGUAGE_CODE = "de";
    public static final int NUM_FOLDS = 2;
    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei/";

    public static void main(String[] args)
        throws Exception
    {

        WekaManualFoldCrossValidation demo = new WekaManualFoldCrossValidation();
        demo.runCrossValidation(getParameterSpace(true), NUM_FOLDS);
    }

    // ##### CV #####
    public void runCrossValidation(ParameterSpace pSpace, int folds)
        throws Exception
    {
        // This is used to ensure that the required DKPRO_HOME environment variable is set.
        // Ensures that people can run the experiments even if they haven't read the setup
        // instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME as
        // explained there.
        DemoUtils.setDkproHome(WekaManualFoldCrossValidation.class.getSimpleName());

        ExperimentCrossValidation batch = new ExperimentCrossValidation("NERDemoCV",
                WekaClassificationAdapter.class, folds);
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }

    public static ParameterSpace getParameterSpace(boolean manualFolds)
        throws ResourceInitializationException
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, BrownCorpusReader.class);

        CollectionReaderDescription readerTrain = CollectionReaderFactory
                .createReaderDescription(BrownCorpusReader.class, BrownCorpusReader.PARAM_LANGUAGE,
                        "de", BrownCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                        BrownCorpusReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.xml");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

        @SuppressWarnings("unchecked")
        Dimension<List<TcFeature>> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, Arrays
                .asList(TcFeatureFactory.create(IsSurroundedByChars.class, IsSurroundedByChars.PARAM_SURROUNDING_CHARS, "\"\"")));


        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_UNIT), dimFeatureSets,
                dimClassificationArgs,
                /*
                 * MANUAL CROSS VALIDATION FOLDS - i.e. the cas created by your reader will be used
                 * as is to make folds
                 */
                Dimension.create(DIM_CROSS_VALIDATION_MANUAL_FOLDS, manualFolds));

        return pSpace;
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(NoOpAnnotator.class);
    }
}
