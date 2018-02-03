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
package org.dkpro.tc.ml.libsvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.pear.util.FileUtil;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatTestTask;
import org.dkpro.tc.ml.libsvm.api.LibsvmPredict;
import org.dkpro.tc.ml.libsvm.api.LibsvmTrainModel;

import libsvm.svm;
import libsvm.svm_model;

public class LibsvmTestTask
    extends LibsvmDataFormatTestTask
    implements Constants
{

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        throwExceptionIfMultiLabelMode();

        runPrediction(aContext);
    }
    
	@Override
	protected void runPrediction(TaskContext aContext) throws Exception {
		File fileTrain = getTrainFile(aContext);
		File fileTest = getTestFile(aContext);

        BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileTest), "utf-8"));
		
        File model = new File(aContext.getFolder("", AccessMode.READWRITE),
                Constants.MODEL_CLASSIFIER);

        LibsvmTrainModel ltm = new LibsvmTrainModel();
        ltm.run(buildParameters(fileTrain, model));
     
        LibsvmPredict predictor = new LibsvmPredict();
        File predTmp = createTemporaryPredictionFile();
        DataOutputStream output = new DataOutputStream(new FileOutputStream(predTmp));
        svm_model svmModel = svm.svm_load_model(model.getAbsolutePath());
        predictor.predict(r, output, svmModel, 0);
        output.close();

        
        File prediction = getPredictionFile(aContext);
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(prediction), "utf-8"));

        List<String> gold = pickGold(FileUtils.readLines(fileTest, "utf-8"));
        List<String> pred = FileUtils.readLines(predTmp, "utf-8");
        bw.write("#PREDICTION;GOLD" + "\n");
        for (int i = 0; i < gold.size(); i++) {
            String p = pred.get(i); 
            String g = gold.get(i);
            bw.write(p + ";" + g);
            bw.write("\n");
        }
        bw.close();
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

    
    private List<String> pickGold(List<String> readLines)
    {
        List<String> gold = new ArrayList<>();
        for (String l : readLines) {
            if (l.isEmpty()) {
                continue;
            }
            int indexOf = l.indexOf("\t");
            gold.add(l.substring(0, indexOf));
        }

        return gold;
    }

    private File createTemporaryPredictionFile()
        throws IOException
    {
        DateFormat df = new SimpleDateFormat("yyyyddMMHHmmss");
        Date today = Calendar.getInstance().getTime();        
        String now = df.format(today);
        
        File createTempFile = FileUtil.createTempFile("libsvmPrediction" + now, ".libsvm");
        createTempFile.deleteOnExit();
        return createTempFile;
    }


}