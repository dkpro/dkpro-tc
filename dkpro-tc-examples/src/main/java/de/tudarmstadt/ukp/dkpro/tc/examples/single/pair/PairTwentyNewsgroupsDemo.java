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
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.functions.SMO;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.PairTwentyNewsgroupsReader;
import de.tudarmstadt.ukp.dkpro.tc.examples.util.DemoUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.ml.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * PairTwentyNewsgroupsExperiment, using Java
 * 
 * The PairTwentyNewsgroupsExperiment takes pairs of news files and trains/tests a binary classifier
 * to learn if the files in the pair are from the same newsgroup. The pairs are listed in a tsv
 * file: see the files in src/main/resources/lists/ as examples.
 * <p>
 * PairTwentyNewsgroupsExperiment uses similar architecture as TwentyNewsgroupsGroovyExperiment (
 * {@link BatchTaskTrainTest}) to automatically wire the standard tasks for a basic TrainTest setup.
 * To remind the user to be careful of information leak when training and testing on pairs of data
 * from similar sources, we do not provide a demo Cross Validation setup here. (Our sample train and
 * test datasets are from separate newsgroups.) Please see TwentyNewsgroupsGroovyExperiment for a
 * demo implementing a CV experiment.
 * 
 * 
 * @author Emily Jamison
 */
public class PairTwentyNewsgroupsDemo
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
    	DemoUtils.setDkproHome(PairTwentyNewsgroupsDemo.class.getSimpleName());
    	
        ParameterSpace pSpace = getParameterSpace();
        PairTwentyNewsgroupsDemo experiment = new PairTwentyNewsgroupsDemo();
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

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
                        "500", FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }),
                Arrays.asList(new Object[] {
                        FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K,
                        "1000", FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N,
                        1, FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));

        // This feature is sensible and fast, but gives bad results on the demo data
        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] { DiffNrOfTokensPairFeatureExtractor.class.getName() }));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, FM_PAIR), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskTrainTest batch = new BatchTaskTrainTest("TwentyNewsgroupsTrainTest",
        		WekaAdapter.getInstance(),
                getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(WekaBatchTrainTestReport.class);
        batch.addReport(WekaBatchOutcomeIDReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(
                createEngineDescription(StanfordSegmenter.class),
                createEngineDescription(StanfordNamedEntityRecognizer.class,
                        StanfordNamedEntityRecognizer.PARAM_VARIANT, "all.3class.distsim.crf"));
    }
}
