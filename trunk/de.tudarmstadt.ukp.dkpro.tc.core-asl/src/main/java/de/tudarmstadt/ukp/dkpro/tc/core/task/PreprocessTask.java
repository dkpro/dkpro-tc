package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.IOException;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.io.ClassificationUnitCasMultiplier;

/**
 * Performs the preprocessing steps, that were configured by the user, on the documents.
 * 
 * @author zesch
 * 
 */
public class PreprocessTask
    extends UimaTaskBase
{
    public static String OUTPUT_KEY_TRAIN = "preprocessorOutputTrain";
    public static String OUTPUT_KEY_TEST = "preprocessorOutputTest";

    private boolean isTesting = false;

    @Discriminator
    protected Class<? extends CollectionReader> readerTrain;

    @Discriminator
    protected Class<? extends CollectionReader> readerTest;

    @Discriminator
    protected List<Object> readerTrainParams;

    @Discriminator
    protected List<Object> readerTestParams;

    @Discriminator
    private boolean isUnitClassification;

    public void setTesting(boolean isTesting)
    {
        this.isTesting = isTesting;
    }

    private AnalysisEngineDescription aggregate;

    public AnalysisEngineDescription getAggregate()
    {
        return aggregate;
    }

    public void setAggregate(AnalysisEngineDescription aAggregate)
    {
        aggregate = aAggregate;
    }

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        Class<? extends CollectionReader> reader = isTesting ? readerTest : readerTrain;
        List<Object> readerParams = isTesting ? readerTestParams : readerTrainParams;

        CollectionReaderDescription readerDesc = createReaderDescription(reader,
                readerParams.toArray());

        return readerDesc;
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        String output = isTesting ? OUTPUT_KEY_TEST : OUTPUT_KEY_TRAIN;
        AnalysisEngineDescription xmiWriter = createEngineDescription(BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION,
                aContext.getStorageLocation(output, AccessMode.READWRITE).getPath());
        if (this.isUnitClassification) {
            AnalysisEngineDescription casMultiplier = createEngineDescription(ClassificationUnitCasMultiplier.class);

            return createEngineDescription(aggregate, casMultiplier, xmiWriter);
        }
        else {
            return createEngineDescription(aggregate, xmiWriter);
        }
    }
}