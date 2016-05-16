/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.mallet.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.mallet.MalletAdapter;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelVector;
import cc.mallet.types.Labeling;

public class MalletTestTask
    extends ExecutableTaskBase
    implements Constants
{
    public static final String MALLET_ALGO = "malletTrainingAlgo";
    @Discriminator(name = MALLET_ALGO)
    MalletAlgo malletAlgo;

    @Discriminator(name = DIM_FEATURE_MODE)
    String featureMode;

    private double gaussianPriorVariance = 10.0; // Gaussian Prior Variance

    private int iterations = 1000;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {

        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String fileName = MalletAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTrain = new File(trainFolder, fileName);

        File testFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
        File fileTest = new File(testFolder, fileName);

        File fileModel = aContext.getFile(MODEL_CLASSIFIER, AccessMode.READWRITE);

        String pred = MalletAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.predictionsFile);
        File predictions = aContext.getFile(pred, AccessMode.READWRITE);

        switch (featureMode) {
        case FM_DOCUMENT:
            document(fileTrain, fileTest, fileModel, predictions);
            break;
        case FM_SEQUENCE:
            sequence(fileTrain, fileTest, fileModel, predictions);
            break;
        default:
            throw new UnsupportedOperationException(
                    "Feature mode [" + featureMode + "] is not supported");
        }

    }

    private void document(File fileTrain, File fileTest, File fileModel, File predictions)
        throws Exception
    {
        Pipe instancePipe = new SerialPipes(new Pipe[] {
                new Target2Label (),                              // Target String -> class label
//                new Input2CharSequence (),                // Data File -> String containing contents
                new CharSequence2TokenSequence(),
                new TokenSequence2FeatureSequence(),
                new FeatureSequence2FeatureVector() });

        InstanceList trainData = new InstanceList(instancePipe);
        InstanceList testData = new InstanceList(instancePipe);

        // Add all the files in the directories to the list of instances.
        // The Instance that goes into the beginning of the instancePipe
        // will have a File in the "data" slot, and a string from args[] in the "target" slot.
        Reader trainFileReader = new InputStreamReader(new FileInputStream(fileTrain), "UTF-8");
        trainData.addThruPipe(new LineGroupIterator(trainFileReader, Pattern.compile("^\\s*$"), true));

        // Create a classifier trainer, and use it to create a classifier
        ClassifierTrainer naiveBayesTrainer = new NaiveBayesTrainer();
        Classifier classifier = naiveBayesTrainer.train(trainData);
        
        ArrayList<Classification> classify = classifier.classify(testData);
        
        for(Classification c : classify){
            LabelVector labelVector = c.getLabelVector();
            Labeling labeling = c.getLabeling();
            System.out.println();
        }
        
    }

    private void sequence(File fileTrain, File fileTest, File fileModel, File predictions)
        throws Exception
    {
        String string = malletAlgo.toString();
        if (string.startsWith("CRF")) {
            ConditionalRandomFields crf = new ConditionalRandomFields(fileTrain, fileTest,
                    fileModel, predictions, gaussianPriorVariance, iterations, malletAlgo);
            crf.run();
        }
        else if (string.startsWith("HMM")) {
            HiddenMarkov hmm = new HiddenMarkov(fileTrain, fileTest, fileModel, predictions,
                    iterations);
            hmm.run();
        }
        else {
            throw new IllegalStateException("No algorithmen set");
        }
    }

}