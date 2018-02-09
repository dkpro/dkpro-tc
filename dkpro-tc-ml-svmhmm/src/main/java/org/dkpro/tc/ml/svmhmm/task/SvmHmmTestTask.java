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
import org.dkpro.tc.api.exception.TextClassificationException;
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
        if (!Constants.LM_SINGLE_LABEL.equals(learningMode)) {
            throw new TextClassificationException(
                    learningMode + " was requested but only single label setup is supported.");
        }
        
        processParameters(classificationArguments);
        
        runPrediction(aContext);

    }
    
    @Override
	protected void runPrediction(TaskContext aContext) throws Exception {
    	
		File fileTrain = getTrainFile(aContext);
		File fileTest = getTestFile(aContext);
		
		File modelFile = aContext.getFile(MODEL_NAME, StorageService.AccessMode.READWRITE);
		List<String> trainCommand = buildTrainCommand(fileTrain, modelFile, paramC, paramOrderE, paramOrderT, paramEpsilon, paramB);
		runCommand(trainCommand);
		
		File predictionsFile = aContext.getFile(Constants.FILENAME_PREDICTIONS, AccessMode.READWRITE);
		List<String> predictionCommand = buildPredictionCommand(fileTest, modelFile, predictionsFile);
		runCommand(predictionCommand);
		
		combinePredictionAndExpectedGoldLabels(fileTest, predictionsFile);
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
    		
    	}while(prediction!= null && gold != null);
    	
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

    public static List<String> buildPredictionCommand(File testFile, File modelLocation,
            File outputPredictions)
                throws IOException
    {
        List<String> result = new ArrayList<>();

        result.add(resolveSvmHmmPredictionCommand());
        result.add(testFile.getAbsolutePath());
        result.add(modelLocation.getAbsolutePath());
        result.add(outputPredictions.getAbsolutePath());

        return result;
    }


    public static List<String> buildTrainCommand(File trainingFile, File targetModelLocation,
            double paramC, int paramOrderE, int paramOrderT, double paramEpsilon, int paramB)
    {
        List<String> result = new ArrayList<>();
        result.add(resolveSvmHmmLearnCommand());

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
    
    public static String resolveSvmHmmPredictionCommand()
    {
        try {
            return new RuntimeProvider(BINARIES_BASE_LOCATION).getFile("svm_hmm_classify")
                    .getAbsolutePath();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String resolveSvmHmmLearnCommand()
    {
        try {
            return new RuntimeProvider(BINARIES_BASE_LOCATION).getFile("svm_hmm_learn")
                    .getAbsolutePath();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
	
}
