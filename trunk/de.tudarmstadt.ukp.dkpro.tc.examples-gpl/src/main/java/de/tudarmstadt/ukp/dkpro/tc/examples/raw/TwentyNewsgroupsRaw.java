package de.tudarmstadt.ukp.dkpro.tc.examples.raw;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.examples.io.TwentyNewsgroupsCorpusReader;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfSentencesDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * Example of "raw" usage of DKPro TC without DKPro Lab parts.
 * This is for advanced users that do want to have full control over what is going on
 * without having to use the lab architecture.
 * 
 * @author zesch
 *
 */
public class TwentyNewsgroupsRaw
{
     
    public static void main(String[] args)
        throws Exception
    {
        String LANGUAGE_CODE = "en";

        String corpusFilePathTrain = "src/main/resources/data/twentynewsgroups/bydate-train";

        CollectionReaderDescription trainReader = CollectionReaderFactory.createReaderDescription(
                TwentyNewsgroupsCorpusReader.class,
                TwentyNewsgroupsCorpusReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                TwentyNewsgroupsCorpusReader.PARAM_LANGUAGE, LANGUAGE_CODE,
                TwentyNewsgroupsCorpusReader.PARAM_PATTERNS, "*/*.txt"
        );
        
        AnalysisEngineDescription preprocessing = AnalysisEngineFactory.createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class)
        );
        
        String[] featureExtractors = new String[] {
                NrOfTokensDFE.class.getName(),
                NrOfSentencesDFE.class.getName()
        };

        String outputPath = "target/tn_raw_output";
        boolean addInstanceId = true;
        
        AnalysisEngineDescription featureExtraction = TaskUtils.getFeatureExtractorConnector(
                null,
                outputPath,
                WekaDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL,
                Constants.FM_DOCUMENT,
                addInstanceId,
                featureExtractors
        );
        
        SimplePipeline.runPipeline(trainReader, preprocessing, featureExtraction);
    }
}
