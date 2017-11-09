/**
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

package org.dkpro.tc.examples.deeplearning.dl4j.doc;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.eval.EvaluationUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.dkpro.tc.ml.deeplearning4j.user.TcDeepLearning4jUser;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class Dl4jDocumentUserCode
    implements TcDeepLearning4jUser
{

    @Override
    public void run(File trainVec, File trainOutcome, File testVec, File testOutcome,
            File embedding, File prediction)
                throws Exception
    {

        int batchSize = 50; // Number of examples in each minibatch
        int nEpochs = 1; // Number of epochs (full passes of training data) to train on

        // DataSetIterators for training and testing respectively
        // Using AsyncDataSetIterator to do data loading in a separate thread; this may improve
        // performance vs. waiting for data to load
		@SuppressWarnings("deprecation")
		WordVectors wordVectors = WordVectorSerializer.loadTxtVectors(embedding);

        NewsIterator iTrain = new NewsIterator.Builder().dataDirectory(trainVec.getParent())
                .wordVectors(wordVectors).batchSize(batchSize).build();

        NewsIterator iTest = new NewsIterator.Builder().dataDirectory(testVec.getParent())
                .wordVectors(wordVectors).batchSize(batchSize).build();


        int inputNeurons = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length; // 100
                                                                                                 // in
                                                                                                 // our
                                                                                                 // case
        int outputs = iTrain.getLabels().size();

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                .updater(Updater.RMSPROP).regularization(true).l2(1e-5)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(1.0).learningRate(0.0018).list()
                .layer(0,
                        new GravesLSTM.Builder().nIn(inputNeurons).nOut(200).activation(Activation.SOFTSIGN)
                                .build())
                .layer(1,
                        new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
                                .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(200)
                                .nOut(outputs).build())
                .pretrain(false).backprop(true).build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));

        System.out.println("Starting training");
        for (int i = 0; i < nEpochs; i++) {
            net.fit(iTrain);
            iTrain.reset();
            System.out.println("Epoch " + i + " complete");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("#Gold\tPrediction" + System.lineSeparator());
        // Run evaluation. This is on 25k reviews, so can take some time
        while (iTest.hasNext()) {
            DataSet t = iTest.next();
            @SuppressWarnings("deprecation")
			INDArray features = t.getFeatureMatrix();
            INDArray lables = t.getLabels();
            // System.out.println("labels : " + lables);
            INDArray outMask = t.getLabelsMaskArray();
            INDArray predicted = net.output(features, false);
            // System.out.println("predicted : " + predicted);
            eval(lables, predicted, outMask, sb);
        }
        iTest.reset();

        FileUtils.writeStringToFile(prediction, sb.toString(), "utf-8");
    }

    private static void eval(INDArray labels, INDArray p, INDArray outMask, StringBuilder sb)
    {
        Pair<INDArray, INDArray> pair = EvaluationUtils.extractNonMaskedTimeSteps(labels, p,
                outMask);

        INDArray realOutcomes = pair.getFirst();
        INDArray guesses = pair.getSecond();

        // Length of real labels must be same as length of predicted labels
        if (realOutcomes.length() != guesses.length())
            throw new IllegalArgumentException(
                    "Unable to evaluate. Outcome matrices not same length");

        INDArray guessIndex = Nd4j.argMax(guesses, 1);
        INDArray realOutcomeIndex = Nd4j.argMax(realOutcomes, 1);

        int nExamples = guessIndex.length();
        for (int i = 0; i < nExamples; i++) {
            int actual = (int) realOutcomeIndex.getDouble(i);
            int predicted = (int) guessIndex.getDouble(i);
            sb.append(actual + "\t" + predicted + System.lineSeparator());
        }
    }
}
