/**
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
 */

package org.dkpro.tc.ml.svmhmm.task;

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
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatTestTask;
import org.dkpro.tc.ml.svmhmm.util.SvmHmmUtils;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

public class SvmHmmTestTask
	extends LibsvmDataFormatTestTask
	implements Constants
{
    private static final String BINARIES_BASE_LOCATION = "classpath:/org/dkpro/tc/ml/svmhmm/";

    private double paramC = 5;

    private double paramEpsilon = 0.5;

    private int paramOrderT = 1;

    private int paramOrderE = 0;

    private int paramB = 0;

    // where the trained model is stored
    private static final String MODEL_NAME = "svm_struct.model";

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        processParameters(classificationArguments);

        super.execute(aContext);
    }
    
    private void combinePredictionAndExpectedGoldLabels(File fileTest, File predictionsFile) throws Exception {
    	
    	BufferedReader readerPrediction = new BufferedReader(new InputStreamReader(new FileInputStream(predictionsFile), "utf-8"));
    	BufferedReader readerGold = new BufferedReader(new InputStreamReader(new FileInputStream(fileTest), "utf-8"));
		
    	File createTempFile = File.createTempFile("svmhmm", ".txt");
    	BufferedWriter writer =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(createTempFile), "utf-8"));
    	
    	String prediction=null;
    	String gold=null;
    	
    	writer.write("#PREDICTION;GOLD" + "\n");
    	do {
    		
    		prediction = readerPrediction.readLine();
    		gold = readerGold.readLine();
    		
    		if(prediction == null || gold == null){
    			break;
    		}
    		
    		gold = gold.split("\t")[0];
    		writer.write(prediction + ";" + gold+"\n");
    		
    	}while(true);
    	
    	writer.close();
    	readerGold.close();
    	readerPrediction.close();
    	
    	predictionsFile.delete();
    	FileUtils.moveFile(createTempFile, predictionsFile);
	}

	private void processParameters(List<Object> classificationArguments)
    {
    	List<String> stringArgs = new ArrayList<>();
    	for(int i=1; i < classificationArguments.size(); i++){
    		stringArgs.add((String)classificationArguments.get(i));
    	}

		paramC = SvmHmmUtils.getParameterC(stringArgs);
		paramEpsilon = SvmHmmUtils.getParameterEpsilon(stringArgs);
		paramOrderE = SvmHmmUtils.getParameterOrderE_dependencyOfEmissions(stringArgs);
		paramOrderT = SvmHmmUtils.getParameterOrderT_dependencyOfTransitions(stringArgs);
		paramB = SvmHmmUtils.getParameterBeamWidth(stringArgs);
    }

    public static List<String> buildPredictionCommand(File binaryPath, File testFile, File modelLocation,
            File outputPredictions)
                throws IOException
    {
        List<String> result = new ArrayList<>();

        result.add(binaryPath.getAbsolutePath());
        result.add(testFile.getAbsolutePath());
        result.add(modelLocation.getAbsolutePath());
        result.add(outputPredictions.getAbsolutePath());

        return result;
    }


    public static List<String> buildTrainCommand(File binaryPath, File trainingFile, File targetModelLocation,
            double paramC, int paramOrderE, int paramOrderT, double paramEpsilon, int paramB)
    {
        List<String> result = new ArrayList<>();
        result.add(binaryPath.getAbsolutePath());

        // svm struct params
        result.add("-c");
        result.add(String.format(Locale.ENGLISH, "%f", paramC));
        result.add("--e");
        result.add(Integer.toString(paramOrderE));
        result.add("--t");
        result.add(Integer.toString(paramOrderT));
        result.add("-e");
        result.add(String.format(Locale.ENGLISH, "%f", paramEpsilon));
        result.add("--b");
        result.add(Integer.toString(paramB));

        // training file
        result.add(trainingFile.getAbsolutePath());

        // output model
        result.add(targetModelLocation.getAbsolutePath());

        return result;
    }

    public static void runCommand(List<String> command)
        throws Exception
    {
            ProcessBuilder processBuilder = new ProcessBuilder(command).inheritIO();

            // run the process
            Process process = processBuilder.start();
            process.waitFor();
    }
    
    public static File resolveSvmHmmPredictionCommand()
    {
        try {
            return new RuntimeProvider(BINARIES_BASE_LOCATION).getFile("svm_hmm_classify");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File resolveSvmHmmLearnCommand()
    {
        try {
            return new RuntimeProvider(BINARIES_BASE_LOCATION).getFile("svm_hmm_learn");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	protected Object trainModel(TaskContext aContext) throws Exception {

		File trainBinary = resolveSvmHmmLearnCommand();
		File fileTrain = getTrainFile(aContext);
		
		// SvmHmm struggles with paths longer than 255 characters to circumvent this
		// issue, we copy all files together into a local directory to ensure short path
		// names that are below this threshold
		File newTrainFileLocation = new File(trainBinary.getParentFile(), fileTrain.getName());
		File tmpModelLocation = new File(trainBinary.getParentFile(), "model.tmp");
		FileUtils.copyFile(fileTrain, newTrainFileLocation);
		
		List<String> trainCommand = buildTrainCommand(trainBinary, newTrainFileLocation, tmpModelLocation, paramC, paramOrderE, paramOrderT, paramEpsilon, paramB);
		runCommand(trainCommand);
		
		File modelFile = aContext.getFile(MODEL_NAME, StorageService.AccessMode.READWRITE);
		FileUtils.copyFile(tmpModelLocation, modelFile);
		
		newTrainFileLocation.delete();
		tmpModelLocation.delete();
		
		return modelFile;
	}

	@Override
	protected void runPrediction(TaskContext aContext, Object model) throws Exception {
		
		File fileTest = getTestFile(aContext);
		File modelFile = (File) model;
		
		File predictionsFile = aContext.getFile(Constants.FILENAME_PREDICTIONS, AccessMode.READWRITE);
		File binary = resolveSvmHmmPredictionCommand();
		
		
		// SvmHmm struggles with paths longer than 255 characters to circumvent this
		// issue, we copy all files together into a local directory to ensure short path
		// names that are below this threshold
		File localModel = new File(binary.getParentFile(), "model.tmp");
		FileUtils.copyFile(modelFile, localModel);
		File localTestFile = new File(binary.getParentFile(), "testfile.txt");
		FileUtils.copyFile(fileTest, localTestFile);
		
		List<String> predictionCommand = buildPredictionCommand(binary, localTestFile, localModel, predictionsFile);
		runCommand(predictionCommand);
		
		localModel.delete();
		localTestFile.delete();
		
		combinePredictionAndExpectedGoldLabels(fileTest, predictionsFile);		
	}
	
}
