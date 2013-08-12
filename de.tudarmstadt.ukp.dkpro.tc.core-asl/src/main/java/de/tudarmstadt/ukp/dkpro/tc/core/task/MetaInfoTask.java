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
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;

public class MetaInfoTask
    extends UimaTaskBase
{

    public static final String META_KEY = "meta";
    public static final String INPUT_KEY = "input";

    @Discriminator
    protected String[] featureSet;

    @Discriminator
    protected Object[] pipelineParameters;

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
            String path = aContext.getStorageLocation(INPUT_KEY, AccessMode.READONLY).getPath();
            return createReaderDescription(SerializedCasReader.class,
                    SerializedCasReader.PARAM_PATH,
                    path + "/", SerializedCasReader.PARAM_PATTERNS,
                    new String[] { SerializedCasReader.INCLUDE_PREFIX + "**/*.ser.gz" });
        }
        // CV setup: filesRoot and files_atrining have to be set as dimension
        else {
            Collection<String> patterns = new ArrayList<String>();
            for (String f : files_training) {

                patterns.add(SerializedCasReader.INCLUDE_PREFIX + "**/*" + f);
            }
            return createReaderDescription(SerializedCasReader.class,
                    SerializedCasReader.PARAM_PATH, filesRoot,
                    SerializedCasReader.PARAM_PATTERNS, patterns);
        }
    }

    @Override
    public AnalysisEngineDescription getAnalysisEngineDescription(TaskContext aContext)
        throws ResourceInitializationException, IOException
    {
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
            parameters.addAll(Arrays.asList(pipelineParameters));
        }

        // make sure that the meta key import can be resolved (even when no meta features have been
        // extracted, as in the regression demo)
        // TODO better way to do this?
        if (parameterKeyPairs.size() == 0) {
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READWRITE)
                    .getPath());
            file.mkdir();
        }

        for (String key : parameterKeyPairs.keySet()) {
            File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READWRITE),
                    parameterKeyPairs.get(key));
            parameters.addAll(Arrays.asList(key, file.getAbsolutePath()));
        }

        List<AnalysisEngineDescription> aeds = new ArrayList<AnalysisEngineDescription>();

        for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
            aeds.add(createEngineDescription(metaCollectorClass, parameters.toArray()));
        }

        return createEngineDescription(aeds.toArray(new AnalysisEngineDescription[0]));
    }
}