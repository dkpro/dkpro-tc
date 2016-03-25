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
package de.tudarmstadt.ukp.dkpro.tc.examples.single.document;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.ParameterSpace;

import weka.classifiers.bayes.NaiveBayes;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.examples.util.DemoUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;

public class TwentyNewsgroupsPreprocessing
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static final int NUM_FOLDS = 3;

    public static final String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";
    
    public static void main(String[] args)
        throws Exception
    {
    	
    	// This is used to ensure that the required DKPRO_HOME environment variable is set.
    	// Ensures that people can run the experiments even if they haven't read the setup instructions first :)
    	// Don't use this in real experiments! Read the documentation and set DKPRO_HOME as explained there.
    	DemoUtils.setDkproHome(TwentyNewsgroupsPreprocessing.class.getSimpleName());
    	
        ParameterSpace pSpace = getParameterSpace();

        TwentyNewsgroupsPreprocessing experiment = new TwentyNewsgroupsPreprocessing();
        experiment.runCrossValidation(pSpace);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace()
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, TwentyNewsgroupsCorpusReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS, asList(
                TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                TwentyNewsgroupsCorpusReader.PARAM_PATTERNS, asList("*/*.txt")));

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                asList( NaiveBayes.class.getName()));

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS, asList(new Object[] {
                		NGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, 100,
                		NGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 2,
                        NGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 4 }));

        Dimension<List<String>> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, asList(
        		NrOfTokensDFE.class.getName(),
        		LuceneNGramDFE.class.getName()));
        
        Dimension<String> dimSegmenter = Dimension.create("segmenter", "break", "opennlp");
        
        ParameterSpace pSpace = new ParameterSpace(
                Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), 
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), 
                dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs, dimSegmenter);

        return pSpace;
    }

    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {

        ExperimentCrossValidation batch = new ExperimentCrossValidation(
                "TwentyNewsgroupsCV-preprocessing",
        		WekaClassificationAdapter.class, NUM_FOLDS)
        {
        	@Discriminator 
        	String segmenter;
        	 
        	
        	@Override
			public AnalysisEngineDescription getPreprocessing() {
				
        		try {
        			if (segmenter.equals("break")) {
                		return createEngineDescription(BreakIteratorSegmenter.class);
        			}
        			else if (segmenter.equals("opennlp")) {
                		return createEngineDescription(OpenNlpSegmenter.class);        				
        			}
        			else {
    					throw new RuntimeException("unexpected discriminator value: " + segmenter);        				
        			}
				} catch (ResourceInitializationException e) {
					throw new RuntimeException(e);
				}

            }
        };
        
        batch.setPreprocessing(createEngineDescription(BreakIteratorSegmenter.class));
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
//        batch.addReport(BatchCrossValidationReport.class);

        Lab.getInstance().run(batch);
    }
}
