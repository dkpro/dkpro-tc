package de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.serialization;

import java.io.File;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.core.util.SaveModelUtils;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

class ModelSerializationDescription extends ExecutableTaskBase implements Constants {

    @Discriminator
    protected List<Object> pipelineParameters;
    @Discriminator
    protected List<String> featureSet;
    @Discriminator
    private String[] classificationArguments;

    private File outputFolder;

    public void setAndCreateOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
        outputFolder.mkdirs();
    }

    @Override
    public void execute(TaskContext aContext) throws Exception {

        trainAndStoreModel(aContext);

        SaveModelUtils.writeFeatureInformation(outputFolder, featureSet);
        SaveModelUtils.writeFeatureClassFiles(outputFolder, featureSet);
        SaveModelUtils.writeModelParameters(aContext, outputFolder, featureSet, pipelineParameters);
        SaveModelUtils.writeModelAdapterInformation(outputFolder, CRFSuiteAdapter.class.getName());
        SaveModelUtils.writeCurrentVersionOfDKProTC(outputFolder);
    }

    private void trainAndStoreModel(TaskContext aContext) throws Exception {
        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY);
        String trainFileName = CRFSuiteAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File train = new File(trainFolder.getPath() + "/" + trainFileName);

        List<String> commandTrainModel = CRFSuiteTestTask.getTrainCommand(
                outputFolder.getAbsolutePath() + "/" + MODEL_CLASSIFIER, train.getAbsolutePath(),
                classificationArguments != null ? classificationArguments[0] : null);

        Process process = new ProcessBuilder().inheritIO().command(commandTrainModel).start();
        process.waitFor();
    }
}