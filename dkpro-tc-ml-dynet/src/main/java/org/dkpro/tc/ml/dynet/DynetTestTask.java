/*******************************************************************************
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.ml.dynet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.tc.core.DeepLearningConstants;
import org.dkpro.tc.core.PythonConstants;
import org.dkpro.tc.core.ml.TcDeepLearningAdapter;
import org.dkpro.tc.ml.TcClassifierTaskBase;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DynetTestTask
	extends TcClassifierTaskBase implements DeepLearningConstants
{

    protected static final String DEFAULT_SEED = "123456789";

    @Discriminator(name = DIM_PYTHON_INSTALLATION)
    protected String python;

    @Discriminator(name = DIM_SEED_VALUE)
    protected String randomSeed;

    @Discriminator(name = DIM_RAM_WORKING_MEMORY)
    protected String workingMemory;
    
    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    protected List<Object> classificationArgs;

    @Discriminator(name = DIM_MAXIMUM_LENGTH)
    protected Integer maximumLength;

    @Discriminator(name = DIM_DICTIONARY_PATHS)
    protected List<String> dictionaries;

    @Discriminator(name = DIM_VECTORIZE_TO_INTEGER)
    protected boolean intVectorization;

    @Discriminator(name = DyNetConstants.DIM_DYNET_DEVICES)
    protected String deviceIds;

    @Discriminator(name = DyNetConstants.DIM_DYNET_AUTOBATCH)
    protected Integer autoBatch;

    @Discriminator(name = DyNetConstants.DIM_DYNET_GPUS)
    protected Integer numGpus;

    @Discriminator(name = DIM_BIPARTITION_THRESHOLD)
    protected double threshold;

    @Override
    public void execute(TaskContext aContext) throws Exception
    {
    	super.execute(aContext);
        File kerasResultOut = getResultLocation(aContext);
        List<String> command = buildTrainCommand(aContext, kerasResultOut);
        dumpDebug(aContext, command);
        train(command);
    }

    protected void dumpDebug(TaskContext aContext, List<String> command) throws Exception
    {

        StringBuilder sb = new StringBuilder();

        for (String c : command) {
            sb.append(c + " ");
        }
        try {
            FileUtils.writeStringToFile(aContext.getFile("cmdDebug.txt", AccessMode.READWRITE),
                    sb.toString(), UTF_8);
        }
        catch (IOException e) {
            throw new Exception(e);
        }
    }

    protected File getResultLocation(TaskContext aContext)
    {
        return aContext.getFile(FILENAME_PREDICTION_OUT,
                AccessMode.READWRITE);
    }

    protected void train(List<String> command) throws Exception
    {
        Process process = new ProcessBuilder().inheritIO().command(command).start();
        process.waitFor();
    }

    protected List<String> buildTrainCommand(TaskContext aContext, File resultOut) throws Exception
    {
        File trainDataVector = getDataVector(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA);
        File trainOutcomeVector = getDataOutcome(aContext, TEST_TASK_INPUT_KEY_TRAINING_DATA);
        File testDataVector = getDataVector(aContext, TEST_TASK_INPUT_KEY_TEST_DATA);
        File testOutcomeVector = getDataOutcome(aContext, TEST_TASK_INPUT_KEY_TEST_DATA);
        File embeddingPath = getEmbedding(aContext);
        String maxLen = getMaximumLength(aContext);

        python = (python == null) ? "python" : python;

        List<String> command = new ArrayList<>();
        command.add(python);
        command.add(new File(classificationArgs.get(1).toString()).getAbsolutePath());

        command.add(DyNetConstants.DYNET_SEED);
        command.add(randomSeed == null ? DEFAULT_SEED : randomSeed);

        if (workingMemory != null && !workingMemory.equals("")) {
            command.add(DyNetConstants.DYNET_MEMORY);
            command.add(workingMemory);
        }

        if (deviceIds != null && !deviceIds.equals("")) {
            command.add(DyNetConstants.DIM_DYNET_DEVICES);
            command.add(deviceIds);
        }

        if (autoBatch != null) {
            command.add(DyNetConstants.DIM_DYNET_AUTOBATCH);
            command.add(autoBatch.toString());
        }

        if (numGpus != null) {
            command.add(DyNetConstants.DIM_DYNET_GPUS);
            command.add(numGpus.toString());
        }

        command.add(PythonConstants.SEED);
        command.add(randomSeed == null ? DEFAULT_SEED : randomSeed);
        command.add(PythonConstants.TRAIN_DATA);
        command.add(trainDataVector.getAbsolutePath());
        command.add(PythonConstants.TRAIN_OUTCOME);
        command.add(trainOutcomeVector.getAbsolutePath());
        command.add(PythonConstants.TEST_DATA);
        command.add(testDataVector.getAbsolutePath());
        command.add(PythonConstants.TEST_OUTCOME);
        command.add(testOutcomeVector.getAbsolutePath());

        if (embeddingPath != null) {
            command.add(PythonConstants.EMBEDDING);
            command.add(embeddingPath.getAbsolutePath());
        }
        if (randomSeed != null) {
            command.add(PythonConstants.SEED);
            command.add(randomSeed);
        }

        command.add(PythonConstants.MAX_LEN);
        command.add(maxLen);

        if (dictionaries != null && dictionaries.size() > 0) {
            List<String> dicts = retrieveDictionaryPaths(aContext);
            command.add(PythonConstants.DICTIONARIES);
            for (String d : dicts) {
                command.add(d);
            }
        }

        command.add(PythonConstants.PREDICTION_OUT);
        command.add(resultOut.getAbsolutePath());

        return command;
    }

    /**
     * Returns the file pointer to the integer-mapped version of the dictionary if integer mapping
     * is used otherwise the unaltered version
     * 
     * @param aContext
     *          the task context
     * 
     * @return dictionary paths
     */
    protected List<String> retrieveDictionaryPaths(TaskContext aContext)
    {

        List<String> dicts = new ArrayList<>();

        if (intVectorization) {
            for (int i = 0; i < dictionaries.size(); i += 2) {
                File folder = aContext.getFolder(TcDeepLearningAdapter.PREPARATION_FOLDER,
                        AccessMode.READONLY);
                String name = new File(dictionaries.get(i)).getName();
                dicts.add(new File(folder, name).getAbsolutePath());
            }
        }
        else {
            for (int i = 0; i < dictionaries.size(); i += 2) {
                dicts.add(dictionaries.get(i));
            }
        }

        return dicts;
    }

    /**
     * Returns the maximum length which is either user defined and might be shorter than the actual
     * longest sequence, or is the longest sequence in the data if no value is provided
     * 
     * @param aContext
     *            Task Context
     * @return String value of maximum length
     * @throws IOException
     *             in case a read error occurs
     */
    protected String getMaximumLength(TaskContext aContext) throws IOException
    {
        if (maximumLength != null) {
            return maximumLength.toString();
        }

        File folder = aContext.getFolder(TcDeepLearningAdapter.PREPARATION_FOLDER,
                AccessMode.READONLY);
        String maxLenFromFile = FileUtils.readFileToString(
                new File(folder, FILENAME_MAXIMUM_LENGTH), UTF_8);

        return maxLenFromFile;
    }

    protected File getDataOutcome(TaskContext aContext, String key) throws FileNotFoundException
    {
        File folder = aContext.getFolder(key, AccessMode.READONLY);
        File vector = new File(folder, FILENAME_OUTCOME_VECTOR);

        if (!vector.exists()) {
            throw new FileNotFoundException(
                    "Could not locate file [" + FILENAME_OUTCOME_VECTOR
                            + "] in folder [" + folder.getAbsolutePath() + "]");
        }
        return vector;
    }

    protected File getDataVector(TaskContext aContext, String key) throws FileNotFoundException
    {
        File folder = aContext.getFolder(key, AccessMode.READONLY);
        File vector = new File(folder, FILENAME_INSTANCE_VECTOR);

        if (!vector.exists()) {
            throw new FileNotFoundException(
                    "Could not locate file [" + FILENAME_INSTANCE_VECTOR
                            + "] in folder [" + folder.getAbsolutePath() + "]");
        }
        return vector;
    }

    protected File getEmbedding(TaskContext aContext)
    {
        File folder = aContext.getFolder(TcDeepLearningAdapter.EMBEDDING_FOLDER,
                AccessMode.READONLY);
        File embedding = new File(folder, FILENAME_PRUNED_EMBEDDING);

        if (!embedding.exists()) {
            LogFactory.getLog(getClass()).debug(
                    "Did not find an embedding at location [" + folder.getAbsolutePath() + "]");
            return null;
        }

        return embedding;
    }

}
