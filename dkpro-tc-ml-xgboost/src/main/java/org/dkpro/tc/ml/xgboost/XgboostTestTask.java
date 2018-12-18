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
package org.dkpro.tc.ml.xgboost;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatTestTask;
import org.dkpro.tc.ml.xgboost.core.XgboostPredictor;
import org.dkpro.tc.ml.xgboost.core.XgboostTrainer;

import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;

public class XgboostTestTask
    extends LibsvmDataFormatTestTask
    implements Constants
{

    public static List<String> getClassificationParameters(TaskContext aContext,
            List<Object> classificationArguments, String learningMode)
        throws IOException
    {
        List<String> parameters = new ArrayList<>();
        if (classificationArguments != null) {
            for (int i = 1; i < classificationArguments.size(); i++) {
                String a = (String) classificationArguments.get(i);
                parameters.add(a);
            }
        }

        if (!learningMode.equals(LM_REGRESSION)) {
            File folder = aContext.getFolder(OUTCOMES_INPUT_KEY, AccessMode.READONLY);
            File file = new File(folder, FILENAME_OUTCOMES);
            List<String> outcomes = FileUtils.readLines(file, UTF_8);
            parameters.add("num_class=" + outcomes.size() + "\n");
        }

        return parameters;
    }

    @Override
    protected Object trainModel(TaskContext aContext) throws Exception
    {

        catchWindows32BitUsers();

        File fileTrain = getTrainFile(aContext);
        File model = new File(aContext.getFolder("", AccessMode.READWRITE),
                Constants.MODEL_CLASSIFIER);

        List<String> parameters = getClassificationParameters(aContext, classificationArguments,
                learningMode);

        XgboostTrainer trainer = new XgboostTrainer();
        trainer.train(fileTrain, model, parameters);

        return model;
    }

    protected void catchWindows32BitUsers()
    {
        PlatformDetector pd = new PlatformDetector();
        if (pd.getOs().equals(PlatformDetector.OS_WINDOWS)
                && pd.getArch().equals(PlatformDetector.ARCH_X86_32)) {
            throw new UnsupportedOperationException(
                    "Xgboost is not available for 32bit Windows operating systems. Please use a 64bit version.");
        }
    }

    @Override
    protected void runPrediction(TaskContext aContext, Object model) throws Exception
    {
        File testFile = getTestFile(aContext);

        XgboostPredictor predictor = new XgboostPredictor();
        List<String> prediction = predictor.predict(testFile, (File) model);

        mergePredictionWithGold(aContext, prediction);
    }

    protected void mergePredictionWithGold(TaskContext aContext, List<String> prediction)
        throws Exception
    {

        File fileTest = getTestFile(aContext);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(aContext.getFile(FILENAME_PREDICTIONS, AccessMode.READWRITE)),
                UTF_8))) {

            List<String> gold = readGoldValues(fileTest);

            checkNoDataCondition(gold, fileTest);

            bw.write("#PREDICTION;GOLD" + "\n");
            for (int i = 0; i < gold.size(); i++) {
                String p = prediction.get(i);
                String g = gold.get(i);
                bw.write(p + ";" + g);
                bw.write("\n");
            }
        }
    }

    protected void checkNoDataCondition(List<String> l, File source)
    {
        if (l.isEmpty()) {
            throw new IllegalStateException(
                    "The file [" + source.getAbsolutePath() + "] contains no prediction results");
        }
    }

    protected List<String> readGoldValues(File f) throws Exception
    {
        List<String> goldValues = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), UTF_8))) {

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] split = line.split("\t");
                goldValues.add(split[0]);
            }

        }

        return goldValues;
    }

}