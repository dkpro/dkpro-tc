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
package de.tudarmstadt.ukp.dkpro.tc.examples.single.sequence;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.NERDemoReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsUFE;
import de.tudarmstadt.ukp.dkpro.tc.features.style.InitialCharacterUpperCaseUFE;
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.mallet.report.ClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.mallet.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.mallet.writer.MalletDataWriter;

/**
 * Example for NER as sequence classification.
 */
public class NERSequenceDemo
    implements Constants
{

    public static final String LANGUAGE_CODE = "de";
    public static final int NUM_FOLDS = 2;
    public static final String corpusFilePathTrain = "src/main/resources/data/germ_eval2014_ner/";

    public static void main(String[] args)
        throws Exception
    {
        NERSequenceDemo demo = new NERSequenceDemo();
        demo.runCrossValidation(getParameterSpace());
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("NamedEntitySequenceDemoCV",
                getPreprocessing(), NUM_FOLDS);
        batch.addInnerReport(ClassificationReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchCrossValidationReport.class);
                
        // Run
        Lab.getInstance().run(batch);
    }

    public static ParameterSpace getParameterSpace()
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, NERDemoReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(new Object[] { NERDemoReader.PARAM_LANGUAGE, "de",
                		NERDemoReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                		NERDemoReader.PARAM_PATTERNS,
                		INCLUDE_PREFIX + "*.txt" }));

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
        		Arrays.asList(new String[] { NrOfCharsUFE.class.getName(), 
        				InitialCharacterUpperCaseUFE.class.getName()
        		}));

        @SuppressWarnings("unchecked")
        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, MalletDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL), Dimension.create(
                        DIM_FEATURE_MODE, Constants.FM_SEQUENCE), dimFeatureSets);

        return pSpace;
    }
    

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(NoOpAnnotator.class);
    }
}
