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
package org.dkpro.tc.examples.shallow.manualFolds;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.lab.Lab;
import org.dkpro.lab.engine.ExecutionException;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.shallow.annotators.UnitOutcomeAnnotator;
import org.dkpro.tc.features.ngram.CharacterNGram;
import org.dkpro.tc.ml.experiment.ExperimentCrossValidation;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import weka.classifiers.bayes.NaiveBayes;

public class WekaManualFoldCrossValidationTest
    extends TestCaseSuperClass implements Constants
{
    
    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei/";

    /*
     * We request more folds than we have files (2) because we set 'manualMode' to true we expect an
     * exception
     */
    @Test(expected = ExecutionException.class)
    public void testManualFoldCrossValdiationException() throws Exception
    {
        runCrossValidation(true, 3);
    }

    private void runCrossValidation(boolean useManualFolds, int numFolds) throws Exception
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, "de",
                TeiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                TeiReader.PARAM_PATTERNS, "a*.xml");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(CharacterNGram.class,
                        CharacterNGram.PARAM_NGRAM_MIN_N, 2, CharacterNGram.PARAM_NGRAM_MAX_N, 3,
                        CharacterNGram.PARAM_NGRAM_USE_TOP_K, 750)));

        Map<String, Object> config = new HashMap<>();
        config.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new WekaAdapter(), NaiveBayes.class.getName() });
        config.put(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass());
        config.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());

        Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_UNIT), dimFeatureSets, mlas,
                /*
                 * MANUAL CROSS VALIDATION FOLDS - i.e. the cas created by your reader will be used
                 * as is to make folds
                 */
                Dimension.create(DIM_CROSS_VALIDATION_MANUAL_FOLDS,useManualFolds));
        
        ExperimentCrossValidation cv = new ExperimentCrossValidation("cv", numFolds);
        cv.setPreprocessing(AnalysisEngineFactory.createEngineDescription(UnitOutcomeAnnotator.class));
        cv.setParameterSpace(pSpace);
        
        Lab.getInstance().run(cv);
    }

    /*
     * We request more folds than we have files (2) without 'manualModel' thus the CAS should be
     * split up and create sufficient many CAS to be distributed into the folds
     */
    @Test
    public void testManualFoldCrossValdiation() throws Exception
    {
        runCrossValidation(true, 2);
    }

}
