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
package de.tudarmstadt.ukp.dkpro.tc.examples.single.unit;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.BrownCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.examples.util.DemoUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensUFE;
import de.tudarmstadt.ukp.dkpro.tc.ml.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaBatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This is an example for POS tagging as unit classification. Each POS is treated as a
 * classification unit, but unlike sequence tagging the decision for each POS is taken
 * independently. This will usually give worse results, so this is only to showcase the concept.
 * 
 */
public class BrownUnitPosDemo
    implements Constants
{

    public static final String LANGUAGE_CODE = "en";

    public static final int NUM_FOLDS = 2;

    public static final String corpusFilePathTrain = "src/main/resources/data/brown_tei/";

    public static void main(String[] args)
        throws Exception
    {
    	// This is used to ensure that the required DKPRO_HOME environment variable is set.
    	// Ensures that people can run the experiments even if they haven't read the setup instructions first :)
    	// Don't use this in real experiments! Read the documentation and set DKPRO_HOME as explained there.
    	DemoUtils.setDkproHome(BrownUnitPosDemo.class.getSimpleName());
    	
        new BrownUnitPosDemo().runCrossValidation(getParameterSpace());
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {

        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("BrownPosDemoCV",
        		WekaAdapter.getInstance(),
                getPreprocessing(), NUM_FOLDS);
        batch.addInnerReport(WekaClassificationReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(WekaBatchCrossValidationReport.class);
        // batch.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    public static ParameterSpace getParameterSpace()
    {
        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, BrownCorpusReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(new Object[] { BrownCorpusReader.PARAM_LANGUAGE, "en",
                        BrownCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                        BrownCorpusReader.PARAM_PATTERNS,
                        new String[] { INCLUDE_PREFIX + "*.xml", INCLUDE_PREFIX + "*.xml.gz" } }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { SMO.class.getName() }),
                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

        @SuppressWarnings("unchecked")
        Dimension<List<Object>> dimPipelineParameters = Dimension.create(DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] { "something", "something" }),
                Arrays.asList(new Object[] { "something2", "something2" }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
                Arrays.asList(new String[] { NrOfTokensUFE.class.getName() }));

        @SuppressWarnings("unchecked")
		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, FM_UNIT), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(createEngineDescription(NoOpAnnotator.class));
    }
}
