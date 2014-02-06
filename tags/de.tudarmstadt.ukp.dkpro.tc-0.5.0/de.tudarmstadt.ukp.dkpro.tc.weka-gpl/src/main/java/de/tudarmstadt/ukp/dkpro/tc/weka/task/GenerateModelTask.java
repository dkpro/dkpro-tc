package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import static de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask.META_KEY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaSerializedModel;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * Builds a model from training data and safes it along with all necessary information to be used in
 * follow-up experiments.
 * 
 * @author daxenberger
 * 
 */
public class GenerateModelTask
    extends ExecutableTaskBase
{
    @Discriminator
    private List<String> featureSet;

    @Discriminator
    private List<Object> pipelineParameters;

    @Discriminator
    private List<String> classificationArguments;

    @Discriminator
    private boolean multiLabel;

    @Discriminator
    private boolean isRegressionExperiment;

    @Discriminator
    private String threshold;

    public static final String INPUT_KEY_TRAIN = "input.train";
    public static final String OUTPUT_KEY = "output";
    public static final String MODEL_KEY = "model.ser";
    public static final String TRAINING_DATA_KEY = "training-data.arff.gz";

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {

        File arffFileTrain = new File(aContext.getStorageLocation(INPUT_KEY_TRAIN,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_KEY);

        Instances trainData = TaskUtils.getInstances(arffFileTrain, multiLabel);

        Instances filteredTrainData;

        if (trainData.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME) != null) {

            int instanceIdOffset = // TaskUtils.getInstanceIdAttributeOffset(trainData);

            trainData.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME).index() + 1;

            Remove remove = new Remove();
            remove.setAttributeIndices(Integer.toString(instanceIdOffset));
            remove.setInvertSelection(false);
            remove.setInputFormat(trainData);

            filteredTrainData = Filter.useFilter(trainData, remove);
        }
        else {
            filteredTrainData = new Instances(trainData);
        }

        List<Attribute> attributes = new ArrayList<Attribute>();
        for (int i = 0; i < filteredTrainData.numAttributes(); i++) {
            Attribute att = filteredTrainData.attribute(i);
            attributes.add(att.copy(att.name()));
        }

        // train model
        List<String> mlArgs = classificationArguments.subList(1, classificationArguments.size());
        Classifier trainedClassifier = AbstractClassifier.forName(classificationArguments.get(0),
                new String[] {});
        ((AbstractClassifier) trainedClassifier).setOptions(mlArgs.toArray(new String[0]));
        trainedClassifier.buildClassifier(filteredTrainData);

        List<String> labels = new ArrayList<String>();
        
        if(multiLabel){
            for (int j = 0; j < trainData.classIndex(); j++) {
                labels.add(trainData.attribute(j).name().split(WekaDataWriter.CLASS_ATTRIBUTE_PREFIX)[1]);
            }
        }
        else{
            for (int i = 0; i < trainData.classAttribute().numValues(); i++) {
                labels.add(trainData.classAttribute().value(i));
            }
        }

        // load meta files and add them to the model
        Set<Class<? extends MetaCollector>> metaCollectorClasses = de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils
                .getMetaCollectorsFromFeatureExtractors(featureSet);
        Map<String, FrequencyDistribution<String>> metaMap = new HashMap<String, FrequencyDistribution<String>>();

        for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
            Map<String, String> parameterKeys = metaCollectorClass.newInstance()
                    .getParameterKeyPairs();
            for (String key : parameterKeys.keySet()) {
                File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READONLY),
                        parameterKeys.get(key));
                FrequencyDistribution<String> freqDist = new FrequencyDistribution<String>();
                freqDist.load(file);
                metaMap.put(key, freqDist);
            }
        }

        // setup model
        WekaSerializedModel model = new WekaSerializedModel(attributes, trainedClassifier,
                threshold, featureSet, labels, pipelineParameters, metaMap);

        // serialize model
        FileOutputStream fileOut = new FileOutputStream(aContext.getStorageLocation(OUTPUT_KEY,
                AccessMode.READWRITE).getPath()
                + "/" + MODEL_KEY);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(model);
        out.close();
        fileOut.close();
    }
}