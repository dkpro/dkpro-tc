/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.examples.single.pair;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.functions.SMO;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.PairTwentyNewsgroupsReader;
import de.tudarmstadt.ukp.dkpro.tc.examples.util.DemoUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity.SimilarityPairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.ml.report.BatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.ml.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import dkpro.similarity.algorithms.lexical.string.CosineSimilarity.NormalizationMode;
import dkpro.similarity.algorithms.lexical.uima.string.CosineSimilarityResource;


/**
 * Demonstrates the usage of external resources within feature extractors, i.e. nested resources in uimaFit.
 * 
 */
public class ExternalResourceDemo
    implements Constants
{

    public static final String experimentName = "PairTwentyNewsgroupsExperiment";
    public static final String languageCode = "en";
    public static final String listFilePathTrain = "src/main/resources/data/twentynewsgroups/pairs/pairslist.train";
    public static final String listFilePathTest = "src/main/resources/data/twentynewsgroups/pairs/pairslist.test";

    public static void main(String[] args)
        throws Exception
    {
    	// This is used to ensure that the required DKPRO_HOME environment variable is set.
    	// Ensures that people can run the experiments even if they haven't read the setup instructions first :)
    	// Don't use this in real experiments! Read the documentation and set DKPRO_HOME as explained there.
    	DemoUtils.setDkproHome(ExternalResourceDemo.class.getSimpleName());
    	
        ParameterSpace pSpace = getParameterSpace();
        ExternalResourceDemo experiment = new ExternalResourceDemo();
        experiment.runTrainTest(pSpace);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace()
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, PairTwentyNewsgroupsReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS,
                Arrays.asList(PairTwentyNewsgroupsReader.PARAM_LISTFILE,
                        listFilePathTrain,
                        PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE,
                        languageCode));
        dimReaders.put(DIM_READER_TEST, PairTwentyNewsgroupsReader.class);
        dimReaders.put(DIM_READER_TEST_PARAMS,
                Arrays.asList(PairTwentyNewsgroupsReader.PARAM_LISTFILE,
                        listFilePathTest,
                        PairTwentyNewsgroupsReader.PARAM_LANGUAGE_CODE,
                        languageCode));

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { SMO.class.getName() }));
        
        ExternalResourceDescription gstResource = ExternalResourceFactory
                .createExternalResourceDescription(CosineSimilarityResource.class,
                		CosineSimilarityResource.PARAM_NORMALIZATION, NormalizationMode.L2.toString());

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {SimilarityPairFeatureExtractor.PARAM_TEXT_SIMILARITY_RESOURCE, gstResource}));

        // This feature is sensible and fast, but gives bad results on the demo data
        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] {SimilarityPairFeatureExtractor.class.getName()}));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, FM_PAIR), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        ExperimentTrainTest batch = new ExperimentTrainTest("TwentyNewsgroupsTrainTest",
        		WekaClassificationAdapter.class,
                getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(BatchOutcomeIDReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
