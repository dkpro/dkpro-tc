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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.commons.logging.LogFactory;
import org.dkpro.tc.mallet.util.CrfFeatureVectorSequenceConverter;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFCacheStaleIndicator;
import cc.mallet.fst.CRFOptimizableByBatchLabelLikelihood;
import cc.mallet.fst.CRFOptimizableByLabelLikelihood;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.CRFTrainerByStochasticGradient;
import cc.mallet.fst.CRFTrainerByThreadedLabelLikelihood;
import cc.mallet.fst.CRFTrainerByValueGradients;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.ThreadedOptimizable;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.optimize.Optimizable;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;

public class ConditionalRandomFields
{
    private File trainFile;
    private File testFile;
    private File modelFile;
    private Double gaussianPrior;
    private File prediction;
    private Integer iterations;
    private MalletAlgo malletAlgo;

    public ConditionalRandomFields(File fileTrain, File fileTest, File fileModel, File prediction,
            Double gaussianPrior, Integer iterations, MalletAlgo malletAlgo)
    {
        this.trainFile = fileTrain;
        this.testFile = fileTest;
        this.modelFile = fileModel;
        this.prediction = prediction;
        this.gaussianPrior = gaussianPrior;
        this.iterations = iterations;
        this.malletAlgo = malletAlgo;
    }

    public void run()
        throws Exception
    {
        runTrainCRF(trainFile, modelFile);
        runTestCRF(testFile, modelFile, prediction);
    }

    public void runTrainCRF(File trainingFile, File modelFile)
        throws Exception
    {
        Reader trainingFileReader = new InputStreamReader(new FileInputStream(trainingFile),
                "UTF-8");
        InstanceList trainingData = null;
        Pipe p = new CrfFeatureVectorSequenceConverter();
        p.setTargetProcessing(true);

        trainingData = new InstanceList(p);
        trainingData.addThruPipe(
                new LineGroupIterator(trainingFileReader, Pattern.compile("^\\s*$"), true));

        CRF crf = trainCRF(trainingData);

        ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(modelFile));
        s.writeObject(crf);
        s.close();
    }

    public void runTestCRF(File testFile, File modelFile, File filePredictions)
        throws Exception
    {
        Reader testFileReader = new InputStreamReader(new FileInputStream(testFile), "UTF-8");

        ObjectInputStream s = new ObjectInputStream(new FileInputStream(modelFile));
        CRF crf = (CRF) s.readObject();
        s.close();

        Pipe p = crf.getInputPipe();
        p.setTargetProcessing(true);

        InstanceList testData = new InstanceList(p);
        testData.addThruPipe(
                new LineGroupIterator(testFileReader, Pattern.compile("^\\s*$"), true));

        if (p.isTargetProcessing()) {
            Alphabet targets = p.getTargetAlphabet();
            StringBuffer buf = new StringBuffer("Labels:");
            for (int i = 0; i < targets.size(); i++) {
                buf.append(" ").append(targets.lookupObject(i).toString());
            }
        }

        writePrediction(new NoopTransducerTrainer(crf), testData, filePredictions);
    }

    @SuppressWarnings("rawtypes")
    public void writePrediction(TransducerTrainer tt, InstanceList testing, File filePredictions)
        throws Exception
    {
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePredictions), "utf-8"));
        writer.write("#prediction;gold" + "\n");

        Transducer model = tt.getTransducer();

        for (int i = 0; i < testing.size(); i++) {
            Instance instance = testing.get(i);
            Sequence input = (Sequence) instance.getData();
            Sequence gold = (Sequence) instance.getTarget();
            assert(input.size() == gold.size());
            Sequence pred = model.transduce(input);
            assert(pred.size() == gold.size());
            for (int j = 0; j < gold.size(); j++) {
                String g = gold.get(j).toString();
                String p = pred.get(j).toString();
                writer.write(p + ";" + g + "\n");
            }
        }
        writer.close();
    }

    public CRF trainCRF(InstanceList trainingData)
        throws Exception
    {

        CRF crf = new CRF(trainingData.getPipe(), null);
        crf.addStatesForLabelsConnectedAsIn(trainingData);
        crf.addStartState();

        switch (malletAlgo) {
        case CRF_StochasticGradient:
            crfStochasticGradient(crf, trainingData);
            break;
        case CRF_LabelLikelihood:
            crfLabelLikelihood(crf, trainingData);
            break;
        case CRF_ValueGradient:
            crfValueGradient(crf, trainingData);
            break;
        case CRF_ValueGradient_multiThreaded:
            crfValueGradientMultiThreaded(crf, trainingData);
            break;
        case CRF_LabelLikelihood_multiThreaded:
            crfLabelLikelihoodMultiThreaded(crf, trainingData);
            break;
        default:
            throw new UnsupportedOperationException(
                    "[" + malletAlgo.toString() + "] is a not implemented algorithm");
        }

        return crf;

    }

    private void crfValueGradientMultiThreaded(CRF crf, InstanceList trainingData)
    {
        int numThreads = 32;
        CRFOptimizableByBatchLabelLikelihood batchOptLabel = new CRFOptimizableByBatchLabelLikelihood(
                crf, trainingData, numThreads);
        ThreadedOptimizable optLabel = new ThreadedOptimizable(batchOptLabel, trainingData,
                crf.getParameters().getNumFactors(), new CRFCacheStaleIndicator(crf));

        // CRF trainer
        Optimizable.ByGradientValue[] opts = new Optimizable.ByGradientValue[] { optLabel };
        // by default, use L-BFGS as the optimizer
        CRFTrainerByValueGradients trainer = new CRFTrainerByValueGradients(crf, opts);
        iterate(trainer, trainingData);
        optLabel.shutdown();
    }

    private void crfLabelLikelihoodMultiThreaded(CRF crf, InstanceList trainingData)
    {
        CRFTrainerByThreadedLabelLikelihood trainer = new CRFTrainerByThreadedLabelLikelihood(crf,
                4);
        iterate(trainer, trainingData);
        trainer.shutdown();
    }

    private void crfStochasticGradient(CRF crf, InstanceList trainingData)
    {
        CRFTrainerByStochasticGradient trainer = new CRFTrainerByStochasticGradient(crf,
                trainingData);
        iterate(trainer, trainingData);
    }

    private void crfLabelLikelihood(CRF crf, InstanceList trainingData)
    {
        CRFTrainerByLabelLikelihood trainer = new CRFTrainerByLabelLikelihood(crf);
        trainer.setGaussianPriorVariance(gaussianPrior);
        iterate(trainer, trainingData);
    }

    private void crfValueGradient(CRF crf, InstanceList trainingData)
    {
        CRFOptimizableByLabelLikelihood optLabel = new CRFOptimizableByLabelLikelihood(crf,
                trainingData);
        Optimizable.ByGradientValue[] opts = new Optimizable.ByGradientValue[] { optLabel };
        // by default, use L-BFGS as the optimizer
        CRFTrainerByValueGradients trainer = new CRFTrainerByValueGradients(crf, opts);
        iterate(trainer, trainingData);
    }

    private void iterate(TransducerTrainer trainer, InstanceList trainingData)
    {
        boolean converged = false;
        for (int i = 0; i <= iterations; i++) {
            converged = trainer.train(trainingData, 1);
            if (converged) {
                LogFactory.getLog(MalletTestTask.class.getName())
                        .info("Training converged after [" + i + 1 + "] iterations");
                break;
            }
        }
        if (!converged) {
            LogFactory.getLog(MalletTestTask.class.getName())
                    .info("Training did not converge after reaching iteration limit [" + iterations
                            + "] ");
        }
    }
}
