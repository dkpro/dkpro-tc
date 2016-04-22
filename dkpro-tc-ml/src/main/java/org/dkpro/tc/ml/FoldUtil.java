package org.dkpro.tc.ml;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.tc.core.io.ClassificationUnitCasMultiplier;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;

public class FoldUtil
{
    public static String foldUtil(String inputFolder)
        throws Exception
    {

        String output = inputFolder + "/split";

        CollectionReader createReader = CollectionReaderFactory.createReader(BinaryCasReader.class,
                BinaryCasReader.PARAM_SOURCE_LOCATION, inputFolder, BinaryCasReader.PARAM_PATTERNS,
                "*.bin");
        AnalysisEngineDescription multiplier = AnalysisEngineFactory.createEngineDescription(
                ClassificationUnitCasMultiplier.class,
                ClassificationUnitCasMultiplier.PARAM_USE_SEQUENCES, false);
        AnalysisEngineDescription xmiWriter = AnalysisEngineFactory.createEngineDescription(
                BinaryCasWriter.class, BinaryCasWriter.PARAM_TARGET_LOCATION, output,
                BinaryCasWriter.PARAM_FORMAT, "6+");

        AnalysisEngineDescription both = AnalysisEngineFactory.createEngineDescription(multiplier,
                xmiWriter);

        SimplePipeline.runPipeline(createReader, both);

        return output;
    }

}
