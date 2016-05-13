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
package org.dkpro.tc.examples.single.sequence;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DiscriminableReaderCollectionFactory;
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.examples.io.BrownCorpusReader;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.features.length.NrOfTokensUFE;
import org.dkpro.tc.features.ngram.LuceneCharacterNGramUFE;
import org.dkpro.tc.fstore.simple.SparseFeatureStore;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;

/**
 * This a pure Java-based experiment setup of POS tagging as sequence tagging.
 */
public class CRFSuiteBrownPosDemo
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static final int NUM_FOLDS = 2;

    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei/";

    public static void main(String[] args)
        throws Exception
    {

        // This is used to ensure that the required DKPRO_HOME environment variable is set.
        // Ensures that people can run the experiments even if they haven't read the setup
        // instructions first :)
        DemoUtils.setDkproHome(CRFSuiteBrownPosDemo.class.getSimpleName());

        ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE, Constants.LM_SINGLE_LABEL);

        CRFSuiteBrownPosDemo experiment = new CRFSuiteBrownPosDemo();
        experiment.runCrossValidation(pSpace);
    }

    public static ParameterSpace getParameterSpace(String featureMode, String learningMode)
        throws ResourceInitializationException
    {
        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        Object newInstance = DiscriminableReaderCollectionFactory.createReaderDescription(BrownCorpusReader.class, BrownCorpusReader.PARAM_LANGUAGE, "en",
                BrownCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                BrownCorpusReader.PARAM_PATTERNS,
                asList(INCLUDE_PREFIX + "*.xml", INCLUDE_PREFIX + "*.xml.gz"));
        dimReaders.put(DIM_READER_TRAIN, newInstance);

        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimPipelineParameters = Dimension.create(DIM_PIPELINE_PARAMS,
                asList(new Object[] { LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_MIN_N, 2,
                        LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_MAX_N, 4,
                        LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_USE_TOP_K, 50 }));

        @SuppressWarnings("unchecked")
        /* If no algorithm is provided, CRFSuite takes lbfgs */
        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                asList(new String[] { CRFSuiteAdapter.ALGORITHM_AVERAGED_PERCEPTRON }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                asList(new String[] { NrOfTokensUFE.class.getName(),
                        LuceneCharacterNGramUFE.class.getName() }));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, learningMode),
                Dimension.create(DIM_FEATURE_MODE, featureMode),
                Dimension.create(Constants.DIM_FEATURE_STORE, SparseFeatureStore.class.getName()),
                dimPipelineParameters, dimFeatureSets, dimClassificationArgs);

        return pSpace;
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {

        ExperimentCrossValidation batch = new ExperimentCrossValidation("BrownPosDemoCV_CRFSuite",
                CRFSuiteAdapter.class, NUM_FOLDS);
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchCrossValidationReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(NoOpAnnotator.class);
    }
}
