/**
 * Copyright 2018
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
package org.dkpro.tc.examples.deeplearning.keras.regression;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.examples.util.DemoUtils;
import org.dkpro.tc.io.DelimiterSeparatedValuesReader;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.experiment.builder.DeepExperimentBuilder;
import org.dkpro.tc.ml.experiment.builder.ExperimentType;
import org.dkpro.tc.ml.keras.KerasAdapter;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class KerasRegressionWassa
    implements Constants
{
    public static final String LANGUAGE_CODE = "en";

    public static void main(String[] args) throws Exception
    {

		DemoUtils.setDkproHome(KerasRegressionWassa.class.getSimpleName());
		
		  DeepExperimentBuilder builder = new DeepExperimentBuilder();
	        builder.experiment(ExperimentType.TRAIN_TEST, "kerasTrainTest")
	               .dataReaderTrain(getTrainReader())
	               .dataReaderTest(getTestReader())
	               .learningMode(LearningMode.REGRESSION)
	               .featureMode(FeatureMode.DOCUMENT)
	               .preprocessing(getPreprocessing())
	               .pythonPath("/usr/local/bin/python3")
	               .embeddingPath("src/test/resources/wordvector/glove.6B.50d_250.txt")
	               .maximumLength(50)
	               .vectorizeToInteger(true)
	               .machineLearningBackend(
	                           new MLBackend(new KerasAdapter(), "src/main/resources/kerasCode/regression/wassa.py")
	                       )
	               .run();

    }

    private static CollectionReaderDescription getTestReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(
                DelimiterSeparatedValuesReader.class, DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/wassa2017/dev/", DelimiterSeparatedValuesReader.PARAM_LANGUAGE,
                "en", DelimiterSeparatedValuesReader.PARAM_PATTERNS, "*.txt",
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 3,
                DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 1);
    }

    private static CollectionReaderDescription getTrainReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(
                DelimiterSeparatedValuesReader.class, DelimiterSeparatedValuesReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/wassa2017/train/",
                DelimiterSeparatedValuesReader.PARAM_LANGUAGE, "en",
                DelimiterSeparatedValuesReader.PARAM_PATTERNS, "*.txt",
                DelimiterSeparatedValuesReader.PARAM_OUTCOME_INDEX, 3,
                DelimiterSeparatedValuesReader.PARAM_TEXT_INDEX, 1);
    }

    protected static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(BreakIteratorSegmenter.class);
    }
}
