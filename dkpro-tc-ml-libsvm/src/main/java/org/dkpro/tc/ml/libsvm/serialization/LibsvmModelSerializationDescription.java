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

package org.dkpro.tc.ml.libsvm.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.libsvm.api.LibsvmTrainModel;

public class LibsvmModelSerializationDescription
    extends ModelSerializationTask
    implements Constants
{

    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    private List<String> classificationArguments;

    boolean trainModel = true;

    private Map<String, Integer> outcome2id = new HashMap<>();

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        trainAndStoreModel(aContext);

        writeModelConfiguration(aContext, LibsvmAdapter.class.getName());
    }

    private void trainAndStoreModel(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
        if (multiLabel) {
            throw new TextClassificationException("Multi-label is not yet implemented");
        }

        buildOutcome2IntegerMap(aContext);
        File fileTrain = replaceOutcomeByIntegers(getTrainFile(aContext));

        File model = new File(outputFolder, Constants.MODEL_CLASSIFIER);

        LibsvmTrainModel ltm = new LibsvmTrainModel();
        ltm.run(buildParameters(fileTrain, model));
        writeOutcomeMappingToThisFolder(aContext);
        copyFeatureNameMappingToThisFolder(aContext);
    }

    private File replaceOutcomeByIntegers(File trainFile)
        throws IOException
    {
    	File parentFile = trainFile.getParentFile();
        File createTempFile = new File(parentFile, "libsvmTrainFile.libsvm");
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(createTempFile), "utf-8"));

        for (String s : FileUtils.readLines(trainFile, "utf-8")) {
            if (s.isEmpty()) {
                continue;
            }
            int indexOf = s.indexOf("\t");
            String outcome = s.substring(0, indexOf);
            Integer integer = outcome2id.get(outcome);
            bw.write(integer.toString());
            bw.write(s.substring(indexOf));
            bw.write("\n");
        }

        bw.close();

        return createTempFile;
    }

    private void buildOutcome2IntegerMap(TaskContext aContext)
        throws IOException
    {
        File folder = aContext.getFolder(Constants.OUTCOMES_INPUT_KEY, AccessMode.READONLY);
        File outcomes = new File(folder, Constants.FILENAME_OUTCOMES);

        Set<String> uniqueOutcomes = new HashSet<>();
        uniqueOutcomes.addAll(FileUtils.readLines(outcomes, "utf-8"));

        int i = 0;
        for (String o : uniqueOutcomes) {
            outcome2id.put(o, i++);
        }
    }

    private void copyFeatureNameMappingToThisFolder(TaskContext aContext)
        throws IOException
    {
        File trainDataFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String mapping = LibsvmAdapter.getFeatureNameMappingFilename();

        FileUtils.copyFile(new File(trainDataFolder, mapping), new File(outputFolder, mapping));
    }

    private File getTrainFile(TaskContext aContext)
    {
        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String trainFileName = LibsvmAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTrain = new File(trainFolder, trainFileName);

        return fileTrain;
    }

    private String[] buildParameters(File fileTrain, File model)
    {
        List<String> parameters = new ArrayList<>();
        if (classificationArguments != null) {
            for (String a : classificationArguments) {
                parameters.add(a);
            }
        }
        parameters.add(fileTrain.getAbsolutePath());
        parameters.add(model.getAbsolutePath());
        return parameters.toArray(new String[0]);
    }

    private void writeOutcomeMappingToThisFolder(TaskContext aContext)
        throws IOException
    {
        String mapping = LibsvmAdapter.getOutcomeMappingFilename();

        String map2String = map2String(outcome2id);
        FileUtils.writeStringToFile(new File(outputFolder, mapping), map2String, "utf-8");
    }

    private String map2String(Map<String, Integer> map)
    {
        StringBuilder sb = new StringBuilder();
        for (String k : map.keySet()) {
            sb.append(k + "\t" + map.get(k) + "\n");
        }

        return sb.toString();
    }

}