package de.tudarmstadt.ukp.dkpro.tc.core.task.uima;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;

public class ExtractFeaturesConnectorTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void extractFeaturesConnectorTest()
            throws Exception
    {

        File outputPath = folder.newFolder();
        
        // we do not need parameters here, but in case we do :)
        Object[] parameters = new Object[] {
              //    "NAME", "VALUE"
        };
        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));
               
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReader.class, 
                TestReader.PARAM_SOURCE_LOCATION, "src/test/resources/data/*.txt"
        );
        
        AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);
        
        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                parameterList,
                outputPath.getAbsolutePath(),
                JsonDataWriter.class.getName(),
                false,
                false,
                AddIdFeatureExtractor.class.getName()
        );

        SimplePipeline.runPipeline(reader, segmenter, featExtractorConnector);
        
        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)), SimpleFeatureStore.class);
        assertEquals(2, fs.getNumberOfInstances());
        assertEquals(1, fs.getUniqueOutcomes().size());

        System.out.println(FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)));
    }
}
