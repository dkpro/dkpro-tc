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
package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramPMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.TestPairReader;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;


public abstract class PPipelineTestBase
{
	protected List<Instance> instanceList;
	protected List<List<String>> outcomeList;
	protected TreeSet<String> featureNames;
	
    protected TemporaryFolder folder;
    protected File lucenePath;
    protected File outputPath;
    protected Object[] parameters;
    protected AnalysisEngineDescription metaCollector;
    protected AnalysisEngineDescription featExtractorConnector;
    
    protected void initialize() throws Exception{
    	folder = new TemporaryFolder();
        lucenePath = folder.newFolder();
        outputPath = folder.newFolder();
        
        instanceList = new ArrayList<Instance>();
        outcomeList = new ArrayList<List<String>>();
    }
    protected String setTestPairsLocation(){
    	return "src/test/resources/data/textpairs.txt";
    }

    protected void runPipeline()
            throws Exception
    {
        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));
               
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestPairReader.class, 
                TestPairReader.PARAM_INPUT_FILE, setTestPairsLocation()
        );
        
        AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);

        AggregateBuilder builder = new AggregateBuilder();
        builder.add(segmenter, Constants.INITIAL_VIEW, Constants.PART_ONE);
        builder.add(segmenter, Constants.INITIAL_VIEW, Constants.PART_TWO);

        getMetaCollector(parameterList);

        getFeatureExtractorCollector(parameterList);

        // run meta collector
        SimplePipeline.runPipeline(reader, builder.createAggregateDescription(), metaCollector);

        // run FE(s)
        SimplePipeline.runPipeline(reader, builder.createAggregateDescription(), featExtractorConnector);

        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)), SimpleFeatureStore.class);
        assertEquals(1, fs.getNumberOfInstances());
        assertEquals(1, fs.getUniqueOutcomes().size());
        
        featureNames = fs.getFeatureNames();

        for (int i=0; i<fs.getNumberOfInstances(); i++) {
            instanceList.add(fs.getInstance(i));
            outcomeList.add(fs.getOutcomes(i));
        }
    }
	protected abstract void getFeatureExtractorCollector(List<Object> parameterList)
		throws ResourceInitializationException;

    //can be overwritten
	protected void getMetaCollector(List<Object> parameterList)
		throws ResourceInitializationException
	{
		metaCollector = AnalysisEngineFactory.createEngineDescription(
                LuceneNGramPMetaCollector.class,
                parameterList.toArray()
        );
	}
}