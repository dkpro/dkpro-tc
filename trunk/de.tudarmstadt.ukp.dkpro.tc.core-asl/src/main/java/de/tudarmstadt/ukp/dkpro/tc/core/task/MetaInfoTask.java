package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

/**
 * Iterates over all documents and stores required collection-level meta data, e.g. which n-grams
 * appear in the documents.
 * 
 * @author zesch
 * 
 */
public class MetaInfoTask
    extends UimaTaskBase
{

    public static final String META_KEY = "meta";
    public static final String INPUT_KEY = "input";

    @Discriminator
    protected List<String> featureSet;

    @Discriminator
    protected List<Object> pipelineParameters;

    @Discriminator
    protected boolean isPairClassification;

    private List<Class<? extends MetaCollector>> metaCollectorClasses;

    @Discriminator
    private File filesRoot;

    @Discriminator
    private Collection<String> files_training;

    @Override
    public CollectionReaderDescription getCollectionReaderDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
        // TrainTest setup: input files are set as imports
        if (filesRoot == null || files_training == null) {
            File file = aContext.getStorageLocation(INPUT_KEY, AccessMode.READONLY);
            return createReaderDescription(BinaryCasReader.class,
                    BinaryCasReader.PARAM_SOURCE_LOCATION, file, BinaryCasReader.PARAM_PATTERNS,
                    BinaryCasReader.INCLUDE_PREFIX + "**/*.bin");
        }
        // CV setup: filesRoot and files_atrining have to be set as dimension
        else {
            Collection<String> patterns = new ArrayList<String>();
            for (String f : files_training) {

                patterns.add(BinaryCasReader.INCLUDE_PREFIX + "**/" + f);
            }
            return createReaderDescription(BinaryCasReader.class,
                    BinaryCasReader.PARAM_SOURCE_LOCATION, filesRoot,
                    BinaryCasReader.PARAM_PATTERNS, patterns);
        }
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {

        // check for error conditions
        if (featureSet == null) {
            throw new ResourceInitializationException(new TextClassificationException(
                    "No feature extractors have been added to the experiment."));
        }

        // automatically determine the required metaCollector classes from the provided feature
        // extractors
        try {
            metaCollectorClasses = TaskUtils.getMetaCollectorsFromFeatureExtractors(featureSet);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
        catch (InstantiationException e) {
            throw new ResourceInitializationException(e);
        }
        catch (IllegalAccessException e) {
            throw new ResourceInitializationException(e);
        }

        // collect parameter/key pairs that need to be set
        Map<String, String> parameterKeyPairs = new HashMap<String, String>();
        for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
            try {
                parameterKeyPairs.putAll(metaCollectorClass.newInstance().getParameterKeyPairs());
            }
            catch (InstantiationException e) {
                throw new ResourceInitializationException(e);
            }
            catch (IllegalAccessException e) {
                throw new ResourceInitializationException(e);
            }
        }

        List<Object> parameters = new ArrayList<Object>();
        if (pipelineParameters != null) {
            parameters.addAll(pipelineParameters);
        }

        // make sure that the meta key import can be resolved (even when no meta features have been
        // extracted, as in the regression demo)
        // TODO better way to do this?
        if (parameterKeyPairs.size() == 0) {
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READONLY)
                    .getPath());
            file.mkdir();
        }

        for (String key : parameterKeyPairs.keySet()) {
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READONLY),
                    parameterKeyPairs.get(key));
            parameters.addAll(Arrays.asList(key, file.getAbsolutePath()));
        }

        AggregateBuilder builder = new AggregateBuilder();

        for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
            // check whether we are dealing with pair classification and if so, add PART_ONE and
            // PART_TWO views
            if (isPairClassification) {
                builder.add(createEngineDescription(metaCollectorClass, parameters.toArray()),
                        CAS.NAME_DEFAULT_SOFA, AbstractPairReader.PART_ONE);
                builder.add(createEngineDescription(metaCollectorClass, parameters.toArray()),
                        CAS.NAME_DEFAULT_SOFA, AbstractPairReader.PART_TWO);
            }
            else {
                builder.add(createEngineDescription(metaCollectorClass, parameters.toArray()));
            }
        }
        return builder.createAggregateDescription();
    }
}