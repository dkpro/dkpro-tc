package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.IOException;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
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
    private List<String> operativeViews;

    @Discriminator
    protected Class<? extends CollectionReader> readerTrain;

    @Discriminator
    protected Class<? extends CollectionReader> readerTest;

    @Discriminator
    protected List<Object> readerTrainParams;

    @Discriminator
    protected List<Object> readerTestParams;

    @Discriminator
    private String featureMode;

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
                aContext.getStorageLocation(output, AccessMode.READWRITE).getPath(),
                BinaryCasWriter.PARAM_FORMAT, "0");

        // check whether we are dealing with pair classification and if so, add PART_ONE and
        // PART_TWO views
        if (featureMode.equals(Constants.FM_PAIR)) {
            AggregateBuilder builder = new AggregateBuilder();
            builder.add(createEngineDescription(aggregate), CAS.NAME_DEFAULT_SOFA,
                    AbstractPairReader.PART_ONE);
            builder.add(createEngineDescription(aggregate), CAS.NAME_DEFAULT_SOFA,
                    AbstractPairReader.PART_TWO);
            aggregate = builder.createAggregateDescription();
        }
        else if (operativeViews != null) {
            AggregateBuilder builder = new AggregateBuilder();
            for (String viewName : operativeViews) {
                builder.add(createEngineDescription(aggregate), CAS.NAME_DEFAULT_SOFA, viewName);
            }
            aggregate = builder.createAggregateDescription();
        }
        // in unit or sequence mode, add cas multiplier
        else if (featureMode.equals(Constants.FM_UNIT) || featureMode.equals(Constants.FM_SEQUENCE)) {
            boolean useSequences = featureMode.equals(Constants.FM_SEQUENCE);

            AnalysisEngineDescription casMultiplier = createEngineDescription(
                    ClassificationUnitCasMultiplier.class,
                    ClassificationUnitCasMultiplier.PARAM_USE_SEQUENCES, useSequences);

            return createEngineDescription(aggregate, casMultiplier, xmiWriter);
        }
        return createEngineDescription(aggregate, xmiWriter);
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }
}