/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.ml.liblinear;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter.AdapterNameEntries;

public class LiblinearTestTask
	extends ExecutableTaskBase
	implements Constants
{
	@Discriminator
	private List<String> classificationArguments;
	@Discriminator
	private String featureMode;
	@Discriminator
	private String learningMode;
	@Discriminator
	String threshold;
	
	@Override
	public void execute(TaskContext aContext)
	    throws Exception
	{
	    boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
	    
	    if (multiLabel) {
	    	throw new TextClassificationException("Multi-label requested, but LIBLINEAR only supports single label setups.");
	    }
	    
	    File fileTrain = new File(aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TRAINING_DATA,
	            AccessMode.READONLY).getPath() + "/" + LiblinearAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile));
	    File fileTest = new File(aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TEST_DATA,
	            AccessMode.READONLY).getPath() + "/" + LiblinearAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile));

	    // default for bias is -1, documentation says to set it to 1 in order to get results closer to libsvm
	    // writer adds bias, so if we de-activate that here for some reason, we need to also deactivate it there
	    Problem train = Problem.readFromFile(fileTrain, 1.0);

	    // FIXME this should be configurable over the classificationArgs
	    SolverType solver = SolverType.L2R_LR; // -s 0
	    double C = 1.0;    // cost of constraints violation
	    double eps = 0.01; // stopping criteria

	    Linear.setDebugOutput(null);
	    
	    Parameter parameter = new Parameter(solver, C, eps);
	    Model model = Linear.train(train, parameter);
	    
	    Problem test = Problem.readFromFile(fileTest, 1.0);
	    
	    // FIXME use evaluation module when available
	    int correct = 0;
	    int incorrect = 0;
	    Feature[][] testInstances = test.x;
	    List<Double> predictions = new ArrayList<Double>();
	    for (int i=0; i<testInstances.length; i++) {
		    Feature[] instance = testInstances[i];
		    double prediction = Linear.predict(model, instance);
		    predictions.add(prediction);
		    
		    if (test.y[i] == prediction) {
		    	correct++;
		    }
		    else {
		    	incorrect++;
		    }
	    }
	    double accuracy = (double) correct / (correct + incorrect);

	    // write predictions
	    File predictionsFile = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE),
	    		LiblinearAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.predictionsFile)
	    );
	    LiblinearUtils.savePredictions(predictionsFile, predictions);
	    
	    // evaluate and write results

	    // file to hold prediction results
	    File evalFile = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE),
	    		LiblinearAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.evaluationFile));
	    StringBuilder sb = new StringBuilder();
	    sb.append(ReportConstants.CORRECT + "=" + correct + "\n");
	    sb.append(ReportConstants.INCORRECT + "=" + incorrect + "\n");
	    sb.append(ReportConstants.PCT_CORRECT + "=" + accuracy + "\n");
	    FileUtils.writeStringToFile(evalFile, sb.toString());
	}
}