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
package de.tudarmstadt.ukp.dkpro.tc.examples.model;

import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.DIM_CLASSIFICATION_ARGS;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.DIM_FEATURE_MODE;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.DIM_FEATURE_SET;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.DIM_LEARNING_MODE;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.DIM_PIPELINE_PARAMS;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.DIM_READER_TRAIN;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.DIM_READER_TRAIN_PARAMS;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.FM_DOCUMENT;
import static de.tudarmstadt.ukp.dkpro.tc.core.Constants.LM_SINGLE_LABEL;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import weka.classifiers.bayes.NaiveBayes;
import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.ml.uima.TcAnnotator;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.SaveModelWekaBatchTask;

public class SaveAndLoadModelTest {

	 @Rule
	  public TemporaryFolder folder= new TemporaryFolder();

	@SuppressWarnings("unchecked")
	@Test
	public void roundTrip() 
			throws Exception
	{
		
	    String trainFolder = "src/main/resources/data/twentynewsgroups/bydate-train";
	    File modelFolder = folder.newFolder();
	    
	    // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, TwentyNewsgroupsCorpusReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS,
                        Arrays.asList(TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION, trainFolder,
                                TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, "en",
                                TwentyNewsgroupsCorpusReader.PARAM_PATTERNS,
                                Arrays.asList(TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX
                                        + "*/*.txt")));

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                		NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 500,
                		NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));

        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] {
                		NrOfTokensDFE.class.getName(),
                		LuceneNGramDFE.class.getName()
                }
        ));

        ParameterSpace pSpace = new ParameterSpace(
        		Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
                dimPipelineParameters,
                dimFeatureSets,
                dimClassificationArgs
        );

	    SaveModelWekaBatchTask batch = new SaveModelWekaBatchTask(
        		"TestSaveModel",
        		modelFolder,
        		WekaClassificationAdapter.class,
        		createEngineDescription(
    	                createEngineDescription(BreakIteratorSegmenter.class),
    	                createEngineDescription(
    	                		OpenNlpPosTagger.class,
    	                		OpenNlpPosTagger.PARAM_LANGUAGE, "en"
    	                )
    	        )
    	);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        Lab.getInstance().run(batch);
	    
	
		SimplePipeline.runPipeline(
				CollectionReaderFactory.createReader(
						StringReader.class,
						StringReader.PARAM_DOCUMENT_TEXT, "This is an example text",
						StringReader.PARAM_LANGUAGE, "en"
				),
				AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
				AnalysisEngineFactory.createEngineDescription(
						TcAnnotator.class,
						TcAnnotator.PARAM_TC_MODEL_LOCATION, modelFolder.getAbsolutePath()
				)
		);
	}
}