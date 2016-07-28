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
package org.dkpro.tc.shadedjar;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.features.length.NrOfChars;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;
import org.dkpro.tc.features.tcu.CurrentUnit;
import org.dkpro.tc.features.tcu.NextUnit;
import org.dkpro.tc.features.tcu.PrevUnit;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureFactory;

import com.google.common.io.Files;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;

public class ModelTrain
    implements Constants
{
    public String trainFile = null;
    public String testFile = null;
    public String homeFolder = null;

    public String experimentName = "integration";
    public String languageCode = "en";

    File home;

    public static void main(String[] args)
        throws Exception
    {
        new ModelTrain().run(args);
    }

    public void run(String[] args)
        throws Exception
    {

        trainFile = args[0];
        testFile = args[1];

        home = Files.createTempDir();
        System.setProperty("DKPRO_HOME", home.getAbsolutePath());

        ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE, Constants.LM_SINGLE_LABEL);

        ModelTrain experiment = new ModelTrain();
        experiment.validation(pSpace);
    }

    @SuppressWarnings("unchecked")
    public ParameterSpace getParameterSpace(String featureMode, String learningMode)
        throws Exception
    {

        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        Dimension<List<TcFeature>> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                Arrays.asList(
                        TcFeatureFactory.create(PrevUnit.class),TcFeatureFactory.create(CurrentUnit.class), TcFeatureFactory.create(NextUnit.class),
                        TcFeatureFactory.create(LuceneCharacterNGram.class,LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 2,
                                LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 4,
                                LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 250,
                                LuceneCharacterNGram.PARAM_NGRAM_LOWER_CASE, false)
                         ));

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                asList(new String[] {
                        CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR }));

        CollectionReaderDescription train = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, languageCode,
                TeiReader.PARAM_SOURCE_LOCATION, trainFile);
        dimReaders.put(DIM_READER_TRAIN, train);

        CollectionReaderDescription test = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, languageCode,
                TeiReader.PARAM_SOURCE_LOCATION, testFile);
        dimReaders.put(DIM_READER_TEST, test);

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, learningMode),
                Dimension.create(DIM_FEATURE_MODE, featureMode), 
                dimFeatureSets, dimClassificationArgs);

        return pSpace;
    }

    protected void validation(ParameterSpace pSpace)
        throws Exception
    {

        ExperimentTrainTest batch = new ExperimentTrainTest(experimentName, CRFSuiteAdapter.class);
        batch.setParameterSpace(pSpace);
        batch.setPreprocessing(
                AnalysisEngineFactory.createEngineDescription(TcPosTaggingWrapper.class));
        batch.addReport(AccuracyReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

}
