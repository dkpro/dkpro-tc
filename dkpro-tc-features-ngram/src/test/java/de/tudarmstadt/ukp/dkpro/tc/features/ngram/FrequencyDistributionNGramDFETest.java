/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.io.TestReaderSentenceToDocument;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.DenseFeatureStore;

public class FrequencyDistributionNGramDFETest
{

    FeatureStore fsFrequenceDist;
    int documentNumber;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // @Before
    // public void setupLogging()
    // {
    // System.setProperty("org.apache.uima.logger.class",
    // "org.apache.uima.util.impl.Log4jLogger_impl");
    // }

    private void initialize(int ngramNMin, int ngramNMax, String tfIdfCalculation)
        throws Exception
    {
        File frequencyDistFile = folder.newFile();
        File dfStoreFile = folder.newFile();
        File outputPathFrequencyDist = folder.newFolder();

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
        		TestReaderSentenceToDocument.class, TestReaderSentenceToDocument.PARAM_SENTENCES_FILE,
                "src/test/resources/fd/text1.txt");
        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        ArrayList<Object> parametersFrequencyDist = new ArrayList<Object>(
                Arrays.asList(new Object[] { FrequencyDistributionNGramDFE.PARAM_NGRAM_MIN_N,
                        ngramNMin, FrequencyDistributionNGramDFE.PARAM_NGRAM_MAX_N, ngramNMax,
                        FrequencyDistributionNGramDFE.PARAM_NGRAM_FD_FILE, frequencyDistFile,
                        FrequencyDistributionNGramDFE.PARAM_TF_IDF_CALCULATION, tfIdfCalculation,
                        FrequencyDistributionNGramDFE.PARAM_DFSTORE_FILE, dfStoreFile}));

        AnalysisEngineDescription metaCollectorFrequencyDist = AnalysisEngineFactory
                .createEngineDescription(NGramMetaCollector.class,
                        parametersFrequencyDist.toArray());

        AnalysisEngineDescription featExtractorConnectorFrequencyDist = TaskUtils
                .getFeatureExtractorConnector(parametersFrequencyDist,
                        outputPathFrequencyDist.getAbsolutePath(), JsonDataWriter.class.getName(),
                        Constants.LM_SINGLE_LABEL, Constants.FM_DOCUMENT,
                        DenseFeatureStore.class.getName(), false, false, false, false,
                        FrequencyDistributionNGramDFE.class.getName());

        // run meta collector
        SimplePipeline.runPipeline(reader, segmenter, metaCollectorFrequencyDist);

        // run FE
        SimplePipeline.runPipeline(reader, segmenter, featExtractorConnectorFrequencyDist);

        Gson gson = new Gson();
        fsFrequenceDist = gson.fromJson(FileUtils.readFileToString(new File(
                outputPathFrequencyDist, JsonDataWriter.JSON_FILE_NAME)), DenseFeatureStore.class);

        documentNumber = fsFrequenceDist.getNumberOfInstances();
        assertEquals(5, documentNumber);
    }


    
    @Test
    public void withoutTfIdfCalculation()
        throws Exception
    {
        initialize(1, 3, "false");
        int firstDocumentCounter = 0;
        int secondDocumentCounter = 0;

        for (Instance instance : fsFrequenceDist.getInstances()) {
        	for (Feature feature : instance.getFeatures()) {
				if (feature.getName().equals("ngram_four")){
					firstDocumentCounter++;
					// "ngram_four" is from the 4 sentence
					if (firstDocumentCounter == 4){
						double featureValue = (double) feature.getValue();
						assertEquals(1, featureValue, 0);
					}
				}
				if (feature.getName().equals("ngram_is")){
					secondDocumentCounter++;
					// "ngram_is" can be found at the 2 sentence
					if (secondDocumentCounter == 2){
						double featureValue = (double) feature.getValue();
						assertEquals(1, featureValue, 0);
					}					
				}
			}
		}
        
    }

    @Test
    public void withTfIdfCalculation()
        throws Exception
    {
        initialize(1, 3, "true");
        int firstDocumentCounter = 0;
        int secondDocumentCounter = 0;
        
        for (Instance instance : fsFrequenceDist.getInstances()) {
        	for (Feature feature : instance.getFeatures()) {
				if (feature.getName().equals("ngram_four")){
					firstDocumentCounter++;
					// "ngram_four" is from the 4 sentence
					if (firstDocumentCounter == 4){
						double featureValue = (double) feature.getValue();
						assertEquals(0.2682, featureValue, 0.0001);
					}
				}
				if (feature.getName().equals("ngram_is")){
					secondDocumentCounter++;
					// "ngram_is" can be found at the 2 sentence
					if (secondDocumentCounter == 2){
						double featureValue = (double) feature.getValue();
						assertEquals(0.0610, featureValue, 0.0001);
					}					
				}
			}
		}        
    }
}
