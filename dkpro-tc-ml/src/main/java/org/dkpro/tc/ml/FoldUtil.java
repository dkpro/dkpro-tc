package org.dkpro.tc.ml;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.tc.core.io.ClassificationUnitCasMultiplier;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;

public class FoldUtil
{
    public static void foldUtil(String inputFolder)
        throws Exception
    {
        CollectionReader createReader = CollectionReaderFactory.createReader(BinaryCasReader.class,
                BinaryCasReader.PARAM_SOURCE_LOCATION, inputFolder, BinaryCasReader.PARAM_PATTERNS,
                "*.bin");
        AnalysisEngine multiplier = AnalysisEngineFactory.createEngine(
                ClassificationUnitCasMultiplier.class,
                ClassificationUnitCasMultiplier.PARAM_USE_SEQUENCES, false);
        AnalysisEngine xmiWriter = AnalysisEngineFactory.createEngine(BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION, inputFolder + "/split",
                BinaryCasWriter.PARAM_FORMAT, "6+");

        SimplePipeline.runPipeline(createReader, multiplier, xmiWriter);

    }

}
